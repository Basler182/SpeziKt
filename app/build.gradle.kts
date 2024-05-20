plugins {
    alias(libs.plugins.spezikt.application)
    alias(libs.plugins.spezikt.compose)
    alias(libs.plugins.spezikt.hilt)
}

android {
    namespace = "edu.stanford.spezikt"

    defaultConfig {
        applicationId = "edu.stanford.spezikt"
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/**.md"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.view.model.ktx)

    implementation(libs.hilt.navigation.compose)

    implementation(project(":core:bluetooth"))
    implementation(project(":spezi-module:onboarding"))
    implementation(project(":core:coroutines"))
}