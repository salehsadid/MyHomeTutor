plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.sadid.myhometutor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sadid.myhometutor"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Signing configuration for release builds
    signingConfigs {
        create("release") {
            // Read from environment variables (GitHub Secrets) or local properties
            val keystoreFile = System.getenv("KEYSTORE_FILE")
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
            val keyAliasName = System.getenv("KEY_ALIAS")
            val keyPass = System.getenv("KEY_PASSWORD")

            if (keystoreFile != null && keystorePassword != null && keyAliasName != null && keyPass != null) {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                keyAlias = keyAliasName
                keyPassword = keyPass
            }
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if available
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.glide)
    implementation(libs.picasso)
    implementation(libs.play.services.auth)
    implementation(libs.circleimageview)
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
    implementation("com.github.yalantis:ucrop:2.2.8")
    implementation(libs.mediarouter)
    implementation("androidx.work:work-runtime:2.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}