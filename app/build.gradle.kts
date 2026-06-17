import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.reader())
} else {
    throw GradleException("local.properties not found in project root!")
}

val mapkitApiKey = localProperties.getProperty("MAPKIT_API_KEY")
    ?: throw GradleException("MAPKIT_API_KEY not found in local.properties!")

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
}

android {
    namespace = "tk.ifroz.loctrackcar"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "tk.ifroz.LocTrackCar"
        minSdk = 26
        targetSdk = 37
        versionCode = 86
        versionName = "10.1.0"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildTypes.all {
        buildConfigField("String", "MAPKIT_API_KEY", "\"$mapkitApiKey\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.work:work-runtime:2.11.2")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.8")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.19.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.yandex.android:maps.mobile:4.38.1-full")
}