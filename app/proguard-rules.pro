# Shahriar VPN — ProGuard rules

# Xray core (gomobile bindings) must not be obfuscated
-keep class libv2ray.** { *; }
-keep class go.** { *; }

# JSON models are built with org.json at runtime — nothing to strip
-dontwarn org.json.**
