import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    alias(libs.plugins.composeCompiler)
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
        namespace = "io.github.saifullah.nurani.ads.is"
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
    val xcf = XCFramework("isKit")

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "isKit"
            isStatic = true
            xcf.add(this)

            val xcframeworkPath = projectDir.resolve("frameworks/IronSource.xcframework")
            val frameworkArchDir = when (target.name) {
                "iosArm64" -> "ios-arm64"
                "iosX64", "iosSimulatorArm64" -> "ios-arm64_x86_64-simulator"
                else -> error("Unsupported target: ${target.name}")
            }
            val frameworkPath = xcframeworkPath.resolve(frameworkArchDir)

            linkerOpts("-framework", "IronSource", "-F$frameworkPath")
            linkerOpts("-ObjC")

            // IronSourceAdQuality is a transitive static library dependency of IronSourceSDK
            val adQualityXcframeworkPath = projectDir.resolve("frameworks/IronSourceAdQualitySDK.xcframework")
            val adQualityArchDir = when (target.name) {
                "iosArm64" -> "ios-arm64"
                "iosX64", "iosSimulatorArm64" -> "ios-arm64_x86_64-simulator"
                else -> error("Unsupported target: ${target.name}")
            }
            val adQualityPath = adQualityXcframeworkPath.resolve(adQualityArchDir)
            val adQualityLib = adQualityPath.resolve("libIronSourceAdQuality.a")
            linkerOpts(adQualityLib.absolutePath)
        }

        target.compilations.getByName("main") {
            cinterops {
                val IronSource by creating {
                    val xcframeworkPath = projectDir.resolve("frameworks/IronSource.xcframework")
                    val frameworkArchDir = when (target.name) {
                        "iosArm64" -> "ios-arm64"
                        "iosX64", "iosSimulatorArm64" -> "ios-arm64_x86_64-simulator"
                        else -> error("Unsupported target: ${target.name}")
                    }
                    val frameworkPath = xcframeworkPath.resolve(frameworkArchDir)

                    defFile(project.file("src/nativeInterop/cinterop/IronSource.def"))

                    compilerOpts("-framework", "IronSource", "-F$frameworkPath")
                    compilerOpts("-I$frameworkPath/IronSource.framework/Headers")
                }
            }
        }
    }

    val isCocoapodsEnabled = project.plugins.hasPlugin("org.jetbrains.kotlin.native.cocoapods")
    if (isCocoapodsEnabled) {
        extensions.configure<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension> {
            summary = "IronSource module for MultiAds library"
            homepage = "https://github.com/saifullah-nurani/MultiAds"
            license = "MIT"
            authors = "Saifullah Nurani"
            extraSpecAttributes["platforms"] = "{ :ios => '14.0' }"
            pod("IronSourceSDK")
            pod("IronSourceAdQualitySDK")
            framework {
                baseName = "isKit"
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
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                api(libs.ironSource.sdk)
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
        description.set("MultiAds: A professional Multiplatform Ad Management Library is module.")
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
        
        withXml {
            val repositories = asNode().appendNode("repositories")
            val repository = repositories.appendNode("repository")
            repository.appendNode("id", "ironsource")
            repository.appendNode("url", "https://android-sdk.is.com/")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
