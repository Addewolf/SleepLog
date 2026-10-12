# Verifiering – SleepLog v0.1

Datum: **2026-10-12**. Checkpoint: **v0.1.0**.

## APK

- Fil: `SleepLog-v0.1.apk` (samma fil som `app/build/outputs/apk/debug/app-debug.apk`).
- Storlek: **929 588 byte**, cirka **0,93 MB**; under gränsen 5 000 000 byte.
- SHA-256: `3c58c8a0d1ce902494cc7f76a26631428680530dacfc759dd123f87f521893bf`.
- Paket: `se.adde.sleeplog`, versionName `0.1`, versionCode `1`.
- min SDK 23, target SDK 36, compile SDK 37. Svenska och engelska ingår i APK-filen.
- `apksigner verify --verbose`: giltiga v1- och v2-signaturer. Lokal Android-debugnyckel.
- APK-manifestet begär inga behörigheter. Testbiblioteken ingår inte i APK-filen.

## Automatiska kontroller

Slutkörningen av följande uppgifter rapporterade **BUILD SUCCESSFUL**:

```text
:app:testDebugUnitTest :app:lintDebug :app:assembleDebug :core:verify
```

**32 kärnkontroller passerade**, bland annat:

- Sovtid och fördelning över midnatt, exakt midnatt, månad/årsskifte och flera dygn.
- Flera pass samma datum och längdposter utan start-/sluttid.
- Sommar-/vintertid i Europe/Stockholm, upprepad hösttimme och fasta GMT-offsetar.
- Ogiltiga datum, klockslag, negativa/noll längder och slut före eller lika med start.
- Ändrade och raderade poster efter sparning och återläsning.
- Återläsning i **en separat JVM-process** samt återställning efter simulerad avbruten skrivning.
- Skadad lagring avvisas. Svenska/engelska rapporter överensstämmer med testposterna.

**12 Android-UI-tester passerade**: sex scenarier vardera på API 23 och API 36
i Robolectric **4.17** på Windows med JDK **25.0.3**. Detta är simulering på datorn.

- Tidsbestämd registrering via datum-/tidväljare, 24-timmarsläge och flera pass.
- Längdposter, redigering med bevarat post-ID och bekräftad/avbruten radering.
- Sparade poster och osparat formulär när Android återskapar aktiviteten.
- Bestående språkval, engelsk UI-text och oförändrade registreringar vid språkbyte.
- Dokumentmallens ögonblicksbild motsvarar lagringen; filväljarens avbrytande lämnar data kvar.
- Ogiltig redigering skriver inte över tidigare registrering.

**Android lint: 0 fel, 2 varningar.** Varningarna gäller att nyare target SDK och
Gradle finns. NoteWriters befintliga verktygsversioner behålls avsiktligt i v0.1.
JDK 25 ger även kompatibilitetsvarningar för byggverktygens native-anrop; bygget och
testerna avslutades utan fel. APK-verifieraren visar sin standardvarning om
AGP:s `META-INF`-metadata för v1-signaturen; v2-signaturkontrollen passerar.

Maskinskapade rapporter finns lokalt under `app/build/reports/`,
`app/build/test-results/` och `.release-prep/verification.log`. De ingår inte i Git.

## Källkod och ursprung

- MIT-licensen är identisk med NoteWriters licens.
- NoteWriters arbetskopia ändrades inte.
- Versionshanterade källfiler kontrollerades efter nyckelmönster, lokala SDK-sökvägar
  och maskinspecifika filer. `.gitignore` utesluter byggresultat, cache, lokala inställningar
  och signeringsmaterial.
- Den första källkodskopian och dess commit dokumenteras i README. Git-historiken är fristående.

## Återstår på fysisk telefon

Ingen telefon var ansluten via ADB under arbetet. Följ [PHONE_TEST.md](PHONE_TEST.md) för:

- Installation och samtidig installation med NoteWriter.
- Faktisk layout, läsbarhet, pekinteraktion, tangentbord och stora systemtypsnitt.
- Lagring efter verklig telefonomstart.
- Att filväljaren sparar dokumentet och att en annan app kan öppna det.

V0.1 har en standardmall i textformat, saknar rapportimport/återställning och summerar
överlappande sovpass var för sig. Den första förekomsten av en upprepad hösttimme går
inte att välja separat. Dessa regler beskrivs även i README och gränssnittet.

Utvecklingen stannar vid denna checkpoint för Adde/Andreas telefontest.
