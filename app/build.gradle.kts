import java.io.FileInputStream
import java.util.Properties

plugins {
    with(libs.plugins) {
        alias(android.application)
        alias(kotlin.android)
        alias(compose.compiler)
        alias(ksp)
    }
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val hasKeystore = keystorePropertiesFile.exists() && keystorePropertiesFile.length() > 0
val keystoreProperties = Properties().also { props ->
    if (hasKeystore) props.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "by.alexandr7035.banking"
    compileSdk = 35

    if (hasKeystore) {
        val keyAlias = keystoreProperties["keyAlias"] as String
        val keyPassword = keystoreProperties["keyPassword"] as String
        val storeFile = keystoreProperties["storeFile"] as String
        val storePassword = keystoreProperties["storePassword"] as String
        signingConfigs {
            create("config") {
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
                this.storeFile = file(storeFile)
                this.storePassword = storePassword
            }
        }
    }

    defaultConfig {
        applicationId = "by.alexandr7035.banking"
        minSdk = 23
        targetSdk = 34
        versionCode = 200
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        getByName("release") {
            if (hasKeystore) signingConfig = signingConfigs.getByName("config")
            isMinifyEnabled = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
        getByName("debug") {
            if (hasKeystore) signingConfig = signingConfigs.getByName("config")
            applicationIdSuffix = ".debug"
            isDebuggable = true
            versionNameSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    with(libs) {

        // Android core dependencies
        implementation(androidx.core.ktx)
        implementation(androidx.ui)
        implementation(androidx.material3)
        implementation(androidx.core.splashscreen)
        implementation(androidx.work.runtime.ktx)

        // SL
        implementation(bundles.koin)

        // Images
        implementation(bundles.coil)

        // Room
        implementation(bundles.room)
        ksp(room.compiler)

        // Lifecycle
        implementation(lifecycle.runtime.compose)
        implementation(lifecycle.runtime.ktx)

        // Compose
        implementation(platform(compose.bom))
        implementation(compose.activity)
        implementation(compose.shimmer)
        implementation(compose.state.events)
        implementation(compose.navigation)
        implementation(compose.graphics)
        implementation(compose.tooling.preview)
        implementation(compose.fonts)
        implementation(compose.paging)
        implementation(compose.constraintlayout)

        // Security
        implementation(androidx.security.crypto)
        implementation(androidx.biometric.ktx)

        // Utils
        implementation(permissionx)
        implementation(ksprefs)

        // QR code
        with(zxing) {
            //noinspection GradleDependency
            implementation(core)
            implementation(android) {
                isTransitive = false
            }
        }

        // Test dependencies
        testImplementation(junit)
        androidTestImplementation(androidx.junit)
        androidTestImplementation(androidx.espresso.core)
        androidTestImplementation(platform(compose.bom))
        androidTestImplementation(androidx.ui.test.junit4)

        // Debug dependencies
        debugImplementation(compose.tooling)
        debugImplementation(androidx.ui.test.manifest)
    }
}
