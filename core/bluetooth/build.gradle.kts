plugins {
    alias(libs.plugins.spezikt.library)
    alias(libs.plugins.spezikt.hilt)
}

android {
    namespace = "edu.stanford.spezikt.bluetooth"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:coroutines"))
}