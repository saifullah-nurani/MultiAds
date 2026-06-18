# Consumer rules for applovin module
-keep class io.github.saifullah.nurani.ads.applovin.** { *; }
-keep interface io.github.saifullah.nurani.ads.applovin.** { *; }

# Keep AppLovin SDK classes
-keep class com.applovin.** { *; }
-dontwarn com.applovin.**
