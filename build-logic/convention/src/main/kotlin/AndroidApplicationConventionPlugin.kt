import com.android.build.api.dsl.ApplicationExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("io.gitlab.arturbosch.detekt")
                apply("com.diffplug.spotless")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().toString().toInt()
                    targetSdk = libs.findVersion("targetSdk").get().toString().toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
                    val javaVersion = JavaVersion.toVersion(
                        libs.findVersion("javaVersion").get().toString()
                    )
                    sourceCompatibility = javaVersion
                    targetCompatibility = javaVersion
                }

                buildFeatures {
                    compose = true
                }
            }

            // Configure Kotlin options
            tasks.withType(KotlinCompile::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(
                        JvmTarget.fromTarget(
                            libs.findVersion("javaVersion").get().toString()
                        )
                    )
                }
            }

            // Configure Detekt
            extensions.configure<DetektExtension> {
                config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
                buildUponDefaultConfig = true
                allRules = false
                autoCorrect = true
                parallel = true
            }

            tasks.withType<Detekt>().configureEach {
                jvmTarget = libs.findVersion("javaVersion").get().toString()
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                    txt.required.set(true)
                    sarif.required.set(true)
                    md.required.set(true)
                }
            }

            tasks.withType<DetektCreateBaselineTask>().configureEach {
                jvmTarget = libs.findVersion("javaVersion").get().toString()
            }

            // Configure Spotless
            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("**/*.kt")
                    targetExclude("**/build/**/*.kt")
                    ktlint(libs.findVersion("ktlint").get().toString())
                        .editorConfigOverride(
                            mapOf(
                                "indent_size" to "4",
                                "indent_style" to "space",
                                "max_line_length" to "120",
                                "insert_final_newline" to "true",
                                "charset" to "utf-8",
                                "trim_trailing_whitespace" to "true",
                                "end_of_line" to "lf",
                                "ij_kotlin_allow_trailing_comma" to "true",
                                "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
                            )
                        )
                    trimTrailingWhitespace()
                    endWithNewline()
                }

                kotlinGradle {
                    target("**/*.gradle.kts")
                    targetExclude("**/build/**/*.gradle.kts")
                    ktlint(libs.findVersion("ktlint").get().toString())
                        .editorConfigOverride(
                            mapOf(
                                "indent_size" to "4",
                                "indent_style" to "space",
                                "max_line_length" to "120",
                            )
                        )
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }

            // Add Compose dependencies
            dependencies {
                val bom = libs.findLibrary("androidx.compose.bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("androidx.compose.ui").get())
                add("implementation", libs.findLibrary("androidx.compose.ui.graphics").get())
                add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                add("implementation", libs.findLibrary("androidx.compose.material3").get())
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())

                // Add common test dependencies
                add("testImplementation", libs.findLibrary("junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())

                // Add Detekt plugins
                add("detektPlugins", libs.findLibrary("detekt.formatting").get())
                add("detektPlugins", libs.findLibrary("detekt.compose.rules").get())
            }
        }
    }
}
