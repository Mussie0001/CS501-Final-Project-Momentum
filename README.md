# Momentum: Habit Tracker App

**Momentum** is a productivity-focused Android application that helps users build and maintain healthy habits through consistent daily tracking. Whether it's exercising, reading, drinking enough water, or studying, Momentum supports users in forming lasting routines with motivation and ease.

## 📱 Core Features

- **Habit Creation & Tracking**: Log daily progress for key habits.
- **Progress Visualization**: View historical data and habit-building trends.
- **Motivational Quotes**: Get daily inspiration via the ZenQuotes API.
- **Step Tracking**: Auto-log steps using the device's Step Counter Sensor.
- **Multi-Device Syncing**: Compatible with Android smartphones and Wear OS smartwatches.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Database**: Room Database for local data persistence
- **API**: [ZenQuotes](https://zenquotes.io/) for motivational quotes
- **Sensor Integration**: Android's `TYPE_STEP_COUNTER` for automatic step logging
- **Platform**: Android with Wear OS support