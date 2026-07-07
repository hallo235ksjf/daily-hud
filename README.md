# DAILY // HUD — native Kotlin/Compose

Komplett umgebaute Version der ursprünglichen Capacitor-Webview-App.
Keine HTML/JS/WebView mehr — reines natives Android mit **Kotlin + Jetpack Compose**.

## Was sich ändert

| | Web-Version (Capacitor) | Diese Version |
|---|---|---|
| UI | HTML/CSS/JS im WebView | Jetpack Compose |
| Speicher | `localStorage` | `SharedPreferences` (JSON, gleiche Struktur) |
| Wecker | `setInterval`, klingelt nur bei offener App/Tab | **`AlarmManager` + Foreground Service**, klingelt auch im Hintergrund/bei gesperrtem Screen |
| Klingeltöne | Web Audio Oscillator | `AudioTrack`-Synthese, exakt gleiche Frequenzmuster (Beep/Chime/Alert) |
| Hintergrundmusik | `<audio>`-Tag | `MediaPlayer` + Storage-Access-Framework-Dateiauswahl |
| Build | Capacitor + npm + `cap sync` | reines Gradle |

Seiten (Aufgaben → Fokus → Wecker → Notizen), Farben, FAB, Doppel-Tap-zum-Löschen —
alles 1:1 aus der Web-Version übernommen.

## Setup in Termux

```bash
pkg install git -y
cd ~/daily-hud-kt
git init
git add .
git commit -m "DAILY HUD: native Kotlin/Compose rewrite"
git branch -M main
git remote add origin https://github.com/DEIN_USERNAME/daily-hud.git
git push -u origin main
```

GitHub Actions baut bei jedem Push automatisch die APK
(`.github/workflows/build-apk.yml`) und lädt sie als Artifact hoch —
kein Android Studio auf dem Handy nötig, kein npm/Capacitor mehr im Weg.

## Fester Debug-Keystore

Wie bei Nova Player liegt ein fester Debug-Keystore (`app/debug.keystore`) im Repo,
damit jede CI-Build die gleiche Signatur hat und du die APK ohne
"Signaturen stimmen nicht überein"-Fehler über die alte Version installieren kannst.

## Berechtigungen, die beim ersten Start abgefragt werden

- **Benachrichtigungen** (Android 13+) — für die Wecker-Notification
- **Exakte Alarme** (Android 12+) — führt dich zu den Systemeinstellungen, falls die
  App das nicht automatisch darf

## Struktur

```
app/src/main/java/com/halel/dailyhud/
├── MainActivity.kt          # Einstiegspunkt, Permission-Requests
├── AppViewModel.kt          # State für Tasks/Alarms/Notes/Settings
├── Data.kt                  # Datenmodelle + SharedPreferences-Repository
├── ToneGen.kt                # native Klingelton-Synthese (AudioTrack)
├── AlarmScheduler.kt         # AlarmManager-Logik
├── AlarmReceiver.kt          # BroadcastReceiver + Boot-Reschedule
├── AlarmRingService.kt       # Foreground Service, klingelt im Hintergrund
├── AlarmRingActivity.kt      # Vollbild-Weckbildschirm (auch über Lockscreen)
├── Theme.kt                  # Dark/Light-Farbpalette (aus dem alten CSS übernommen)
└── ui/
    ├── MainScreen.kt          # HorizontalPager, FAB, Dialog-State
    ├── Components.kt          # TopBar, Karten, Doppel-Tap-Löschen-Chip
    ├── Pages.kt                # Aufgaben/Fokus/Wecker/Notizen-Seiten
    ├── Dialogs.kt              # Bottom-Sheets zum Anlegen/Bearbeiten
    └── SettingsSheet.kt        # Theme, Klingelton, Hintergrundmusik
```

Alle Daten bleiben weiterhin nur auf dem Gerät — keine Cloud, kein Server.
