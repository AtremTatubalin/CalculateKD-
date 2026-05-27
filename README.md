# Сроки КД

Офлайн Android-приложение для предварительной оценки трудоёмкости и срока разработки КД.

Стартовые коэффициенты являются начальными и должны корректироваться по статистике выполненных проектов.

## Формулы
- T = Tbase × Kcharacter × Kpackage × Kinputs × Kapproval
- D = ceil(T / (engineers × effectiveHoursPerDay) × calendarReserve)
- Dmin = ceil(D × (1 - uncertainty))
- Dmax = ceil(D × (1 + uncertainty))

## Сборка
Открыть проект в Android Studio (JDK 17), выполнить `./gradlew test` и `./gradlew assembleDebug`.
APK: `app/build/outputs/apk/debug/`.
