# Bezpieczeństwo i prywatność

- Klucz API jest szyfrowany AES-GCM; klucz szyfrujący jest generowany w Android Keystore i nie jest eksportowany.
- Lokalna pamięć rozmów jest domyślnie wyłączona. Po włączeniu jest szyfrowana przed zapisem do katalogu prywatnego aplikacji.
- Ruch do dostawcy AI wymaga HTTPS. Cleartext HTTP jest wyłączony w manifeście i Network Security Config.
- Klucze, hasła, dane pamięci i pliki keystore nie mogą trafiać do repozytorium ani logów.
- Mikrofon jest używany po ręcznym działaniu użytkownika; nie ma ukrytego nasłuchu ani automatycznego uruchamiania mikrofonu z tła.
- Nakładka wymaga jawnego specjalnego dostępu użytkownika i może być zatrzymana z aplikacji.
- Uprawnienie powiadomień jest proszone w kontekście przycisku testowego.
- Emotion Core to heurystyka tekstowa i nie jest narzędziem diagnostycznym.
- Przy zmianie telefonu/ponownej instalacji klucz Android Keystore nie jest migrowany. Zaszyfrowane wpisy pamięci mogą stać się nieczytelne — wyczyść je i rozpocznij nową pamięć na nowym urządzeniu.
