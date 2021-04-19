# SelfieLib

SelfieLib is an Android library that automates selfie-taking without a button. Place your head in
shown area, keep your eyes open and smile.

### Add to Your App
- Clone the repository.
- Build the project. This will create an `aar` file. 
``` 
./gradlew assemble
```
- Go to `SelfieLib -> app -> build -> outputs -> aar` and locate `selfieLib-1.0-release.aar`.
- Then open your app in Android Studio and go to: `File -> New -> New Module`.
- Select `Import .JAR/.AAR Package` and navigate to `selfieLib-1.0-release.aar`.
- Click Finish.
- Make sure you have `include ':SelfieLib'` at the top of your `settings.gradle` file.

### Add Dependencies
```
    implementation project(":SelfieLib")

    // ViewModel
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.activity:activity-ktx:$activity_version"
    implementation "androidx.fragment:fragment-ktx:$fragment_version"
    // LiveData
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"

    //CameraX Dependencies
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-camera2:${camerax_version}"
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    implementation "androidx.camera:camera-view:$cameraXAlphaVersion"

    // Firebase
    implementation "com.google.mlkit:face-detection:$fbFaceDetectionVersion"

    // Lottie
    implementation "com.airbnb.android:lottie:$lottieVersion"

```
where; 
```
ext {
        camerax_version = "1.0.0-rc04"
        cameraXAlphaVersion = "1.0.0-alpha23"
        fbFaceDetectionVersion = "16.0.6"
        lifecycle_version = "2.3.1"
        activity_version = "1.2.2"
        fragment_version = "1.3.2"
        lottieVersion = "3.7.0"
    }
```

### Add the Code
- In your activity/fragment layout, add: 
```
<com.nerdz.selfielib.SelfieView
    android:id="@+id/selfie_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

- In your activity/fragment
```
selfieView = view.findViewById(R.id.selfie_view)
selfieView.start { result ->
// use result.mediaBitmap as selfie taken
}
```

- That's it :)

## Sample App
Check the [sample](https://github.com/orcunozyurt/SelfieApp)

## Stack
- Library was built with Kotlin.
- Uses [Jetpack CameraX](https://developer.android.com/training/camerax) as camera library
- Uses [ML Kit](https://developers.google.com/ml-kit/vision/face-detection/android) for face detection purposes

## Architecture
- Follows MVVM Pattern

## Tests
Run tests from Android Studio or `./gradlew clean test`
