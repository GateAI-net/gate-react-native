import { NativeModules, Platform } from 'react-native';
import { GateAIClientBase, GateAIError, type GateAIConfiguration } from './client';
export { GateAIError } from './client';
export type { GateAIConfiguration, GateAIContext, GateAIRequest, GateAIResponse } from './client';
export type GateAIClient = GateAIClientBase;
export const GateAIClient = {
  async create(configuration: GateAIConfiguration): Promise<GateAIClient> {
    if (Platform.OS !== 'ios' && Platform.OS !== 'android') throw new GateAIError('unsupported_platform', 'Gate/AI requires iOS or Android.');
    const native = NativeModules.GateAI;
    if (!native?.execute) throw new GateAIError('not_linked', 'Gate/AI native module is missing. Install pods and rebuild your app. Expo Go is not supported; use a development build.');
    return GateAIClientBase.connect(payload => native.execute(payload), configuration);
  },
};
