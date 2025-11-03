plugins {
    id("openlibrarybooks.android.library")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.core.database"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,COPYRIGHT}"
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.rxjava3)
    ksp(libs.androidx.room.compiler)

    implementation(libs.rxjava3)
    implementation(libs.rxkotlin)

    implementation(libs.gson)

    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.room.testing)
}
