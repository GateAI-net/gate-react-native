#import <React/RCTBridgeModule.h>
@interface RCT_EXTERN_MODULE(GateAI, NSObject)
RCT_EXTERN_METHOD(execute:(NSString *)payload resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
@end
