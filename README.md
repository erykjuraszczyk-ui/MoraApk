# Mora — REDMAGIC Edition

Mobilny klient Agent Cortana / Mora dla Androida 13+.

**Status:** pierwszy szkielet aplikacji (0.1.0) — Kotlin, Jetpack Compose, Voice Core, klient AI zgodny z API OpenAI Chat Completions, szyfrowana pamięć lokalna, Emotion Core (8 stanów), animowany interfejs holograficzny, opcjonalna nakładka, powiadomienia oraz CI budujące APK debug.

## Wymagania

- Android Studio z obsługą Android SDK 36
- JDK 17
- Gradle 8.13
- Android 13+ (minSdk 33)

## Budowanie

W Android Studio otwórz katalog repozytorium, pozwól Gradle zsynchronizować zależności i wybierz **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

Z wiersza poleceń, po zainstalowaniu Gradle 8.13 i Android SDK:

```bash
gradle testDebugUnitTest assembleDebug
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions wykonuje testy jednostkowe, buduje APK debug i publikuje go jako artefakt workflow.

## Konfiguracja AI

W aplikacji otwórz **Ustawienia** i podaj:
- Endpoint HTTPS, np. `https://api.openai.com/v1` lub adres zgodnego serwera;
- identyfikator modelu obsługującego endpoint;
- klucz API.

Aplikacja dopisuje `/chat/completions`, jeśli nie podano pełnej ścieżki. Klucz jest szyfrowany przez AES-GCM z kluczem przechowywanym w Android Keystore. Klucz nie jest częścią repozytorium ani APK. Nie wpisuj klucza do kodu źródłowego, issue ani logów.

## Funkcje w tej wersji

- Polski interfejs i czat.
- Rozpoznawanie mowy przez Android SpeechRecognizer (pl-PL) i synteza przez Android TextToSpeech.
- Połączenie z modelem przez HTTPS i API Chat Completions.
- Przełączane tryby Mora / Cortana.
- Emotion Core z ośmioma etykietami ekspresji i heurystyczną analizą tekstu.
- Lokalna pamięć opcjonalna; treść jest szyfrowana AES-GCM przed zapisem.
- Animowany holograficzny orb 2D w Compose, z ograniczaniem animacji.
- Opcjonalna nakładka systemowa po udzieleniu specjalnego dostępu.
- Kanał powiadomień i przycisk powiadomienia testowego.
- Tryb gry ograniczający animacje aplikacji.

## Ograniczenia — jawnie

1. Emotion Core analizuje słowa i kontekst tekstowy. Nie diagnozuje emocji ani nie odczytuje ich z pewnością.
2. Holographic UI w wersji 0.1.0 jest animowanym interfejsem 2D. Pełny model 3D wymaga dostarczenia i integracji modelu `.glb/.gltf`.
3. Rozpoznawanie mowy korzysta z dostawcy rozpoznawania skonfigurowanego w Androidzie; dostępność i działanie offline zależą od urządzenia i usług.
4. Nie ma ukrytego, stałego nasłuchu ani automatycznego startu mikrofonu w tle. Sesja głosowa rozpoczyna się po akcji użytkownika.
5. REDMAGIC Game Space nie ma tutaj niepublicznej integracji producenta. Przełącznik „Tryb gry” ogranicza animacje Mory; zarządzanie baterią i Game Space pozostaje po stronie systemu.
6. Asystent AI wymaga własnego endpointu i klucza API. Bez konfiguracji aplikacja pokazuje jasną instrukcję zamiast udawać, że model działa.
7. CI publikuje APK debug. Wydanie release wymaga własnego klucza podpisującego — patrz [docs/RELEASE.md](docs/RELEASE.md).

## Struktura

- `app/src/main/java/pl/mora/app/domain` — AI i Emotion Core.
- `app/src/main/java/pl/mora/app/data` — bezpieczne ustawienia i pamięć.
- `app/src/main/java/pl/mora/app/ui` — ViewModel i Jetpack Compose.
- `app/src/main/java/pl/mora/app/integration` — nakładka i powiadomienia.
- `.github/workflows` — build/test APK i opcjonalny podpis release.
