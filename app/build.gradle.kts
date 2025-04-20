plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.proyecto3"
    compileSdk = 35 // Actualizado a 35 para cumplir con los requisitos

    defaultConfig {
        applicationId = "com.example.proyecto3"
        minSdk = 23 // Mantiene compatibilidad con dispositivos antiguos
        targetSdk = 34 // Puedes mantenerlo en 34 para comportamiento de runtime
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configurations.all {
        exclude(group = "com.android.support", module = "support-compat")
    }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // AndroidX (versiones compatibles)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0") // Versión compatible con API 34

    // Credenciales (versiones alternativas si es necesario)
    // implementation("androidx.credentials:credentials:1.2.0") // Versión anterior si es necesario
    // implementation("androidx.credentials:credentials-play-services-auth:1.2.0")

    // Material Calendar View
    implementation("com.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")
    }

    implementation("androidx.multidex:multidex:2.0.1")
}