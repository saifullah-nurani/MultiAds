require 'fileutils'

module MultiAdsCocoapodsFramework
  module_function

  def bootstrap(module_dir:, project_path:, framework_name:)
    framework_path = File.join(module_dir, 'build', 'cocoapods', 'framework', "#{framework_name}.framework")
    return if Dir.exist?(framework_path) && !Dir.empty?(framework_path)

    repo_root = File.expand_path('..', module_dir)
    gradlew = File.join(repo_root, 'gradlew')
    task = "#{project_path}:linkReleaseFrameworkIosSimulatorArm64"

    unless system(gradlew, '-p', repo_root, task)
      raise "Failed to bootstrap #{framework_name}. Run: ./gradlew #{task}"
    end

    source = File.join(module_dir, 'build', 'bin', 'iosSimulatorArm64', 'releaseFramework', "#{framework_name}.framework")
    unless Dir.exist?(source)
      raise "Gradle completed but #{source} was not created"
    end

    FileUtils.mkdir_p(File.dirname(framework_path))
    FileUtils.rm_rf(framework_path)
    FileUtils.cp_r(source, File.dirname(framework_path))
  end

  def build_phase_script
    <<-SCRIPT
      if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
          echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \\"YES\\""
          exit 0
      fi

      set -e

      MODULE_ROOT="$PODS_TARGET_SRCROOT"
      REPO_ROOT="$MODULE_ROOT/.."
      FRAMEWORK_NAME="$PRODUCT_MODULE_NAME"

      case "$CONFIGURATION" in
          *[Rr]elease*)
              BUILD_TYPE="Release"
              OUTPUT_BUILD_TYPE="releaseFramework"
              ;;
          *)
              BUILD_TYPE="Debug"
              OUTPUT_BUILD_TYPE="debugFramework"
              ;;
      esac

      case "$PLATFORM_NAME" in
          iphoneos)
              KOTLIN_TARGET="IosArm64"
              OUTPUT_TARGET="iosArm64"
              ;;
          iphonesimulator)
              if echo "$ARCHS" | grep -q "x86_64" && ! echo "$ARCHS" | grep -q "arm64"; then
                  KOTLIN_TARGET="IosX64"
                  OUTPUT_TARGET="iosX64"
              else
                  KOTLIN_TARGET="IosSimulatorArm64"
                  OUTPUT_TARGET="iosSimulatorArm64"
              fi
              ;;
          *)
              echo "Unsupported platform: $PLATFORM_NAME"
              exit 1
              ;;
      esac

      "$REPO_ROOT/gradlew" -p "$REPO_ROOT" "$KOTLIN_PROJECT_PATH:link${BUILD_TYPE}Framework${KOTLIN_TARGET}"

      SOURCE_FRAMEWORK="$MODULE_ROOT/build/bin/$OUTPUT_TARGET/$OUTPUT_BUILD_TYPE/$FRAMEWORK_NAME.framework"
      TARGET_FRAMEWORK="$MODULE_ROOT/build/cocoapods/framework/$FRAMEWORK_NAME.framework"

      if [ ! -d "$SOURCE_FRAMEWORK" ]; then
          echo "Gradle did not create $SOURCE_FRAMEWORK"
          exit 1
      fi

      rm -rf "$TARGET_FRAMEWORK"
      mkdir -p "$(dirname "$TARGET_FRAMEWORK")"
      cp -R "$SOURCE_FRAMEWORK" "$(dirname "$TARGET_FRAMEWORK")"
    SCRIPT
  end
end
