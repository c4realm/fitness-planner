==#**Arcade Fitness Planner**==


> **Plan it. Track it. Do it.**
> **<img width="1536" height="1024" alt="ChatGPT Image May 26, 2026, 06_44_39 PM" src="https://github.com/user-attachments/assets/9f91d9a6-0243-41a4-90e5-eca837a0a9a1" />**

An Android fitness planning and tracking application developed as a final project for the Mobile Application Development course at **St. Mary's University**.

---

## 👥 Team
| Name | Role |
|---|---|
| Amar Abdulmejid | Lead Developer |
| Kaleab Dejene | UI/UX & Frontend |
| Kidus Kibrom | Backend & API |
| Yonas Ajanew | Database & Testing |

**Instructor:** Dawit Yetemgeta  
**Course:** Mobile Application Development  
**Department:** Computer Science

---

## 🏗️ Project Status

### Phase 1 — Onboarding ✅
| Screen | Status |
|---|---|
| Splash Screen | ✅ Done |
| Login Screen | ✅ Done |
| Registration Screen | ✅ Done |
| Dashboard Screen | ✅ Done |

### Phase 2 — Core Features 🔜
| Screen | Status |
|---|---|
| Workout Planner | 🔜 Next |
| Exercise Library | 🔜 |
| Workout Tracking | 🔜 |
| Progress Tracking | 🔜 |

### Phase 3 — Tools & Profile 🔜
| Screen | Status |
|---|---|
| Workout History | 🔜 |
| Goal Setting | 🔜 |
| BMI Calculator | 🔜 |
| User Profile | 🔜 |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (API 24+) |
| Language | Java |
| UI | XML Layouts + Material Components |
| Database | PostgreSQL (via REST API) |
| Networking | Retrofit2 + OkHttp3 |
| Auth | Google Sign-In + JWT |
| Build | Gradle |

---

## 📁 Project Structure

```
app/src/main/
├── java/com/arcadefitness/
│   ├── activities/          # All screen Activity classes
│   │   ├── SplashActivity.java
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   └── DashboardActivity.java
│   ├── adapters/            # RecyclerView adapters
│   ├── models/              # Data model POJOs
│   ├── network/             # API service interfaces
│   └── utils/               # Helpers, constants, SharedPrefs
│       ├── AppConstants.java
│       ├── SessionManager.java
│       └── ValidationUtils.java
└── res/
    ├── layout/              # XML screen layouts
    ├── drawable/            # Shapes, selectors, vectors
    ├── values/              # colors.xml, strings.xml, themes.xml, dimens.xml
    ├── font/                # Inter font family
    └── anim/                # Transition animations
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK API 24–34
- Git

### Steps
```bash
# 1. Clone the repo
git clone https://github.com/YOUR_USERNAME/ArcadeFitness.git

# 2. Open in Android Studio
#    File → Open → select the ArcadeFitness folder

# 3. Sync Gradle
#    Android Studio will prompt — click "Sync Now"

# 4. Run on emulator or physical device
#    Run → Run 'app'  (Shift+F10)
```

### Google Sign-In Setup
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a project → Enable **Google Sign-In API**
3. Create OAuth 2.0 credentials → download `google-services.json`
4. Place `google-services.json` in `app/` directory
5. Replace `YOUR_WEB_CLIENT_ID` in `AppConstants.java`

---

## 🎨 Design System

| Token | Value |
|---|---|
| Primary Orange | `#FF6B00` |
| Background | `#121212` |
| Card Surface | `#1A1A1A` |
| Input Surface | `#1C1C1C` |
| Border | `#252525` |
| Text Primary | `#FFFFFF` |
| Text Secondary | `#888888` |
| Font | Inter (400, 500, 700, 900) |

---

## 📝 License
Academic project — St. Mary's University © 2025
