# Ai Healthy

Мобильное приложение для здоровья: AI-помощник Healthy, трекеры питания, настроения, привычек, сна и ежедневник.

## Стек

- **Android:** Kotlin, Jetpack (ViewModel, LiveData, Room), Material Design, Compose
- **Backend:** Node.js (Express), прокси к DeepSeek API
- **Данные:** Room, SharedPreferences, REST API (Retrofit, OkHttp)

## Структура

- `app/` — Android-приложение
- `backend/` — Node.js сервер для чата с AI

## Сборка

1. Открой проект в Android Studio.
2. В `app/build.gradle.kts` при необходимости измени `BACKEND_URL` (по умолчанию указан сервер).
3. Backend: в папке `backend` создай `.env` с `DEEPSEEK_API_KEY` и `PORT=3000`, затем `npm install` и `npm start`.
4. Собери APK: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.

## Подписка

14 дней бесплатного триала, далее подписка (логика в `SubscriptionManager`).

## Лицензия

Приватный проект.
