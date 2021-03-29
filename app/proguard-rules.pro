# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#********************************Start rules for gson****************************************#
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.banglalink.toffee.data.network.request.** { <fields>; }
-keep class com.banglalink.toffee.data.database.entities.** { <fields>; }
-keep class com.banglalink.toffee.data.network.response.** { <fields>; }
-keep class com.banglalink.toffee.model.** { <fields>; }
-keep class com.banglalink.toffee.ui.player.** { <fields>; }

-keep class com.google.api.services.pubsub.** { <fields>; }
-keep class com.google.api.client.** { <fields>; }
-keep class com.google.api.client.googleapis.** { <fields>; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}


-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep class com.banglalink.toffee.ui.common** { *; }

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
#********************************End rules for gson****************************************#

-keepnames public class * extends androidx.fragment.app.Fragment
#-keepnames public class * extends com.google.android.material.appbar.AppBarLayout.*
#-keep class com.banglalink.toffee.ui.widget.AppBarLayoutBehavior {
#    public <methods>;
#}
-keepnames abstract class com.google.android.material.appbar.HeaderBehavior
-keepclassmembers class com.google.android.material.appbar.HeaderBehavior {
    private java.lang.Runnable flingRunnable;
    android.widget.OverScroller scroller;
}
-keep class androidx.navigation** { *; }

-keepclassmembers class com.loopnow.fireworklibrary.** { <fields>; }