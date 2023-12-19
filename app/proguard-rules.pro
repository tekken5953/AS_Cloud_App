-keepattributes SourceFile,LineNumberTable

-keep class com.google.gson.** { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }
-keep class app.airsignal.weather.dao.IgnoredKeyFile {*;}
