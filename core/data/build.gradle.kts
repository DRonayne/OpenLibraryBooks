plugins {
    id("openlibrarybooks.android.library")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.core.data"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,COPYRIGHT}"
        }
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))

    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    implementation(libs.kotlinx.coroutines.rx3)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
