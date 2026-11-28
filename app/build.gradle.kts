import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    //aqui
    alias(libs.plugins.ksp)  // <--- Incorporar
}

// Datos de la firma del APK release: se leen de keystore.properties (en la raíz del proyecto).
// Ese archivo y el .jks NO se suben a GitHub porque contienen la clave privada.
val firmaProps = Properties().apply {
    val archivo = rootProject.file("keystore.properties")
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}

android {
    namespace = "com.example.appventas"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appventas"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Firma digital del APK release (keystore creado con keytool)
    signingConfigs {
        if (!firmaProps.isEmpty) {
            create("release") {
                storeFile = rootProject.file(firmaProps.getProperty("storeFile"))
                storePassword = firmaProps.getProperty("storePassword")
                keyAlias = firmaProps.getProperty("keyAlias")
                keyPassword = firmaProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // El APK release sale firmado con nuestra clave
            if (!firmaProps.isEmpty) signingConfig = signingConfigs.getByName("release")
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
    }

    // Necesario para las pruebas con MockK y JUnit 5 (archivos de licencia repetidos)
    packaging {
        resources {
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
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

    //Se agregan mismas dependencias que pasamos en clases
    implementation("androidx.compose.runtime:runtime-livedata")

    // CameraX
    val camerax_version = "1.4.2"

    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")

// ZXing
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

// ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Dependencia para viewModel
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Dependencia para la navegación con Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Íconos clásicos de Compose
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    // Material Symbols (para íconos nuevos como Fastfood, Grass, Cheese, etc.)
    implementation("androidx.compose.material:material-icons-extended:1.7.0-beta01")

// Dependencias Room
    val roomVersion = "2.8.4"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")  // <-- KSP

// Retrofit y Gson Converter (API REST de convenios)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

// Coil: muestra la foto del comprobante guardada en el teléfono
    implementation("io.coil-kt:coil-compose:2.7.0")

// Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")

// JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

// MockK
    testImplementation("io.mockk:mockk:1.13.10")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// 👇 Obligatorio para usar JUnit 5 (Kotest)
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
