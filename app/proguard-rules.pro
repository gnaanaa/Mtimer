# Hilt rules
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.internal.lifecycle.HiltViewModelMap *
-keep,allowobfuscation,allowshrinking @dagger.hilt.internal.generatesrootinput.InjectedFieldSignature *

# Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Health Connect rules
-keep class androidx.health.connect.client.records.** { *; }
-keep class androidx.health.connect.client.units.** { *; }

# Kotlin Serialization rules
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Google Drive API rules
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.googleapis.extensions.android.**

# Gson rules
-keepattributes *Annotation*, Signature, EnclosingMethod
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Data Models (for Gson serialization)
-keep class com.gnaanaa.mtimer.data.sync.BackupData { *; }
-keep class com.gnaanaa.mtimer.data.db.PresetEntity { *; }
-keep class com.gnaanaa.mtimer.data.db.SessionEntity { *; }

# Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepnames class retrofit2.KotlinExtensions$suspendImpl$1 {}
-keepnames class retrofit2.KotlinExtensions {}

-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$ScheduledAtFixedRatePost {
    void run();
}

-dontwarn kotlinx.coroutines.**
