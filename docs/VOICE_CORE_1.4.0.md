# MORA Voice Core 1.4.0

## Cel

Udostępnić ustawienia syntezy mowy bez ingerencji w ustawienia systemowe Androida. Funkcje działają w aplikacji towarzyszącej MORA AI i korzystają z głosów zainstalowanych przez dostawcę TTS urządzenia.

## Funkcje

- Regulacja szybkości mowy w ograniczonym zakresie 0,75–1,25×.
- Regulacja wysokości głosu w zakresie 0,8–1,2×.
- Lista głosów, które Android zgłasza jako polskie.
- Wybór polskiego głosu i zapamiętanie jego identyfikatora.
- Oznaczenie głosów wymagających połączenia sieciowego.
- Odsłuch próbki oraz zatrzymywanie aktualnego odczytu.
- Trwały zapis ustawień szybkości, wysokości i wybranego głosu w prywatnych preferencjach aplikacji.

## Działanie i ograniczenia

- Rozpoznawanie mowy pozostaje ustawione na język `pl-PL`.
- Synteza używa systemowego Android TextToSpeech; aplikacja nie instaluje dodatkowych głosów.
- Dostępność, nazwy i jakość głosów zależą od silnika TTS zainstalowanego na REDMAGIC 7.
- Głos oznaczony jako online może wymagać połączenia z usługą dostawcy Androida.
- Jeśli system nie zgłasza polskich głosów, ekran wyświetla instrukcję sprawdzenia ustawień zamiany tekstu na mowę.
- Ustawienia nie zmieniają globalnej szybkości mowy innych aplikacji.

## Test na telefonie

1. Otwórz ustawienia Androida i sprawdź preferowany silnik zamiany tekstu na mowę.
2. W razie potrzeby pobierz polskie dane głosowe w ustawieniach systemowych.
3. Uruchom MORA AI i wejdź w Ustawienia → Voice Core 1.4.0.
4. Naciśnij „Odśwież głosy”; wybierz jeden z wykrytych głosów polskich.
5. Zmień szybkość i wysokość głosu, a następnie wybierz „Odsłuchaj próbkę”.
6. Zamknij i ponownie uruchom MORA AI; sprawdź, czy ustawienia zostały zachowane.
7. Odtwórz odpowiedź z poziomu rozmowy i sprawdź przycisk zatrzymania.

## Bezpieczeństwo

Nie ma ciągłego nasłuchiwania, nagrywania w tle ani automatycznego wysyłania próbek audio przez MORA. Dostawca systemowego rozpoznawania mowy lub syntezy może jednak przetwarzać dane zgodnie z własną konfiguracją i polityką prywatności.
