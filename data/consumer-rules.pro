
#********************************Start rules for gson****************************************#

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
#-keepattributes Signature

# For using GSON @Expose annotation
#-keepattributes *Annotation*

# Gson specific classes
#-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
#-keep class * implements com.google.gson.TypeAdapter
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keep class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
#-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
#********************************End rules for gson****************************************#

-dontwarn org.apache.commons.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

#-keep class okhttp3.** { *; }
#-dontwarn okhttp3.**
#-dontwarn okio.**

#-keep class retrofit2.** { *; }
#-dontwarn retrofit2.**

# Pubsub & Google apis
-keep class com.google.api.services.pubsub.** { <fields>; }
-keep class com.google.api.client.** { <fields>; }
-keep class com.google.api.client.googleapis.** { <fields>; }

#-keepclassmembers class * extends java.lang.Enum {
#    <fields>;
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}

#-keep class com.google.obf.** { *; }
#-keep interface com.google.obf.** { *; }