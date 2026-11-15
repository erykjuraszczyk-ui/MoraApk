# Wydanie i podpisywanie APK

## Debug APK

Workflow **Android CI** uruchamia testy i buduje `app-debug.apk`. To kompilacja debug podpisana kluczem debugowym Android Gradle Plugin — służy do testów, nie do publikacji jako stały kanał aktualizacji.

## Własny klucz release

Utwórz własny keystore poza repozytorium i zachowaj kopię zapasową w bezpiecznym miejscu. Utrata klucza może uniemożliwić aktualizację istniejącej instalacji tym samym identyfikatorem aplikacji.

Przykładowe utworzenie klucza (uruchom lokalnie w JDK):

```bash
keytool -genkeypair -v -keystore mora-release.jks -alias mora-release -keyalg RSA -keysize 3072 -validity 10000
```

Nie commituj pliku `.jks` ani haseł.

## Sekrety GitHub Actions

W ustawieniach repozytorium dodaj:

- `MORA_KEYSTORE_BASE64` — zawartość keystore zakodowana Base64;
- `MORA_KEY_ALIAS` — alias klucza;
- `MORA_KEYSTORE_PASSWORD` — hasło magazynu;
- `MORA_KEY_PASSWORD` — hasło klucza.

Kodowanie Base64 wykonaj lokalnie: `base64 -w 0 mora-release.jks`. Nie wklejaj sekretów do rozmowy ani issue.

Workflow **Signed Android Release** uruchom ręcznie albo przez tag `v*`. Buduje i publikuje podpisany artefakt APK.

## Kontrola artefaktu

Po pobraniu pliku sprawdź podpis:

```bash
apksigner verify --verbose --print-certs app-release.apk
sha256sum app-release.apk
```

Przed dystrybucją przetestuj instalację, aktualizację i odinstalowanie na urządzeniu testowym. Zwiększaj `versionCode` przy każdym kolejnym wydaniu.
