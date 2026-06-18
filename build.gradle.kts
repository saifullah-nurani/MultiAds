plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.mavenPublish) apply false
}

allprojects {
    group = project.findProperty("PROJECT_GROUP")?.toString() ?: ""
    version = project.findProperty("PROJECT_VERSION")?.toString() ?: ""

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    pluginManager.withPlugin("com.android.application") {
        val appExt = extensions.getByType(com.android.build.api.dsl.ApplicationExtension::class.java)
        appExt.packaging {
            resources.excludes.add("META-INF/versions/**")
        }
        appExt.compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
    pluginManager.withPlugin("com.android.library") {
        val libExt = extensions.getByType(com.android.build.api.dsl.LibraryExtension::class.java)
        libExt.packaging {
            resources.excludes.add("META-INF/versions/**")
        }
        libExt.compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }
}