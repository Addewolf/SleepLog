# Synliga bekräftelseknappar från början – v0.1.2

## Återkoppling och avgränsning

Adde rapporterade att bekräftelsen först syntes efter ett tryck på knappområdet.
Installerad APK-version och telefonmodell var inte kända vid undersökningen;
benämningen ”OK” kan även avse v0.1, eftersom v0.1.1 använder Välj/Select.
Telefonens exakta renderingsfel är därför inte reproducerat eller fastställt här.

V0.1.1 använde fortfarande Androids kantlösa knappar. Den stilen ger ingen
stadig fylld knappbakgrund i viloläget; tryckeffekten är separat. Den tidigare
kontrollen verifierade textfärg och layout, inte den faktiskt ritade vilobilden.

## Ändring

- Båda dialogknapparna får en permanent grön yta och vit text före första ritningen.
- `res/drawable/picker_action_background.xml` innehåller en solid bakgrund och en
  separat tryckeffekt. Färgerna hämtas fortfarande från `values/colors.xml`.
- `PickerAction` anger stilen. `ui/PickerDialogs.kt` sätter samma bakgrund på de
  skapade plattformsknapparna, tar bort ärvd bakgrundstint och sätter en konstant
  textfärg så att tryck/fokus inte behövs för att få läsbar kontrast.
- Datum-/tidsflöde, språk, 24-timmarsformat, paket och lagringsformat är oförändrade.
  VersionCode är 3 och versionName 0.1.2.

## Verifiering

Tester: 30 UI-/layouttester på API 23/36 och ett test med Robolectrics native
renderare på API 36. Renderartestet ritar båda knapparna i alla fem väljare till
bitmappar utan att först trycka på dem, och kräver både grön fyllning och vita
textpixlar. Övriga tester kontrollerar viloläge, tryck/släpp, stor text,
stående/liggande skärm, systemets nattläge, bekräftelse, avbrytande och sparning.
De 32 befintliga kärnkontrollerna för tid, lagring och dokument körs också.

Slutbygget 2026-10-12: **BUILD SUCCESSFUL**, **31 Android-tester och 32
kärnkontroller passerade**. Lint: **0 fel, 2 befintliga versionsvarningar**.
Bygglogg lokalt: `.release-prep/verification-v0.1.2.log`.

APK: **SleepLog-v0.1.2.apk**, **936 235 byte** (cirka 0,94 MB).
SHA-256: `35832c0320986c8be7416351881a11178368f7f63ef834d6e904d128422672fb`.
`apksigner` verifierade v1/v2 och samma signeringscertifikat som både levererade
v0.1 och v0.1.1. `aapt` bekräftade `se.adde.sleeplog`, versionCode 3/versionName
0.1.2. Git-diff visar oförändrat manifest och oförändrad `core/`, inklusive lagring.
Checkpoint: **v0.1.2**.

Ingen telefon var ansluten via ADB. Prova första visningen enligt
[PHONE_TEST.md](PHONE_TEST.md) på telefonen; avinstallera inte den befintliga appen.
