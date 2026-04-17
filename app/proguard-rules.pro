# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.registeroffline.data.remote.dto.** { *; }
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-dontwarn dagger.hilt.**

# Compressor
-dontwarn id.zelory.compressor.**
