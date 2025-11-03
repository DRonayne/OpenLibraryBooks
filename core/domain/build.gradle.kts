plugins {
    id("openlibrarybooks.android.library")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.core.domain"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.rxjava3)
    implementation(libs.rxkotlin)

    testImplementation(libs.kotest.property)
    testImplementation(libs.turbine)
}
