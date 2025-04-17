# Momentum: Habit Tracker App

**Momentum** is a productivity-focused Android application that helps users build and maintain healthy habits through consistent daily tracking. Whether it's exercising, reading, drinking enough water, or studying, Momentum supports users in forming lasting routines with motivation and ease. With a clean, adaptive interface, the app plans to support both portrait and landscape orientations and offers seamless functionality across mobile and Wear OS devices.


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

# Sprint 2 Check-in

## Key Features Completed 

- **Create & Track Habits**: Users can add new habits, mark them as completed, or delete them as needed.
- **Progress Visualization**: Track daily, weekly, and monthly progress with color-coded indicators.
- **Room Database Integration**: All habits and completion records are stored locally using Room DB for reliable data persistence.

## Calendar Views

- **Weekly & Monthly Toggle**: Switch between a weekly or monthly calendar to review habit completion history.
- **Date Highlights**: Completed habits are visually marked on the calendar for quick progress tracking.
- **Daily Summary**: Selecting a date displays a list of habits completed that day.

## Motivational Support

- **ZenQuotes API Integration**: The app displays a fresh motivational quote on launch to inspire users.

## Multi-Device Support

- **Phone (Pixel 8)**: Fully functional with adaptive layouts for different screen orientations.
- **Wear OS Watch (In Progress)**: Basic compatibility for square WearOS watches, with ongoing improvements.

## Future Enhancements

- **User Authentication**: Secure login/logout to sync habits across devices.
- **Step Counter Integration**: Visual step-tracking for fitness-related habits.

## Technical Details

- **Database**: Room DB for storing habits and completion logs (future support for user-specific data).
- **APIs**: ZenQuotes (live)
- **Code Structure**: Modular design with separate Gradle-linked modules for phone and watch.

## Challenges
- Balancing a minimal UI with cross-device functionality—especially for Wear OS—while maintaining a seamless user experience.
