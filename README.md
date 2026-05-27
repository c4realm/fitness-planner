# 🏋️ Arcade Fitness Planner

<div align="center">

### **Plan it. Track it. Do it.**

<img width="700" alt="Arcade Fitness Logo" src="https://github.com/user-attachments/assets/9f91d9a6-0243-41a4-90e5-eca837a0a9a1" />

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge)
![Language](https://img.shields.io/badge/Language-Java-orange?style=for-the-badge)
![UI](https://img.shields.io/badge/UI-Material%20Design-ff6b00?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-In%20Development-blue?style=for-the-badge)
![University](https://img.shields.io/badge/St.%20Mary's-University-red?style=for-the-badge)

</div>

---

## 📖 About The Project

**Arcade Fitness Planner** is an Android fitness planning and workout tracking application developed as a final project for the **Mobile Application Development** course at **St. Mary's University**.

The application is designed to help users:

* 🏋️ Plan workouts efficiently
* 📊 Track workout progress
* 🎯 Set personal fitness goals
* 📅 Manage fitness schedules
* 📈 Monitor overall improvement

---

# 👥 Team

| Name                | Role               |
| ------------------- | ------------------ |
| **Amar Abdulmejid** | Lead Developer     |
| **Kaleab Dejene**   | UI/UX & Frontend   |
| **Kidus Kibrom**    | Backend & API      |
| **Yonas Ajanew**    | Database & Testing |

---

## 🎓 Academic Information

| Field           | Details                        |
| --------------- | ------------------------------ |
| **Instructor**  | Dawit Yetemgeta                |
| **Course**      | Mobile Application Development |
| **Department**  | Computer Science               |
| **Institution** | St. Mary's University          |

---

# 🏗️ Development Roadmap

## ✅ Phase 1 — Onboarding

| Screen              | Status      |
| ------------------- | ----------- |
| Splash Screen       | ✅ Completed |
| Login Screen        | ✅ Completed |
| Registration Screen | ✅ Completed |
| Dashboard Screen    | ✅ Completed |

---

## 🔜 Phase 2 — Core Features

| Feature           | Status     |
| ----------------- | ---------- |
| Workout Planner   | 🔜 Next    |
| Exercise Library  | 🔜 Pending |
| Workout Tracking  | 🔜 Pending |
| Progress Tracking | 🔜 Pending |

---

## 🔜 Phase 3 — Tools & Profile

| Feature         | Status     |
| --------------- | ---------- |
| Workout History | 🔜 Pending |
| Goal Setting    | 🔜 Pending |
| BMI Calculator  | 🔜 Pending |
| User Profile    | 🔜 Pending |

---

# 🛠️ Tech Stack

| Category           | Technology                        |
| ------------------ | --------------------------------- |
| **Platform**       | Android (API 24+)                 |
| **Language**       | Java                              |
| **UI Framework**   | XML Layouts + Material Components |
| **Database**       | PostgreSQL (via REST API)         |
| **Networking**     | Retrofit2 + OkHttp3               |
| **Authentication** | Google Sign-In + JWT              |
| **Build System**   | Gradle                            |

---

# 📂 Project Structure

```bash
app/src/main/
├── java/com/arcadefitness/
│   ├── activities/          # All screen Activity classes
│   │   ├── SplashActivity.java
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   └── DashboardActivity.java
│   │
│   ├── adapters/            # RecyclerView adapters
│   ├── models/              # Data model POJOs
│   ├── network/             # API service interfaces
│   └── utils/               # Helpers, constants, SharedPrefs
│       ├── AppConstants.java
│       ├── SessionManager.java
│       └── ValidationUtils.java
│
└── res/
    ├── layout/              # XML screen layouts
    ├── drawable/            # Shapes, selectors, vectors
    ├── values/              # colors.xml, strings.xml, themes.xml, dimens.xml
    ├── font/                # Inter font family
    └── anim/                # Transition animations
```

---

# ⚙️ Setup & Installation

## 📋 Prerequisites

Before running the project, make sure you have:

* Android Studio Hedgehog (2023.1.1+)
* JDK 17+
* Android SDK API 24–34
* Git

---

## 🚀 Installation Steps

```bash
# 1️⃣ Clone the repository
git clone https://github.com/YOUR_USERNAME/ArcadeFitness.git

# 2️⃣ Open the project in Android Studio
# File → Open → Select the ArcadeFitness folder

# 3️⃣ Sync Gradle dependencies
# Android Studio will prompt → Click "Sync Now"

# 4️⃣ Run the application
# Run → Run 'app'  (Shift + F10)
```

---

# 🔐 Google Sign-In Setup

1. Go to **Google Cloud Console**
2. Create a new project
3. Enable **Google Sign-In API**
4. Create OAuth 2.0 credentials
5. Download `google-services.json`
6. Place the file inside the `app/` directory
7. Replace `YOUR_WEB_CLIENT_ID` inside `AppConstants.java`

---

# 🎨 Design System

| Design Token       | Value                      |
| ------------------ | -------------------------- |
| **Primary Orange** | `#FF6B00`                  |
| **Background**     | `#121212`                  |
| **Card Surface**   | `#1A1A1A`                  |
| **Input Surface**  | `#1C1C1C`                  |
| **Border Color**   | `#252525`                  |
| **Text Primary**   | `#FFFFFF`                  |
| **Text Secondary** | `#888888`                  |
| **Typography**     | Inter (400, 500, 700, 900) |

---

# 📌 Features Overview

* ✅ Modern Material UI
* ✅ Authentication System
* ✅ Workout Planning
* ✅ Exercise Tracking
* ✅ Progress Monitoring
* ✅ Google Sign-In Integration
* ✅ REST API Communication
* ✅ Responsive Android Layouts

---

# 📜 License

Academic Project — **St. Mary's University © 2025**

---

<div align="center">

### ⭐ Built with Java & Android Studio

**Arcade Fitness Planner Team**

</div>
