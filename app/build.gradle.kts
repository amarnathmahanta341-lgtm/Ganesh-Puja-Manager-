plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "in.ganeshpuja.manager"
    compileSdk = 35

    defaultConfig {
        applicationId = "in.ganeshpuja.manager"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
