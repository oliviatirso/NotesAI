# HOW TO GET THE ERROR MESSAGE

## Steps (Important!):

1. **Open Logcat:**
   - Look at the BOTTOM of Android Studio
   - Find the tab that says "Logcat"
   - If you don't see it: View → Tool Windows → Logcat

2. **Clear the logs:**
   - Click the trash can icon to clear old logs

3. **Run the app:**
   - Click the Run button
   - Watch Logcat IMMEDIATELY as the app tries to launch

4. **Find the crash:**
   - Look for RED text
   - Find this line: `FATAL EXCEPTION: main`
   - Copy EVERYTHING from that line down

5. **What to copy:**
   - Everything that's RED
   - Lines starting with `at com.example.notesai`
   - The "Caused by:" line
   - Any stack trace

## Example of what to look for:
```
FATAL EXCEPTION: main
Process: com.example.notesai
java.lang.RuntimeException: ...
    at com.example.notesai.MainActivity.onCreate(...)
    ...
Caused by: ...
```

## Please paste the full error here!

