# SleepLog

En liten Android-app för att registrera sömn. Svenska är förvalt; **Språk / Language**
växlar mellan svenska och engelska. Klockslag visas alltid i 24-timmarsformat.

## Användning

- **Datum och klockslag:** välj datum och tid för insomning och uppvaknande.
  Formuläret föreslår de senaste åtta timmarna; ändra värdena innan du sparar.
  **Välj** bekräftar värdet; **Avbryt** behåller det tidigare värdet.
- **Enbart längd:** välj datum och antal minuter. Dessa poster får inga påhittade klockslag.
- Spara flera pass samma dag. Historiken visar varje post och **Sovtid per dygn** visar summorna.
- **Ändra** öppnar en sparad post. **Radera** kräver bekräftelse.
- **Skapa dokument → Standard** sparar ett UTF-8-textdokument med dygnssummor och samtliga
  registreringar. Språket följer appens val. Välj plats och filnamn i Androids filväljare.

Registreringarna sparas i appens privata lagring på telefonen. Appen har inga
nätverksbehörigheter, konton, annonser eller molntjänster. Sparade poster och språkval
bevaras när appen stängs. Avinstallation eller **Rensa lagring** tar bort dem.
Exporterade dokument ligger kvar där du sparade dem. Exporten är en rapport, inte en
återläsningsbar säkerhetskopia. Automatisk molnbackup och enhetsöverföring är avstängda.

## Installation och telefontest

Android **6.0 eller senare** krävs. Paketnamnet är `se.adde.sleeplog`, så SleepLog
och NoteWriter kan vara installerade samtidigt.

Överför `SleepLog-v0.1.3.apk` till telefonen och öppna den i filhanteraren. Tillåt
installation från den källan om Android frågar. APK-filen är signerad med den lokala
Android-debugnyckeln för denna första testversion. Behåll samma nyckel vid uppdateringar
för att kunna installera över versionen och bevara data.
V0.1.3 använder samma paket, signeringscertifikat och lagringsformat som tidigare versioner.
Installera som uppdatering; avinstallera inte och rensa inte appens lagring.

Med USB-felsökning och en redan godkänd dator kan du i stället köra:

```powershell
adb install -r SleepLog-v0.1.3.apk
```

Följ [telefonens testlista](docs/PHONE_TEST.md). Teststatus och exakta bygguppgifter
finns i [rapporten för gemensamma dialogknappar i v0.1.3](docs/DIALOG_FIX.md),
[rapporten för synliga knappar i v0.1.2](docs/PICKER_VISIBILITY.md),
[fixrapporten för v0.1.1](docs/PICKER_FIX.md) och
[grundversionens verifieringsrapport](docs/VERIFICATION.md).

## Tidsregler i v0.1

- Ett tidsbestämt pass sparar start, slut och den tidszon som gällde när posten skapades.
  Senare byte av telefonens tidszon flyttar inte den äldre postens datum/klockslag.
- Tiden räknas som verkligt förfluten tid, inklusive sommar-/vintertid, och delas vid
  lokal midnatt i postens tidszon. Vid upprepad hösttimme används den senare förekomsten;
  den första förekomsten kan inte väljas separat i v0.1. Icke-existerande vårklockslag avvisas.
- Längdposter räknas helt på valt datum. Överlappande pass summeras var för sig;
  appen slår inte ihop överlappningar. Summan kan därför överstiga 24 timmar.
- Datum stöds 1900–2100. Varje post måste vara minst en minut och högst 366 dygn.
- De senaste 30 datumen respektive posterna visas först. **Visa fler** utökar listan.
  Dokumentet innehåller alltid alla registreringar.
- Osparade formulär bevaras när skärmen återskapas, exempelvis vid språkbyte eller rotation.
  Spara innan appen tvångsstoppas eller telefonen startas om.

## Bygga och testa

Byggmiljön återanvänds från NoteWriter: Gradle **9.6.0** (wrapper), Android Gradle
Plugin **9.4.0** med inbyggt Kotlin, SDK Platform **37**, Build Tools **36.0.0**,
target SDK **36** och min SDK **23**. Den lokala byggningen använder Android Studios
JDK **25**. Java/Kotlin-källan kompileras med Java 17-kompatibilitet.

1. Installera Android Studio med dessa SDK-verktyg. Öppna projektmappen.
2. Ange `JAVA_HOME` till Android Studios JDK. Ange `ANDROID_HOME` till din SDK eller
   låt Android Studio skapa `local.properties`. Den filen ska inte versionshanteras.
3. Kör från projektroten:

```powershell
.\gradlew.bat --no-daemon --console=plain :core:verify :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

På macOS/Linux används `sh ./gradlew` med samma argument. Första körningen laddar ned
bygg- och testverktyg från Google/Maven. Därefter kan `--offline` användas när allt finns
i cachen. APK: `app/build/outputs/apk/debug/app-debug.apk`.

`core:verify` testar tidsregler, rapporter och lagring, inklusive återläsning i en separat
process. Android-gränssnittet testas med JUnit/Robolectric på datorn. Testbiblioteken ingår
inte i appen. Dessa tester ersätter inte handhavande eller verklig omstart på telefon.

## Var ändrar man vad?

| Del | Plats |
| --- | --- |
| Bakgrunds- och ytfärger | `app/src/main/res/values/colors.xml` |
| Typsnitt, textstorlekar och textstilar | `app/src/main/res/values/typography.xml` |
| Tema och gemensam knappstil | `app/src/main/res/values/styles.xml` |
| Gemensamma knappar, kort och avstånd | `app/src/main/java/se/adde/sleeplog/ui/Controls.kt` |
| Dialogknappar och dialogtema | `app/src/main/java/se/adde/sleeplog/ui/Dialogs.kt`, `values/styles.xml` (`DialogTheme`/`DialogAction`), `drawable/dialog_action_background.xml` |
| Datum-/tidsdialogernas rullning och höjd | `app/src/main/java/se/adde/sleeplog/ui/PickerDialogs.kt` |
| Ikon och framtida bilder | `app/src/main/res/drawable/` |
| Svenska/engelska UI-texter | `app/src/main/res/values/strings.xml`, `values-en/strings.xml` |
| Registreringarnas datamodell | `core/src/main/java/se/adde/sleeplog/core/SleepEntry.java` |
| Tidsberäkning och dygnsfördelning | `core/src/main/java/se/adde/sleeplog/core/Times.java` |
| Versionsmärkt lokal lagring och återställning | `core/src/main/java/se/adde/sleeplog/core/SleepStore.java` |
| Standardmall, rapporttexter och formatering | `core/src/main/java/se/adde/sleeplog/core/StandardReport.java` |
| Skärm, registreringsflöde och Androids filväljare | `app/src/main/java/se/adde/sleeplog/MainActivity.kt` |

Standardmallen är tills vidare vanlig text. Fler mallar och deras slutliga placering
bestäms efter telefontestet. Ingen extra UI- eller lagringsram behövs i appen.

## Ursprung och licens

SleepLog är ett separat projekt baserat på
[Addewolf/NoteWriter](https://github.com/Addewolf/NoteWriter), källcommit
[`8cb95bbe3cdf06ad8790b82a9ad53ac7ea10a251`](https://github.com/Addewolf/NoteWriter/commit/8cb95bbe3cdf06ad8790b82a9ad53ac7ea10a251).

Git-metoden är en **selektiv källkodskopia med ny, fristående historik**. Byggfiler,
Gradle-wrapper, MIT-licens och `.gitignore` återanvänds. Androids dokumentflöde,
insets-hantering och den enkla plattformsbaserade UI-principen har anpassats från NoteWriter.
NoteWriters `.git`, byggresultat, lokala inställningar och skärmbilder ingår inte i den
nya Git-historiken. Projektet är inte en GitHub-fork; det har egen appidentitet och utveckling.

**Andreas Andersson** är projektets skapare och upphovsrättsinnehavare.
Astra (OpenAI Codex) har använts för implementation och tekniska kontroller;
Sol / Atlas (OpenAI ChatGPT) har använts för planering och vägledning kring ursprungsprojektet.
AI-assistenterna är inte upphovsrättsinnehavare.

[MIT License](LICENSE) — Copyright (c) 2026 Andreas Andersson.
Medföljande tredjepartskomponenter behåller sina egna licenser.

Teknisk referens: [Androids dokumentflöde](https://developer.android.com/training/data-storage/shared/documents-files),
[språkresurser](https://developer.android.com/training/basics/supporting-devices/languages)
och [Robolectric](https://github.com/robolectric/robolectric/releases).
