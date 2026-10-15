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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
