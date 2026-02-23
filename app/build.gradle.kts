// Я описываю сборку приложения Ai Healthy
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.example.airich"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.aihealthy.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // URL сервера задаю в buildTypes ниже
        
    }

    signingConfigs {
        create("debugProject") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debugProject")
            buildConfigField("String", "BACKEND_URL", "\"http://89.169.46.180:3000/\"")
            buildConfigField("boolean", "SUBSCRIPTION_ENABLED", "true") // в debug — триал/подписка включены
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("debugProject")
            buildConfigField("String", "BACKEND_URL", "\"http://89.169.46.180:3000/\"")
            buildConfigField("boolean", "SUBSCRIPTION_ENABLED", "true") // как в debug: экран входа, подписка/триал, аватар
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    bundle {
        language { enableSplit = false }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Room — для локальной БД
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    
    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    
    // Fragment
    implementation(libs.androidx.fragment.ktx)
    
    // RecyclerView
    implementation(libs.androidx.recyclerview)
    
    // CardView
    implementation(libs.androidx.cardview)
    
    // AppCompat
    implementation(libs.androidx.appcompat)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Material Design
    implementation(libs.material)
    
    // MPAndroidChart for mood graph
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Retrofit — я шлю запросы к моему backend
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    
    // Gson для сериализации
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}