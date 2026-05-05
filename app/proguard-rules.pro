# ProGuard rules for MediaPipe and CameraX

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-keep interface com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# CameraX
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }

# Kotlin
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembernames class ** {
    native <methods>;
}

# General
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
