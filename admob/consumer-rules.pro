# Consumer rules for admob module
-keep class io.github.saifullah.nurani.ads.admob.** { *; }
-keep interface io.github.saifullah.nurani.ads.admob.** { *; }

# Keep GAD classes if they are being stripped (though the SDK should have its own rules)
-keep class com.google.android.gms.ads.** { *; }
