import com.android.build.gradle.LibraryExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
                apply("io.gitlab.arturbosch.detekt")
                apply("com.diffplug.spotless")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion("compileSdk").get().toString().toInt()

                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().toString().toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    consumerProguardFiles("consumer-rules.pro")
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

                packaging {
                    resources {
                        excludes += "/META-INF/LICENSE.md"
                        excludes += "/META-INF/LICENSE-notice.md"
                    }
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
                    freeCompilerArgs.add("-Xannotation-default-target=param-property")
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

            // Add common test dependencies
            dependencies {
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("kotest.runner.junit5").get())
                add("testImplementation", libs.findLibrary("kotest.assertions.core").get())

                add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
                add("androidTestImplementation", libs.findLibrary("mockk.android").get())

                // Add Detekt plugins
                add("detektPlugins", libs.findLibrary("detekt.formatting").get())
                add("detektPlugins", libs.findLibrary("detekt.compose.rules").get())
            }
        }
    }
}
