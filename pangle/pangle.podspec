require_relative '../gradle/cocoapods_framework'

module_dir = File.dirname(__FILE__)
MultiAdsCocoapodsFramework.bootstrap(module_dir: module_dir, project_path: ':pangle', framework_name: 'pangleKit')

Pod::Spec.new do |spec|
    spec.name                     = 'pangle'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/saifullanurani/MultiAds'
    spec.source                   = { :http=> ''}
    spec.authors                  = 'Saifullah Nurani'
    spec.license                  = 'MIT'
    spec.summary                  = 'Pangle module for MultiAds library'
    spec.vendored_frameworks      = 'build/cocoapods/framework/pangleKit.framework'
    spec.libraries                = 'c++'
    spec.dependency 'Ads-Global'
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':pangle',
        'PRODUCT_MODULE_NAME' => 'pangleKit',
    }
    spec.script_phases = [
        {
            :name => 'Build pangle',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :output_files => [
                '${PODS_TARGET_SRCROOT}/build/cocoapods/framework/${PRODUCT_MODULE_NAME}.framework/${PRODUCT_MODULE_NAME}'
            ],
            :script => MultiAdsCocoapodsFramework.build_phase_script
        }
    ]
    spec.platforms = { :ios => '14.0' }
end
