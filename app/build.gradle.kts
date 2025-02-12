plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.myapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapp"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res/raw")
        }
    }
}

dependencies {
	implementation ("com.google.android.material:material:1.9.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    implementation(libs.commons.collections4)
    implementation(libs.xmlbeans)
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.cloud:google-cloud-texttospeech:2.15.0")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.cloud:google-cloud-translate:2.20.0")
    implementation("io.grpc:grpc-okhttp:1.57.0")
    implementation ("androidx.media:media:1.7.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("com.google.cloud:google-cloud-speech:2.6.0")
    implementation ("io.grpc:grpc-okhttp:1.48.1")
    implementation ("io.grpc:grpc-protobuf:1.48.1")
    implementation ("io.grpc:grpc-stub:1.48.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.6.0")

    
}