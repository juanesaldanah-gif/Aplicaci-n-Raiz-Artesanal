plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.raizartesanal.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.raizartesanal.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    implementation(project(":core:device"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:catalog"))
    implementation(project(":feature:cart"))
    implementation(project(":feature:payments"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:admin"))
}
