plugins {
    id("openlibrarybooks.android.library.compose")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.feature.widget"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,COPYRIGHT}"
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":core:designsystem"))

    // Glance for widget
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Material Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Image loading with Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // WorkManager for widget updates
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.rxjava3)
    implementation(libs.androidx.hilt.work)

    // RxJava
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)
    implementation(libs.kotlinx.coroutines.rx3)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.assertions.core)
}
