# nonnon

**nonnon** is a Persian-first, offline-first English-learning Android app with a pink/purple visual identity and its own illustrated mascot.

## Android compatibility
- Minimum Android: 6.0 (API 23); designed for Android 9 / Galaxy S8+
- Native Android UI; lessons work without an account or internet connection
- Pronunciation uses the phone's built-in Text-to-Speech engine

## Included in this starter release
- Six CEFR stages (A1–C2), each arranged as 10 units with 5 playable short lessons per unit
- Multiple-choice practice, examples, instant feedback, hearts and XP
- Vocabulary notebook with pronunciation playback
- Daily goal, streak, profile, locally saved progress, sound and goal settings
- Custom nonnon launcher icon and mascot artwork

The first build uses locally generated question sets from starter vocabulary decks (12 entries per level). It is a playable app foundation, not yet a fully editorially authored 300-lesson course. Larger reviewed lesson packs and the final approved mascot image can be added without changing the app structure.

## Build an APK
GitHub Actions builds a debug APK on every push to `main` and supports manual runs. Open **Actions → Android APK → latest run → Artifacts → nonnon-debug-apk** and download the APK.

Local build: JDK 17 + Gradle 8.9, then `gradle --no-daemon assembleDebug`.
