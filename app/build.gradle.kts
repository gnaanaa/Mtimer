import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    id("org.owasp.dependencycheck")
}

dependencyCheck {
    failBuildOnCVSS = 7.0f // Fail on High/Critical vulnerabilities
    format = "HTML"
    outputDirectory = "build/reports/dependency-check"
}

android {
    namespace = "com.gnaanaa.mtimer"
    compileSdk = 36

    val secretsPropertiesFile = rootProject.file("secrets.properties")
    val secretsProperties = Properties()
    if (secretsPropertiesFile.exists()) {
        secretsProperties.load(FileInputStream(secretsPropertiesFile))
    }

    defaultConfig {
        applicationId = "com.gnaanaa.mtimer"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Priority: Environment Variables (CI) -> secrets.properties (Local)
            val isCI = System.getenv("CI") == "true"
            
            storeFile = if (isCI) file("release.jks") else secretsProperties.getProperty("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = System.getenv("RELEASE_STORE_PASSWORD") ?: secretsProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: secretsProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: secretsProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.x")

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)

    implementation(libs.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.datastore)
    implementation(libs.health.connect)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.work.runtime)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.fitness)
    implementation(libs.google.api.client)
    implementation(libs.google.drive.api)
    implementation(libs.google.http.client.gson)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
