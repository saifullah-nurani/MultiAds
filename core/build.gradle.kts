plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
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
        namespace = "io.github.saifullah.nurani.ads.core.compose"
        compileSdk = 36
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
            baseName = "coreKit"
            isStatic = true
        }
    }

    val isCocoapodsEnabled = project.plugins.hasPlugin("org.jetbrains.kotlin.native.cocoapods")
    if (isCocoapodsEnabled) {
        extensions.configure<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension> {
            summary = "Core module for MultiAds library"
            homepage = "https://github.com/saifullah-nurani/MultiAds"
            license = "MIT"
            authors = "Saifullah Nurani"
            extraSpecAttributes["platforms"] = "{ :ios => '14.0' }"
            framework {
                baseName = "coreKit"
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
                api("org.jetbrains.compose.foundation:foundation:1.10.0")
                implementation(libs.kotlin.stdlib)
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
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
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
        description.set("MultiAds: A professional Multiplatform Ad Management Library core module.")
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
