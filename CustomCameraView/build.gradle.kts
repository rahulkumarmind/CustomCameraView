plugins {
    id("maven-publish")
    id("com.android.library")
}

android {
    namespace = "com.rahul.customcameraview"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {

                from(components.findByName("release"))

                groupId = "com.github.rahulkumarmind"
                artifactId = "CustomCameraView"
                version = "1.0.0"
            }
        }
    }
}