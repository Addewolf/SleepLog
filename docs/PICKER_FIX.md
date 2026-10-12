# Datum- och tidsväljare – v0.1.1

## Orsak

Adde bekräftade att v0.1 installerats och fungerar på telefonen, men rapporterade
att bekräftelsen i datum-/tidsväljarna inte gick att se tydligt och att ändringar
försvann när väljaren stängdes.

En reproducerbar temabugg hittades i Robolectric: med v0.1:s app-tema fick
standarddialogens positiva knapp färgen `#FFFFFFFF` på API 36, medan API 23
gav `#FF4A604D`. Appens generella knappstil har vit text för fyllda gröna knappar.
Den textfärgen läckte in i nyare Androids ljusa dialog med kantlösa knappar.
Knappen fanns och dess synliga rektangel låg inom skärmen i provet 320 × 480 dp.
De gamla testerna klickade programmässigt på knappen och upptäckte därför inte
att texten hade fel färg.

Dialogernas callback körs först vid positiv bekräftelse. Att stänga en dialog
utan bekräftelse lämnar med avsikt det tidigare fältvärdet kvar. Koden läser
fältets aktuella värde när dialogen öppnas; den återställer inte till appstarttid.
Exakt färg/layout på användarens telefon har inte mätts via ADB.

Ett separat layouttest i 480 × 320 dp med textskala 2,0 reproducerade dessutom
klippning av knapparna i standarddialogen: på API 23 var en knapp inte synlig;
på API 36 syntes endast 44 av dess 61 pixlar i höjd. Enbart färgändringen räckte
alltså inte för korta skärmar.

## Ändring

- Ett separat `PickerTheme` med `PickerAction` i `res/values/styles.xml` isolerar
  systemväljarna från formulärets knappstil. Mörkgrön text på ljus dialogyta.
- Alla tre datumfält och båda tidsfält använder detta tema och uttryckliga
  **Välj/Avbryt** (engelska **Select/Cancel**).
- Androids vanliga datum-/tidsdialoger behålls, inklusive fokusbekräftelse av
  inmatade värden och 24-timmarsläget. Bekräftelse uppdaterar rätt fält och
  förhandsvisning; avbrytande ändrar inget. Lagring och tidsregler är oförändrade.
- `ui/PickerDialogs.kt` gör själva väljarinnehållet rullbart inom dialogen.
  Bekräftelseknapparna ligger kvar utanför rullningen.
  `CompactDatePicker` och `CompactTimePicker` i temat väljer Androids rullväljare.
  Kalender-/urtavlelägets inbyggda knappanel kan annars hamna inne i väljarens
  innehåll, vilket gör att enbart en rullbehållare inte räcker. Höjden begränsas
  efter tillgänglig skärmhöjd och plats reserveras för text och knappar.
- `versionCode=2`, `versionName=0.1.1`, samma `se.adde.sleeplog` och debugnyckel.
  Ingen databasmigrering, rensning eller ändring av lagringsfilernas namn/format.

## Verifiering

Slutbygget 2026-10-12 rapporterade **BUILD SUCCESSFUL** för
`:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :core:verify`.

- **28 UI-/layouttester passerade** på API 23 och 36 i Robolectric 4.17.
  Alla tre datumfält och båda tidsfält verifieras för bekräftelse, avbrytande,
  återöppning och 24-timmarsformat. Nya och redigerade poster kontrolleras mot
  förhandsvisning, lagring, dygnssummor och rapport. Post-ID bevaras vid redigering.
- Layoutproven omfattar 320 × 480 dp med textskala 1,0 och 2,0 samt
  480 × 320 dp i liggande läge med textskala 2,0, även engelska i liggande läge.
  Båda knapparnas färg, synlighet, hela synliga rektangel, minst 48 dp höjd och
  oavklippta text kontrolleras för samtliga fem fält.
- **32 kärnkontroller passerade**, inklusive återläsning av lagring i separat process.
- **Lint: 0 fel, 2 tidigare varningar** om nyare target SDK/Gradle.
- Paket `se.adde.sleeplog`, versionCode **2**, versionName **0.1.1** verifierade
  med `aapt`. `apksigner` verifierade v1/v2 och **identiskt signeringscertifikat**
  jämfört med levererade `SleepLog-v0.1.apk`.
- Git-diff bekräftar att manifestet och hela `core/`, inklusive lagring, är oförändrade.

APK: **SleepLog-v0.1.1.apk**, **926 484 byte** (cirka 0,93 MB).
SHA-256: `8d9ffd4deba752ba73520ff48dca6c08e2e981e9ca659c8d405df80e6ca12b26`.
Checkpoint: **v0.1.1**. Bygglogg lokalt: `.release-prep/verification-v0.1.1.log`.
Den ursprungliga v0.1-rapporten finns kvar i [VERIFICATION.md](VERIFICATION.md).

Telefonkontrollen följer [PHONE_TEST.md](PHONE_TEST.md). Ingen ADB-telefon var
ansluten under fixen. Datorsimulering ersätter inte kontroll av tillverkarens
Android-variant och telefonens faktiska skärm-/textinställningar.
