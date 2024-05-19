plugins {
    alias(libs.plugins.spezikt.library)
    alias(libs.plugins.spezikt.hilt)
}

android {
    namespace = "edu.stanford.spezikt.core.coroutines"
}

dependencies {
    api(libs.bundles.ktx.coroutines)
}