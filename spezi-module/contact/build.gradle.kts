plugins {
    alias(libs.plugins.spezikt.android.library.compose)
}

android {
    namespace = "edu.stanford.spezikt.spezi_module.contact"
}

dependencies {
    androidTestImplementation(libs.bundles.mockito)
}