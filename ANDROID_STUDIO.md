# Setting up the USB Keyboard Project in Android Studio

## Prerequisites

- Android Studio (latest version recommended)
- Git (for cloning the repository)
- JDK 8 or higher

## Project Setup Instructions

1. **Create Project Directory Structure**
   ```bash
   mkdir UsbKeyboard
   cd UsbKeyboard
   ```

2. **Create Gradle Configuration Files**

   Create the following files with their respective contents:

   - `build.gradle` (root level):
   ```groovy
   buildscript {
       ext {
           kotlin_version = '1.8.0'
           compose_version = '1.4.0'
       }
       repositories {
           google()
           mavenCentral()
       }
       dependencies {
           classpath 'com.android.tools.build:gradle:7.4.2'
           classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
       }
   }

   allprojects {
       repositories {
           google()
           mavenCentral()
       }
   }

   task clean(type: Delete) {
       delete rootProject.buildDir
   }
   ```

   - `app/build.gradle`:
   ```groovy
   plugins {
       id 'com.android.application'
       id 'kotlin-android'
   }

   android {
       namespace 'com.example.usbkeyboard'
       compileSdk 33

       defaultConfig {
           applicationId "com.example.usbkeyboard"
           minSdk 24
           targetSdk 33
           versionCode 1
           versionName "1.0"

           testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
       }

       buildTypes {
           release {
               minifyEnabled false
               proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
           }
       }
       
       compileOptions {
           sourceCompatibility JavaVersion.VERSION_1_8
           targetCompatibility JavaVersion.VERSION_1_8
       }
       
       kotlinOptions {
           jvmTarget = '1.8'
       }
   }

   dependencies {
       implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
       implementation 'androidx.core:core-ktx:1.9.0'
       implementation 'androidx.appcompat:appcompat:1.6.1'
       implementation 'com.google.android.material:material:1.8.0'
       implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
       testImplementation 'junit:junit:4.13.2'
       androidTestImplementation 'androidx.test.ext:junit:1.1.5'
       androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
   }
   ```

   - `settings.gradle`:
   ```groovy
   include ':app'
   rootProject.name = "USB Keyboard"
   ```

   - `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
   android.useAndroidX=true
   kotlin.code.style=official
   android.nonTransitiveRClass=true
   ```

   - `gradle/wrapper/gradle-wrapper.properties`:
   ```properties
   distributionBase=GRADLE_USER_HOME
   distributionPath=wrapper/dists
   distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-bin.zip
   zipStoreBase=GRADLE_USER_HOME
   zipStorePath=wrapper/dists
   ```

3. **Create Project Structure**
   ```bash
   mkdir -p app/src/main/java/com/example/usbkeyboard
   mkdir -p app/src/main/res/{layout,drawable,values,xml}
   ```

4. **Copy Source Files**
   - Copy all `.kt` files to `app/src/main/java/com/example/usbkeyboard/`
   - Copy all layout files to `app/src/main/res/layout/`
   - Copy all drawable files to `app/src/main/res/drawable/`
   - Copy all values files to `app/src/main/res/values/`
   - Copy device_filter.xml to `app/src/main/res/xml/`
   - Copy AndroidManifest.xml to `app/src/main/`

5. **Initialize Gradle Wrapper**
   ```bash
   gradle wrapper
   ```

6. **Open in Android Studio**
   1. Open Android Studio
   2. Select "Open an existing Android Studio project"
   3. Navigate to and select the UsbKeyboard directory
   4. Wait for the project to sync and index

7. **Build the Project**
   - Click "Build > Make Project" or press Ctrl+F9 (Cmd+F9 on Mac)
   - If there are any dependency issues, click "File > Sync Project with Gradle Files"

## Troubleshooting

1. **Gradle Sync Failed**
   - Check that all configuration files are properly created
   - Verify that the Android SDK is properly installed
   - Try "File > Invalidate Caches / Restart"

2. **Missing Dependencies**
   - Make sure you have the latest Android SDK tools installed
   - Verify that you have the correct Kotlin plugin version

3. **Build Errors**
   - Check that all source files are in the correct packages
   - Verify that the AndroidManifest.xml is properly configured
   - Ensure all resource files are properly named and located

## Running the App

1. Connect an Android device with USB debugging enabled
2. Select your device in the toolbar's target device menu
3. Click the "Run" button or press Shift+F10 (Ctrl+R on Mac)

## Additional Notes

- The minimum supported Android version is 7.0 (API level 24)
- Make sure your device supports USB Host mode
- USB debugging must be enabled on the device
- The app requires USB host permissions to function

For more details about the app's functionality, refer to the USER_GUIDE.md and TECHNICAL.md documents. 