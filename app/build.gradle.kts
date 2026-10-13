plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// --- Claves de Supabase ---
// Se leen de local.properties (que NO se sube a git) y quedan en BuildConfig.
// Si faltan o la clave no es la pública (sb_publishable_), la compilación se detiene con un mensaje claro.
val propiedadesLocales: Map<String, String> = providers
    .fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText
    .orElse("")
    .get()
    .lines()
    .map { it.trim() }
    .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
    .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim() }

fun propiedadSupabase(nombre: String): String =
    propiedadesLocales[nombre]?.takeIf { it.isNotBlank() } ?: throw GradleException(
        "Falta $nombre en local.properties. Agrega estas dos líneas (sin comillas) y vuelve a sincronizar Gradle:\n" +
            "  SUPABASE_URL=https://TU-PROYECTO.supabase.co\n" +
            "  SUPABASE_ANON_KEY=sb_publishable_...\n" +
            "Las encuentras en Supabase → Project Settings → API Keys. Usa solo la clave publishable, nunca la secreta.",
    )

val supabaseUrl = propiedadSupabase("SUPABASE_URL")
val supabaseClave = propiedadSupabase("SUPABASE_ANON_KEY")

if (!Regex("""https://[a-z0-9]+\.supabase\.co/?""").matches(supabaseUrl)) {
    throw GradleException("SUPABASE_URL en local.properties no tiene el formato https://TU-PROYECTO.supabase.co")
}
if (supabaseClave.startsWith("sb_secret_") || "service_role" in supabaseClave) {
    throw GradleException(
        "SUPABASE_ANON_KEY en local.properties es una clave SECRETA. Nunca la pongas en la app: " +
            "reemplázala por la clave publishable (sb_publishable_...) y revoca la secreta en Supabase.",
    )
}
if (!supabaseClave.startsWith("sb_publishable_")) {
    throw GradleException("SUPABASE_ANON_KEY en local.properties debe ser la clave publishable (empieza por sb_publishable_).")
}

android {
    namespace = "com.finanzia.finanzia"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.finanzia.finanzia"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${supabaseUrl.trimEnd('/')}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseClave\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            // Los tests de integración (paquete "integracion") hablan con Supabase real: solo corren con
            // ./gradlew testDebugUnitTest -Pintegracion. Sin esa opción se excluyen y no cuentan.
            all { prueba ->
                if (project.hasProperty("integracion")) {
                    prueba.filter.includeTestsMatching("com.finanzia.finanzia.integracion.*")
                } else {
                    prueba.filter.excludeTestsMatching("com.finanzia.finanzia.integracion.*")
                }
            }
            // Permite correr los tests de Compose en la computadora (Robolectric), sin emulador.
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    // Supabase (SDK oficial supabase-kt): Auth para cuentas y sesiones, Postgrest para las tablas.
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.okhttp)
    testImplementation(libs.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}