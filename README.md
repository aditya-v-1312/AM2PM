# AM2PM ☀️🌑
> "Focus on what matters."

AM2PM is a minimalist, offline-first productivity app for Android 14+. Built with Java and a custom minimalist design system.

## 📱 Features
* **Missions:** Add tasks with Descriptions, Categories, and Due Dates.
* **Smart Reminders:** Automated local notifications for due tasks.
* **Home Screen Widget:** Instant view of your task list without opening the app.
* **Auto-Cleanup:** Completed tasks vanish automatically after a satisfying check animation.
* **Privacy First:** 100% Offline Database (Room/SQLite).

## 🛠️ Tech Stack
* **Language:** Java
* **UI:** XML Layouts (Custom Gradient Themes)
* **Database:** Room Persistence Library
* **Background:** AlarmManager & BroadcastReceivers
* **CI/CD:** GitHub Actions (Auto-builds APKs on push)

## 🚀 How to Build
1. Clone the repo.
2. Open in VS Code or Android Studio.
3. Run `./gradlew assembleDebug`.