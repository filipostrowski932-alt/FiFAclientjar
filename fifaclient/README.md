# FIFACLIENT (Fabric 1.21.11)

Client-side mod nastawiony na wizualia: duży animowany ClickGUI, HUD z motywami, edytor HUD-a, powiadomienia.

## Budowanie
1. Potrzebujesz JDK 21.
2. Otwórz folder w IntelliJ IDEA (najprościej; sam pobierze Gradle z `gradle-wrapper.properties`),
   albo w terminalu: `gradle wrapper --gradle-version 9.2.0` a potem `./gradlew build`.
3. Gotowy jar: `build/libs/fifaclient-1.0.0.jar` -> wrzuć do `.minecraft/mods`
   razem z Fabric API (0.141.1+1.21.11) i Fabric Loaderem >= 0.18.1.

Testowanie bez budowania jara: `./gradlew runClient`.

## Sterowanie
- Right Shift - ClickGUI (zmienisz w Ustawienia -> Sterowanie -> FIFACLIENT)
- C (trzymaj) - Zoom
- W ClickGUI: lewy klik = włącz/wyłącz, prawy klik na module = ustawienia, przycisk "HUD Editor" = przeciąganie elementów
- W edytorze HUD: przeciąganie, scroll = rozmiar, prawy klik = reset pozycji

## Moduły
HUD: Watermark, FPS, Ping, CPS, Clock, Coordinates, Keystrokes, Armor, ModuleList
Visual: Zoom, Fullbright, Notifications
Movement: ToggleSprint
Client: Appearance (motyw: FIFA / Neon / Sunset / Ocean / Rainbow, przyciemnienie tła, zaokrąglenie)

Konfiguracja zapisuje się w `.minecraft/config/fifaclient.json`.

## Dodawanie własnego modułu
Stwórz klasę rozszerzającą `ClientModule` (lub `HudModule` dla elementu HUD) i dopisz ją w `ModuleManager.init()`.
Ustawienia: `add(new BoolSetting(...))`, `NumberSetting`, `ModeSetting` - pojawią się w GUI i configu automatycznie.

## Uwaga
Fullbright jest czysto kliencki, ale część serwerów zabrania go w regulaminie - sprawdź zasady serwera.
