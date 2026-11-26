# Student Alarm (Android, Kotlin + Jetpack Compose)

Alarms for a student's day: wake up, class, homework, study sessions and exams.

## Features
- Alarms with a time, label and type (Wake Up, Class, Study Session, Homework, Exam, Other)
- Repeat on chosen weekdays, or once (one-time alarms switch themselves off after ringing)
- One-tap "school-day routine"
- On/off switch per alarm, delete button
- Uses the phone's alarm system, so alarms fire on time even in battery-saver/Doze mode,
  play on the alarm volume, and are restored after a reboot

## Build it on Windows
1. Install **Android Studio** (free): https://developer.android.com/studio
2. **New Project > Empty Activity** (the one with Jetpack Compose). Set:
   - Name: `StudentAlarm`
   - Package name: `com.example.studentalarm`  (must match exactly)
   - Minimum SDK: API 24 or higher
3. Wait for the first Gradle sync to finish.
4. Copy the 7 `.kt` files from this folder into
   `app/src/main/java/com/example/studentalarm/`
   (drag them onto the `studentalarm` folder in the Project panel).
   Say **Yes** to replacing `MainActivity.kt`.
5. Open `app/src/main/AndroidManifest.xml` and add the pieces from
   `AndroidManifest-additions.xml` (three permissions and two receivers).
6. Click **Sync Project** if Android Studio shows the banner.

## Put it on your Android phone
**Option A: cable**
1. On the phone: Settings > About phone > tap **Build number** 7 times.
2. Settings > Developer options > turn on **USB debugging**.
3. Plug the phone into the PC and tap **Allow** on the phone.
4. Pick your phone in Android Studio's device drop-down and press **Run** (green triangle).

**Option B: APK file**
1. Build > Build Bundle(s) / APK(s) > **Build APK(s)**.
2. Click "locate" in the popup, copy `app-debug.apk` to the phone (WhatsApp, Drive or USB).
3. Open it on the phone and allow "Install unknown apps" when asked.

## First run
- Tap **Allow** when asked to send notifications, or the alarms will be silent.
- On Xiaomi, Oppo, Vivo, OnePlus and Samsung phones, also set Settings > Apps > Student Alarm >
  Battery to **No restrictions** (and turn on **Autostart** if shown). Otherwise the phone may
  stop the app from ringing.

## Notes
- The ring is a high-priority notification using the alarm sound; it keeps sounding until you
  tap or swipe it away. Behaviour can vary a little between phone brands.
- It does not show a full-screen "wake-up" screen over the lock screen. That can be added.
