# Taho fork: rules every app that includes this library needs. Games link against the MIDP and vendor classes by
# name at run time (their dex is loaded separately), so none of them may be removed or renamed.
-keep class javax.** { *; }
-keep class com.kddi.** { *; }
-keep class com.siemens.mp.** { *; }
-keep class com.samsung.util.** { *; }
-keep class com.sonyericsson.accelerometer.** { *; }
-keep class com.sprintpcs.media.** { *; }
-keep class com.mascotcapsule.micro3d.v3.** { *; }
-keep class com.mexa.** { *; }
-keep class com.j_phone.** { *; }
-keep class com.jblend.** { *; }
-keep class com.motorola.** { *; }
-keep class com.nokia.mid.** { *; }
-keep class com.sun.midp.midlet.** { *; }
-keep class com.vodafone.** { *; }
-keep class mmpp.media.** { *; }
-keep class org.microemu.** { *; }

# Our own runtime: JNI entry points, reflection, and the profile that Gson reads and writes.
-keep class ru.woesss.j2me.** { *; }
-keep class ru.playsoftware.j2meloader.** { *; }

# The converter (dexlib) is driven through its main method and reads its own resources by name.
-keep class com.android.dx.** { *; }

# Gson, for R8 full mode
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
