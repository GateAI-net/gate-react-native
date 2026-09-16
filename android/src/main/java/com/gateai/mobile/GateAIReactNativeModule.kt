package com.gateai.mobile

import com.facebook.react.bridge.*
import com.facebook.react.ReactPackage
import com.facebook.react.uimanager.ViewManager
import kotlinx.coroutines.*

class GateAIReactNativeModule(context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {
    private val implementation = GateMobileBridge(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun getName() = "GateAI"
    @ReactMethod
    fun execute(payload: String, promise: Promise) {
        scope.launch {
            try { promise.resolve(implementation.execute(payload)) }
            catch (e: CancellationException) { promise.reject("cancelled", "Native module was destroyed."); throw e }
        }
    }
    override fun invalidate() { scope.cancel(); super.invalidate() }
}

class GateAIPackage : ReactPackage {
    override fun createNativeModules(context: ReactApplicationContext): List<NativeModule> = listOf(GateAIReactNativeModule(context))
    override fun createViewManagers(context: ReactApplicationContext): List<ViewManager<*, *>> = emptyList()
}
