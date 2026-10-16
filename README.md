<div dir="rtl">

# ⚡ فیلترشکن اندروید شهریار

```
  ███████╗██╗  ██╗ █████╗ ██╗  ██╗██████╗ ██╗ █████╗ ██████╗
  ██╔════╝██║  ██║██╔══██╗██║  ██║██╔══██╗██║██╔══██╗██╔══██╗
  ███████╗███████║███████║███████║██████╔╝██║███████║██████╔╝
  ╚════██║██╔══██║██╔══██║██╔══██║██╔══██╗██║██╔══██║██╔══██╗
  ███████║██║  ██║██║  ██║██║  ██║██║  ██║██║██║  ██║██║  ██║
  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚═╝  ╚═╝
        ⚡ CYBERPUNK VPN CLIENT — POWERED BY XRAY-CORE ⚡
```

<p align="center">
  <a href="#-features"><img alt="Platform" src="https://img.shields.io/badge/platform-Android%207.0%2B-00F0FF?style=for-the-badge&logo=android&logoColor=00F0FF"></a>
  <a href="#-build"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.9-FF2A6D?style=for-the-badge&logo=kotlin&logoColor=FF2A6D"></a>
  <a href="#-build"><img alt="Compose" src="https://img.shields.io/badge/Jetpack%20Compose-UI-B537F2?style=for-the-badge&logo=jetpackcompose&logoColor=B537F2"></a>
  <img alt="License" src="https://img.shields.io/badge/License-GPL--3.0-05FFA1?style=for-the-badge&logo=gnu&logoColor=05FFA1">
  <img alt="Core" src="https://img.shields.io/badge/Xray--Core-latest-FFB703?style=for-the-badge&logo=x&logoColor=FFB703">
</p>

> 🌆 یک کلاینت VPN اندرویدی با ظاهر **سایبرپانکی**، مبتنی بر قدرتمندترین هسته متن‌باز دنیا (Xray)، ساخته‌شده با Kotlin و Jetpack Compose — با دکمه پاور نئونی که با یک لمس، یوتیوب و کل اینترنت آزاد رو برات باز می‌کنه.

---

## ✨ امکانات

| قابلیت | توضیح |
|---|---|
| 🎨 **ظاهر سایبرپانکی** | پالت نئونی (فیروزه‌ای/سرخابی/سبز)، افکت Glitch روی تیتر، اسکن‌لاین CRT، دکمه پاور متحرک |
| 🛡️ **هسته Xray** | آخرین نسخه هسته به‌صورت خودکار دانلود و اجرا می‌شود |
| 🔌 **پروتکل‌ها** | `VLESS + Reality` ✅ `VMess` ✅ `Trojan` ✅ `Shadowsocks` ✅ |
| 📥 **ورود کانفیگ** | چسباندن لینک تکی (`vless://…`) یا **اشتراک کامل** (base64) |
| 📺 **دکمه یوتیوب** | با یک لمس بعد از اتصال، یوتیوب باز می‌شود |
| 📊 **آمار زنده** | نمایش لحظه‌ای آپلود/دانلود از خود هسته |
| 🖥️ **ترمینال لاگ** | مشاهده رویدادهای هسته به سبک ترمینال سایبرپانکی |
| 🚫 **مسدودسازی QUIC** | به‌صورت پیش‌فرض QUIC بلاک می‌شود تا اتصال پایدارتر باشد (UDP:443) |
| 🔒 **حریم خصوصی** | ترافیک خود برنامه از تونل مستثنی است؛ رنج‌های خصوصی مستقیم می‌روند |
| ⚙️ **CI خودکار** | هر push → بیلد APK آماده نصب در بخش Actions |

---

## 📱 نصب APK (سریع‌ترین راه)

**۱.** وارد تب [**Actions**](../../actions) این ریپو شو

**۲.** آخرین بیلد سبز (✅ Build Shahriar APK) را باز کن

**۳.** از پایین صفحه، آرتیفکت **`Shahriar-VPN-debug-apk`** را دانلود کن

**۴.** فایل را روی گوشی منتقل و نصب کن (اجازه «منابع ناشناس» را بده)

> 💡 بیلد سبز یعنی تست‌ها پاس شده و APK سالم است.

---

## 🚀 استفاده

1. کانفیگ (لینک `vless://` یا …) را از ادمین سرور/باتل بگیر
2. برنامه را باز کن → تب **کانفیگ‌ها** → دکمه **+** → لینک را paste کن
3. روی کانفیگ بزن تا **سبز** شود (انتخاب شد)
4. برگرد تب **اتصال** → دکمه پاور نئونی را بزن → صبر کن تا **وصل** شود
5. دکمه **یوتیوب** را بزن و لذت ببر 🎬

راهنمای کامل و عیب‌یابی: [`docs/USAGE.md`](docs/USAGE.md) • سؤالات متداول: [`docs/FAQ.md`](docs/FAQ.md)

---

## 🏗️ بیلد از سورس

### با Android Studio
1. ریپو را clone کن و در Android Studio باز کن
2. همگام‌سازی Gradle انجام می‌شود؛ فایل `libv2ray.aar` **به‌صورت خودکار** دانلود می‌شود (≈65MB)
3. `Build ▸ Build Bundle(s) / APK(s) ▸ Build APK(s)`

### با خط فرمان
```bash
git clone https://github.com/<YOUR-USERNAME>/Shahriar-VPN-Android.git
cd Shahriar-VPN-Android
gradle assembleDebug          # نیاز به Gradle 8.9+ و JDK 17
```

جزئیات بیشتر: [`docs/BUILD.md`](docs/BUILD.md)

---

## 🧬 معماری

```
┌──────────────────────────────────────────────────┐
│                  📱 SHAHRIAR UI                  │
│   Jetpack Compose • Neon Glitch • CRT Scanlines  │
├──────────────────────────────────────────────────┤
│  LinkParser → ProfileStore → XrayConfigBuilder   │
│  (vmess/vless/trojan/ss)   (SharedPreferences)   │
├──────────────────────────────────────────────────┤
│           ShahriarVpnService (VpnService)        │
│   tun: 26.26.26.1/30 • MTU 8500 • route 0.0.0.0  │
├──────────────────────────────────────────────────┤
│        libv2ray.aar (AndroidLibXrayLite)         │
│   CoreController.startLoop(config, tunFd)        │
├──────────────────────────────────────────────────┤
│              ⚡ XRAY-CORE (Go) ⚡                  │
│   inbounds: tun + socks:10808                    │
│   outbounds: proxy / direct / blackhole          │
└──────────────────────────────────────────────────┘
```

---

## 🗂️ ساختار پروژه

```
Shahriar-VPN-Android/
├── app/src/main/java/com/shahriar/vpn/
│   ├── MainActivity.kt              # ورودی برنامه
│   ├── core/
│   │   ├── LinkParser.kt            # پارسر لینک‌های اشتراکی
│   │   ├── XrayConfigBuilder.kt     # سازنده JSON کانفیگ هسته
│   │   ├── VpnManager.kt            # پوش libv2ray (شروع/توقف/پینگ)
│   │   └── VpnEventBus.kt           # حالت‌ها، لاگ‌ها، آمار
│   ├── data/ProfileStore.kt         # ذخیره کانفیگ‌ها
│   ├── service/ShahriarVpnService.kt# سرویس Android VPN
│   ├── ui/                          # تم نئون، کامپوننت‌ها، ۴ صفحه
│   └── util/Constants.kt
├── app/src/test/                    # تست‌های واحد پارسر
├── .github/workflows/build.yml      # CI بیلد APK
└── docs/                            # راهنمای استفاده/بیلد/FAQ
```

---

## ❓ سؤالات متداول

**⚠️ آیا استفاده از این برنامه قانونی است؟**
دسترسی آزاد به اطلاعات حق همه است؛ اما مسئولیت استفاده بر عهده خود شماست. قوانین محلی را در نظر بگیرید.

**🔐 آیا اطلاعات من لاگ می‌شود؟**
هیچ سروری برای این برنامه وجود ندارد؛ همه‌چیز بین گوشی شما و سرور شخصی خودتان است. لاگ‌ها فقط محلی و در حافظه برنامه‌اند.

**📺 یوتیوب باز نمی‌شود؟**
اول وصل شو، بعد تست پینگ بگیر. اگر پینگ ناموفق بود کانفیگ/سرور را عوض کن. QUIC به‌صورت پیش‌فرض بلاک است و اپلیکیشن‌ها به TCP برمی‌گردند — این طبیعی و مطلوب است.

**🆚 تفاوت با v2rayNG چیه؟**
شهریار یک کلاینت مینیمال و سبک با تمرکز بر تجربه سایبرپانکی و راه‌اندازی سه‌مرحله‌ای است؛ برای فیچرهای پیشرفته (زیرشبکه، روتینگ کامل، per-app) می‌توانید از کلاینت‌های بزرگ‌تر استفاده کنید.

بقیه: [`docs/FAQ.md`](docs/FAQ.md)

---

## 🙏 قدردانی

- [Xray-core](https://github.com/XTLS/Xray-core) — قلب تپنده پروژه
- [AndroidLibXrayLite](https://github.com/2dust/AndroidLibXrayLite) — بایندینگ اندروید هسته
- [v2rayNG](https://github.com/2dust/v2rayNG) — مرجع الگوهای فنی VPN در اندروید
- [Jetpack Compose](https://developer.android.com/compose) — UI مدرن اندروید

## 📄 مجوز

[GPL-3.0](LICENSE) © ۲۰۲۶ — با ❤ و نئون ساخته شده

</div>

---

<div align="center">

**English TL;DR:** Shahriar VPN is an open-source, cyberpunk-styled Android VPN client powered by the Xray-core library. It supports VLESS (Reality), VMess, Trojan and Shadowsocks, imports share-links/subscriptions, and ships a GitHub Actions pipeline that builds an installable debug APK on every push. Licensed under GPL-3.0 — use responsibly.

</div>
