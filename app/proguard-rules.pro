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

-keep class com.banglalink.toffee.ui.player.** { <fields>; }

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-keep class com.banglalink.toffee.ui.common** { *; }


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

-keepnames class com.google.android.exoplayer2.ext.cast.CastPlayer$StatusListener
-keepclassmembers class com.google.android.exoplayer2.ext.cast.CastPlayer {
    private com.google.android.exoplayer2.ext.cast.CastPlayer$StatusListener statusListener;
}

#-keep class com.google.android.exoplayer2** { *; }
#-keep class com.loopnow.fireworklibrary** { *; }
#-keep class com.loopnow.fireworkplayer** { *; }
#-keep class com.banglalink.toffee.ui.firework.FireworkFragment

-keep class com.google.ads.interactivemedia.** { *; }
-keep interface com.google.ads.interactivemedia.** { *; }

-keep class * extends java.util.ListResourceBundle { *; }

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.medallia.** { *; } 
-dontwarn com.medallia.**
-keep class com.conviva.** { *; }

-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**