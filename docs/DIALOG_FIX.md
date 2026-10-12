# Gemensamma synliga dialogknappar – v0.1.3

Adde bekräftade att v0.1.2:s datum-/tidsväljare fungerar på telefonen. Samma
osynliga viloläge fanns däremot kvar på **Avbryt/Radera** i raderingsdialogen.

## Orsak och ändring

Förra fixen tillämpades endast i `PickerDialogs.show`. Radering, språkval,
dokumentmall och valideringsmeddelande skapades direkt med `AlertDialog.Builder`
och använde fortfarande appens ursprungliga dialogstil.

`ui/Dialogs.kt` samlar nu det separata dialogtemat och behandlingen av de faktiska
knapparna före första ritning. Samtliga appägda dialoger, inklusive väljarna,
använder samma lösning: grön bakgrund, vit text och ingen ärvd bakgrundstint.
`DialogTheme`/`DialogAction` ligger i `values/styles.xml`, bakgrunden i
`drawable/dialog_action_background.xml` och färgerna i `values/colors.xml`.
Appens standardvärde för `alertDialogTheme` pekar också på det gemensamma temat.

Underrubriken ”En lugn plats för dina sovtider.” och den engelska motsvarigheten
är borttagna ur både gränssnitt och språkresurser. Ingen ersättningstext tillagd.

Raderingsbekräftelse, beräkningar och lagring är oförändrade. Paketet är fortsatt
`se.adde.sleeplog`; versionCode 4/versionName 0.1.3.

## Verifiering

Slutbygget 2026-10-12: **BUILD SUCCESSFUL**. **33 Android-tester** passerade
(20 aktivitetstester och 10 väljar-/layouttester på API 23/36 samt 3 native-
renderingstester på API 36). **32 kärnkontroller** passerade. Lint: **0 fel,
2 befintliga versionsvarningar**. Bygglogg: `.release-prep/verification-v0.1.3.log`.

APK: **SleepLog-v0.1.3.apk**, **928 141 byte** (cirka 0,93 MB).
SHA-256: `e8bd5810b01f672ca2cfbe9d34a4e428ffe521e9c4bee3df2f26fe01cac1f724`.
Signaturen verifierad med `apksigner`, samma certifikat som v0.1.2. Paket/version
kontrollerade med `aapt`; manifest och `core/` är oförändrade. Sökning i appkällan
bekräftar att underrubriken och dess språkresurser är borttagna.
Checkpoint: **v0.1.3**.

Nya native-renderingstester kontrollerar bakgrunds- och textpixlar före tryck i
radering, språkval, mallval och felmeddelande, på svenska och engelska med textskala
2,0. De kontrollerar även att Avbryt bevarar posten och Radera tar bort den först
efter bekräftelse. Tidigare väljar- och kärntester behålls.

Telefonkontroll: [PHONE_TEST.md](PHONE_TEST.md). Den nya raderingsfixen är ännu
inte bekräftad på fysisk telefon; v0.1.2:s väljarfix är bekräftad av Adde.
