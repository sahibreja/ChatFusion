plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.reja.chatapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.reja.chatapp"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding =true
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.9.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //circular image
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")

    //Firebase Storage
    implementation("com.google.firebase:firebase-storage")

    //Lottie Animation
    implementation ("com.airbnb.android:lottie:6.4.0")

    //Glide library for loading online images into view
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    //Image View for zoom in and out
    implementation ("com.davemorrissey.labs:subsampling-scale-image-view:3.10.0")

    //for pdf viewer
    implementation ("com.github.barteksc:android-pdf-viewer:2.8.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    //Web rtc
    implementation ("com.google.code.gson:gson:2.11.0")
    implementation ("org.webrtc:google-webrtc:1.0.32006")

    implementation ("com.guolindev.permissionx:permissionx:1.7.1")

    implementation("com.google.firebase:firebase-messaging")

    //Media3
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")


    implementation ("com.github.HaarigerHarald:android-youtubeExtractor:v2.1.0")
}