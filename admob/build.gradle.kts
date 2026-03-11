plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.mavenPublish)
}

val isCocoapodsEnabled = project.findProperty("PROJECT_ENABLE_COCOAPODS")?.toString()?.toBoolean() ?: true
if (isCocoapodsEnabled) {
    apply(plugin = "org.jetbrains.kotlin.native.cocoapods")
}



kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.saifullah.nurani.ads.admob"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        androidResources {
            this.enable = true
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "admobKit"
            isStatic = true

            val xcframeworkPath = projectDir.resolve("frameworks/GoogleMobileAds.xcframework")
            val frameworkArchDir = when (target.name) {
                "iosArm64" -> "ios-arm64"
                "iosX64", "iosSimulatorArm64" -> "ios-arm64_x86_64-simulator"
                else -> error("Unsupported target: ${target.name}")
            }
            val frameworkPath = xcframeworkPath.resolve(frameworkArchDir)

            linkerOpts("-framework", "GoogleMobileAds", "-F$frameworkPath")
            linkerOpts("-ObjC")
        }

        target.compilations.getByName("main") {
            cinterops {
                val GoogleMobileAds by creating {
                    val xcframeworkPath = projectDir.resolve("frameworks/GoogleMobileAds.xcframework")
                    val frameworkArchDir = when (target.name) {
                        "iosArm64" -> "ios-arm64"
                        "iosX64", "iosSimulatorArm64" -> "ios-arm64_x86_64-simulator"
                        else -> error("Unsupported target: ${target.name}")
                    }
                    val frameworkPath = xcframeworkPath.resolve(frameworkArchDir)

                    defFile(project.file("src/nativeInterop/cinterop/GoogleMobileAds.def"))

                    // Tell cinterop where to find the headers for this framework
                    compilerOpts("-framework", "GoogleMobileAds", "-F$frameworkPath")

                    // Add specific paths so the c-interop tool knows how to resolve `#import <GoogleMobileAds/GoogleMobileAds.h>`
                    compilerOpts("-I$frameworkPath/GoogleMobileAds.framework/Headers")
                }
            }
        }
    }

    val isCocoapodsEnabled = project.plugins.hasPlugin("org.jetbrains.kotlin.native.cocoapods")
    if (isCocoapodsEnabled) {
        extensions.configure<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension> {
            summary = "Admob module for MultiAds library"
            homepage = "https://github.com/saifullah-nurani/MultiAds"
            license = "MIT"
            authors = "Saifullah Nurani"
            extraSpecAttributes["platforms"] = "{ :ios => '14.0' }"
            pod("Google-Mobile-Ads-SDK")
            framework {
                baseName = "admobKit"
                isStatic = true
            }
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(project(":core"))
                implementation(libs.compose.runtime)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                api(libs.play.services.ads)
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

tasks.withType<com.android.build.gradle.tasks.BundleAar> {
    from("consumer-rules.pro") {
        rename { "proguard.txt" }
    }
}

mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name.set(project.findProperty("PROJECT_NAME")?.toString() ?: "")
        description.set("MultiAds: A professional Multiplatform Ad Management Library admob module.")
        url.set("https://github.com/saifullah-nurani/MultiAds")
        
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        
        developers {
            developer {
                id.set("saifullah-nurani")
                name.set("Saifullah Nurani")
                email.set("donaldperryman04@gmail.com")
            }
        }
        
        scm {
            connection.set("scm:git:github.com/saifullah-nurani/MultiAds.git")
            developerConnection.set("scm:git:ssh://github.com/saifullah-nurani/MultiAds.git")
            url.set("https://github.com/saifullah-nurani/MultiAds")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
