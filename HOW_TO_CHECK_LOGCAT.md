# How to Check Logcat for Crash Errors

## Quick Steps:

1. **Open Logcat:**
   - Look at the bottom of Android Studio
   - Click the "Logcat" tab
   - If you don't see it: View → Tool Windows → Logcat

2. **Clear and Filter:**
   - Click the "Clear logcat" button (trash icon) to clear old logs
   - In the search box, type: `com.example.notesai` or `FATAL`

3. **Run the App:**
   - Click Run button
   - Watch Logcat for red error messages

4. **Find the Crash:**
   Look for:
   ```
   FATAL EXCEPTION: main
   Process: com.example.notesai
   ```

5. **Copy the Error:**
   - Select all the red error text
   - Copy it and share with me

## Common Error Patterns:

**Resource Not Found:**
```
android.content.res.Resources$NotFoundException: Resource ID #0x...
```

**Null Pointer:**
```
java.lang.NullPointerException: Attempt to invoke virtual method...
```

**Inflate Error:**
```
android.view.InflateException: Binary XML file line #...
```

