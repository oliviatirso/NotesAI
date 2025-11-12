# How to Run NotesAI App

## Option 1: Using Android Studio (Recommended)

1. **Open Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to `/Users/oliviatirso/AndroidStudioProjects/NotesAI`
   - Click "OK"

2. **Sync Project**
   - Android Studio will automatically sync Gradle
   - Wait for the sync to complete (you'll see "Gradle sync finished" at the bottom)

3. **Set up Device/Emulator**
   - **Physical Device**: Connect your Android device via USB and enable USB debugging
   - **Emulator**: 
     - Click the "Device Manager" icon in the toolbar
     - Click "Create Device"
     - Select a device (e.g., Pixel 5)
     - Select a system image (API 21 or higher)
     - Click "Finish"

4. **Run the App**
   - Click the green "Run" button (▶️) in the toolbar
   - Or press `Shift + F10` (Windows/Linux) or `Cmd + R` (Mac)
   - Select your device/emulator
   - The app will install and launch automatically

## Option 2: Using Command Line

### Prerequisites
- Android SDK installed
- Java 19 or lower (Java 21 is not compatible with this Gradle/AGP version)
- ADB (Android Debug Bridge) installed

### Steps

1. **Set Java Version** (if using Java 21):
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 19)
   ```

2. **Connect Device or Start Emulator**:
   - Connect your Android device via USB
   - Or start an emulator from Android Studio

3. **Check Connected Devices**:
   ```bash
   adb devices
   ```
   You should see your device listed

4. **Build and Install**:
   ```bash
   cd /Users/oliviatirso/AndroidStudioProjects/NotesAI
   ./gradlew installDebug
   ```

5. **Launch the App**:
   ```bash
   adb shell am start -n com.example.notesai/.MainActivity
   ```

### Build APK Only
To build the APK without installing:
```bash
./gradlew assembleDebug
```
The APK will be in: `app/build/outputs/apk/debug/app-debug.apk`

## Troubleshooting

### Java Version Issues
If you get "Unsupported class file major version" errors:
- Use Java 19 or lower
- Set JAVA_HOME: `export JAVA_HOME=$(/usr/libexec/java_home -v 19)`
- Or use Android Studio (it comes with its own JDK)

### Gradle Sync Issues
- Click "File" → "Sync Project with Gradle Files"
- Or click the "Sync Now" link if it appears

### Build Errors
- Clean the project: `./gradlew clean`
- Rebuild: `./gradlew assembleDebug`

## Notes
- The app requires Android API 21 (Android 5.0) or higher
- Android Studio is the easiest way to run the app as it handles Java version automatically

