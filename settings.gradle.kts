rootProject.name = "MultiAdsProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://artifact.bytedance.com/repository/pangle/")
        }
        maven {
            url = uri("https://android-sdk.is.com/")
        }
    }
}

include(":sample")
include(":androidApp")
include(":core")
include(":admob")
include(":man")
include(":applovin")
include(":inmobi")
include(":vungle")
include(":pangle")
include(":is")
include(":multi-ads")