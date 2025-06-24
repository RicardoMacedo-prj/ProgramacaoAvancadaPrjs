plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

android {
    namespace = "com.example.stickynotes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stickynotes"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.compose.ui:ui:1.8.3")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.core:core-ktx:1.16.0")
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation ("androidx.compose.ui:ui-tooling:1.8.3")
    implementation ("androidx.compose.material:material-icons-extended:1.7.8")
    implementation ("androidx.compose.material3:material3:1.3.2")
    implementation ("androidx.navigation:navigation-compose:2.9.0")
    implementation ("androidx.activity:activity-compose:1.10.1")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.8.2")
}
