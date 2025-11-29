# How to Find the Crash Error

## Step 1: Open Logcat in Android Studio
1. Look at the bottom panel in Android Studio
2. Click on the "Logcat" tab
3. If you don't see it, go to: View → Tool Windows → Logcat

## Step 2: Filter for Errors
1. In the Logcat search/filter box, type: `FATAL` or `ERROR`
2. Look for red error messages

## Step 3: Look for the Crash
Look for lines that say:
- `FATAL EXCEPTION: main`
- `Process: com.example.notesai`
- `java.lang.RuntimeException`
- `Caused by:`

## Step 4: Copy the Error
Copy the entire error message (especially the stack trace) starting from "FATAL EXCEPTION" down to the "at com.example.notesai..." lines.

## Common Crash Causes to Check:

1. **Missing Resource Error**
   - Look for: `android.content.res.Resources$NotFoundException`
   - This means a drawable, string, or layout file is missing

2. **Null Pointer Exception**
   - Look for: `java.lang.NullPointerException`
   - This means something is null when it shouldn't be

3. **Class Not Found**
   - Look for: `java.lang.ClassNotFoundException`
   - This means an activity or class is missing

4. **Inflate Exception**
   - Look for: `android.view.InflateException`
   - This means there's an error in an XML layout file

## What to Share:
Please share:
1. The full error message from Logcat
2. The line that says "Caused by:"
3. Any lines starting with "at com.example.notesai"

