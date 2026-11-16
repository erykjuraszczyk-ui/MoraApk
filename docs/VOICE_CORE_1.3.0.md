# MORA Voice Core 1.3.0

## Zakres

Voice Core 1.3.0 rozwija lokalną warstwę mowy w aplikacji towarzyszącej MORA AI dla Androida 13+.

### Funkcje
- Rozpoznawanie wypowiedzi z preferowanym językiem polskim `pl-PL`.
- Rozpoznawanie pojedynczego najlepszego wyniku, bez strumieniowego dopisywania częściowych rezultatów do czatu.
- Synteza mowy po polsku przez systemowy Android TextToSpeech, jeśli urządzenie ma zainstalowane dane głosowe.
- Kolejka `QUEUE_FLUSH`: nowa wypowiedź zastępuje poprzednią.
- Przycisk zatrzymania odczytu w ekranie rozmowy.
- Jawne anulowanie rozpoznawania i bezpieczne zwalnianie zasobów kontrolera.
- Obsługa braku usługi rozpoznawania oraz błędów uruchomienia bez awarii ekranu.

## Prywatność i ograniczenia
- Klasa VoiceController nie wysyła nagrania do własnego serwera MORA.
- Androidowy dostawca rozpoznawania mowy może przetwarzać dźwięk zgodnie z własną konfiguracją i polityką prywatności. To zależy od usługi systemowej, a nie od aplikacji MORA.
- Mikrofon jest uruchamiany wyłącznie po dotknięciu przycisku i przyznaniu uprawnienia RECORD_AUDIO.
- Nie ma nasłuchiwania w tle ani aktywacji słowem wybudzającym.
- Dostępność polskiego TTS zależy od zainstalowanego głosu w systemie.

## Test na REDMAGIC 7
1. Zainstaluj debug APK z GitHub Actions po udanym przebiegu CI.
2. Otwórz Ustawienia Androida i sprawdź, czy dostępny jest polski głos zamiany tekstu na mowę.
3. W MORA AI przyznaj dostęp do mikrofonu po pierwszym naciśnięciu ikony mikrofonu.
4. Powiedz krótkie zdanie po polsku i sprawdź, czy tekst pojawia się w rozmowie.
5. Naciśnij ikonę głośnika przy odpowiedzi asystenta.
6. Naciśnij przycisk zatrzymania i potwierdź, że odczyt ustaje.
7. Odmów uprawnienia do mikrofonu lub wyłącz usługę rozpoznawania i sprawdź, czy aplikacja wyświetla komunikat zamiast ulegać awarii.

## Następny krok
Voice Core 1.4.0: ustawienia szybkości mowy i wybór dostępnego głosu systemowego, z odczytem listy głosów bez instalowania zewnętrznych pakietów.
