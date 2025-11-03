plugins {
    id("openlibrarybooks.android.library")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.core.common"
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
}
