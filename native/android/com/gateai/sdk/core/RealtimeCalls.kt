package com.gateai.sdk.core

import com.gateai.sdk.network.GateApiException
import java.util.UUID

/**
 * The result of starting an OpenAI Realtime call through the gate.
 *
 * @property sdp The SDP answer to apply with `PeerConnection.setRemoteDescription`.
 * @property callId OpenAI's identifier for the call (from the `Location` header), when
 * present. Gate/AI records the call's usage under this ID, so it can be used to correlate a
 * session with the rows in your analytics.
 */
data class RealtimeCallAnswer(
    val sdp: String,
    val callId: String?
)

/**
 * Starts an OpenAI Realtime WebRTC call through the gate.
 *
 * Create a `PeerConnection`, generate an SDP offer, and pass it here together with the
 * session configuration (for example `{"type": "realtime", "model": "gpt-realtime"}`). The
 * gate forwards the offer to OpenAI with your provider key and returns the SDP answer to
 * apply to the peer connection. Audio then flows directly between the device and OpenAI,
 * while the gate meters the call and applies your usage limits for its whole duration.
 *
 * ```kotlin
 * val answer = client.createRealtimeCall(
 *     offerSdp = offer.description,
 *     sessionJson = """{"type":"realtime","model":"gpt-realtime"}"""
 * )
 * peerConnection.setRemoteDescription(observer, SessionDescription(SessionDescription.Type.ANSWER, answer.sdp))
 * ```
 *
 * Ephemeral client secrets (`/v1/realtime/client_secrets`) are not available through a gate,
 * because a session started with one bypasses metering and limits.
 *
 * @param offerSdp The local SDP offer.
 * @param sessionJson OpenAI session configuration as a JSON string.
 * @param additionalHeaders Extra headers for the request, such as `X-App-Feature`.
 * @throws GateApiException when the gate or OpenAI rejects the call (including when a device
 * usage limit has been reached).
 */
suspend fun GateAIClient.createRealtimeCall(
    offerSdp: String,
    sessionJson: String,
    additionalHeaders: Map<String, String> = emptyMap()
): RealtimeCallAnswer {
    val boundary = "gateai-${UUID.randomUUID()}"
    val response = performProxyRequest(
        path = RealtimeCallRequest.PATH,
        method = HttpMethod.POST,
        body = RealtimeCallRequest.multipartBody(offerSdp, sessionJson, boundary),
        additionalHeaders = additionalHeaders + ("Content-Type" to "multipart/form-data; boundary=$boundary")
    )
    val sdp = response.body?.takeIf { it.isNotEmpty() }
        ?: throw GateApiException(response.status, response.headers, response.body)
    val location = response.headers.entries.firstOrNull { it.key.equals("Location", ignoreCase = true) }?.value
    return RealtimeCallAnswer(sdp = sdp, callId = RealtimeCallRequest.callIdFromLocation(location))
}

/**
 * Builds the `multipart/form-data` body OpenAI's `POST /v1/realtime/calls` expects: the SDP
 * offer plus the session configuration as JSON.
 */
internal object RealtimeCallRequest {
    const val PATH = "v1/realtime/calls"

    fun multipartBody(offerSdp: String, sessionJson: String, boundary: String): ByteArray = buildString {
        append("--").append(boundary).append("\r\n")
        append("Content-Disposition: form-data; name=\"sdp\"\r\n")
        append("Content-Type: application/sdp\r\n\r\n")
        append(offerSdp)
        append("\r\n--").append(boundary).append("\r\n")
        append("Content-Disposition: form-data; name=\"session\"\r\n")
        append("Content-Type: application/json\r\n\r\n")
        append(sessionJson)
        append("\r\n--").append(boundary).append("--\r\n")
    }.toByteArray(Charsets.UTF_8)

    fun callIdFromLocation(location: String?): String? {
        val last = location?.substringBefore('?')?.trimEnd('/')?.substringAfterLast('/') ?: return null
        return last.takeIf { it.startsWith("rtc_") }
    }
}
