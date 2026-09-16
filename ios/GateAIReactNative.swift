import Foundation
import React

@objc(GateAI)
final class GateAIReactNative: NSObject {
    @MainActor private lazy var implementation = GateMobileBridge()
    @objc static func requiresMainQueueSetup() -> Bool { false }
    @objc(execute:resolver:rejecter:)
    func execute(_ payload: String, resolver resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        Task { @MainActor in
            resolve(await implementation.execute(payload))
        }
    }
}
