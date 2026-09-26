plugins { id("com.android.application") }

android {
    namespace = "com.aforotanque2026"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.aforotanque2026"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
