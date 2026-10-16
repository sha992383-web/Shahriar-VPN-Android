<div dir="rtl">

# 🏗️ راهنمای بیلد (Build)

## پیش‌نیازها

| ابزار | نسخه |
|---|---|
| JDK | 17 (Temurin توصیه می‌شود) |
| Gradle | 8.9+ |
| Android SDK | compileSdk 34 |
| Android Studio | Hedgehog به بالا (اختیاری اما توصیه‌شده) |

## نکته مهم — کتابخانه هسته (libv2ray.aar)

فایل `libv2ray.aar` (~65MB) **در گیت ذخیره نمی‌شود**. تسک Gradle به نام `downloadXrayAar` آن را **به‌صورت خودکار** از ریلیزهای [AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite/releases) دانلود می‌کند و در `app/libs/` می‌گذارد.

اگر می‌خواهی دستی دانلود کنی:

```bash
mkdir -p app/libs
curl -L -o app/libs/libv2ray.aar \
  https://github.com/2dust/AndroidLibXrayLite/releases/latest/download/libv2ray.aar
```

## بیلد با Android Studio (پیشنهادی)

1. `File ▸ Open` و پوشه پروژه را باز کن
2. منتظر Sync بمان (اولین بار چند دقیقه طول می‌کشد — دانلود AAR هم اینجا انجام می‌شود)
3. `Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)`
4. APK خروجی: `app/build/outputs/apk/debug/app-debug.apk`

## بیلد با خط فرمان (Linux/macOS)

```bash
# اگر gradle نصب داری:
gradle assembleDebug --no-daemon

# یا با wrapper بعد از ساختش با Android Studio:
./gradlew assembleDebug
```

## بیلد با خط فرمان (Windows)

```powershell
gradle.bat assembleDebug --no-daemon
```

(دانلود خودکار AAR در ویندوز با PowerShell انجام می‌شود — خودکار است.)

## تست‌های واحد

```bash
gradle testDebugUnitTest
```

تست‌ها منطق پارسر لینک‌ها (vmess/vless/trojan/ss/subscription) را پوشش می‌دهند و به SDK اندروید وابسته نیستند.

## CI خودکار

فایل `.github/workflows/build.yml` روی هر push به main/master:

1. JDK 17 را ست می‌کند
2. Gradle 8.9 را نصب می‌کند
3. تست‌های واحد را اجرا می‌کند
4. `assembleDebug` را می‌سازد (AAR خودکار دانلود می‌شود)
5. APK را به‌عنوان آرتیفکت آپلود می‌کند: `Shahriar-VPN-debug-apk`

بیلد دستی: تب Actions → Build Shahriar APK → Run workflow

## نسخه Release

برای بیلد release، امضای خودت را بساز:

```bash
keytool -genkey -v -keystore shahriar.jks -keyalg RSA -keysize 2048 -validity 10000 -alias shahriar
```

و در `app/build.gradle.kts` بلاک `signingConfigs` را اضافه کن.

</div>
