Pod::Spec.new do |s|
  s.name = 'GateAIReactNative'
  s.version = '0.1.0'
  s.summary = 'Gate/AI react-native SDK'
  s.homepage = 'https://gate-ai.net'
  s.license = { :type => 'MIT', :file => 'LICENSE' }
  s.author = { 'Gate/AI' => 'support@gate-ai.net' }
  s.source = { :git => 'https://github.com/digerata/GateAI.git', :tag => 'react-native/0.1.0' }
  s.ios.deployment_target = '16.0'
  s.swift_version = '5.0'
  s.source_files = 'ios/**/*.{swift,h,m}'
  s.frameworks = 'Security', 'CryptoKit', 'DeviceCheck'
  s.dependency 'React-Core'
end
