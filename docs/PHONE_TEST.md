# SleepLog – kort telefontest

## Uppdatering v0.1.1: datum och tid

1. Notera en befintlig post och installera `SleepLog-v0.1.1.apk` över appen.
   **Avinstallera inte och rensa inte lagring.** Kontrollera att posten finns kvar.
2. Öppna datum respektive tid under både **Insomning** och **Uppvaknande**.
   Ändra och tryck **Välj**: rätt fält ska uppdateras och väljaren stängas.
   Öppna igen: det bekräftade värdet ska vara valt. Ändra och tryck **Avbryt**:
   fältet ska behålla sitt tidigare värde. Testa även bakåtknappen.
3. Spara `2026-11-01 23:15 → 2026-11-02 07:45`. Förhandsvisning och sparad post
   ska visa **8 tim 30 min** (dygnsbidrag 45 min respektive 7 tim 45 min).
   Öppna **Ändra**, ändra alla fyra fälten och spara. Samma post ska uppdateras.
4. Testa datumet för **Enbart längd**, både på en ny post och via **Ändra**.
   Exakta klockslag ska fortfarande saknas.
5. Prova telefonens stora skärm-/textstorlek, stående och liggande läge.
   **Välj** och **Avbryt** ska vara läsbara och nåbara. Prova även English:
   **Select/Cancel**, fortfarande 24-timmarsformat.
6. Stäng och öppna appen igen; kontrollera sparade värden. Skapa ett dokument
   och jämför tiderna med historiken. Lämna återkoppling om något döljs på din telefon.

## Grundfunktioner

Testa med påhittade poster nedan. Dessa datum gör resultaten enkla att jämföra.

1. Installera APK-filen och öppna **SleepLog**. Kontrollera att NoteWriter också finns kvar
   och går att öppna. Gränssnittet ska börja på svenska, med 24-timmarsklocka.
2. Registrera **2026-10-11 23:00 → 2026-10-12 07:30**. Passet ska vara **8 tim 30 min**.
   Dygnssummorna ska bli **11 oktober: 1 tim**, **12 oktober: 7 tim 30 min**.
3. Registrera **2026-10-12 14:00 → 14:30** och därefter **Enbart längd: 45 minuter** på
   samma datum. **12 oktober ska nu visa 8 tim 45 min**. Längdposten ska uttryckligen
   sakna exakta klockslag.
4. Ändra längdposten till **90 minuter**. Det ska fortfarande finnas tre poster och
   **12 oktober ska visa 9 tim 30 min**. Prova också att ändra ett datum/klockslag och
   kontrollera uppdateringen; återställ sedan testvärdena.
5. Tryck **Radera** på 30-minuterspasset och välj **Avbryt**: posten ska finnas kvar.
   Radera igen och bekräfta: två poster ska återstå och **12 oktober ska visa 9 tim**.
6. Byt till **English**, stäng appen och öppna igen. Språket och de två posterna ska finnas kvar.
   Byt tillbaka till svenska. Rotera skärmen medan du ändrar en osparad längd och kontrollera
   att utkastet bevaras; avbryt ändringen.
7. **Starta om telefonen.** Kontrollera att båda sparade posterna, värdena och dygnssummorna
   fortfarande finns. Detta är det verkliga omstartstestet, inte bara att stänga skärmen.
8. Välj **Skapa dokument → Standard**, spara i Dokument eller Hämtade filer och öppna filen
   i en annan app. Kontrollera att den innehåller två poster, dygnssummorna **1 tim** och
   **9 tim**, samt att 90-minutersposten saknar exakta klockslag.
9. Prova att avbryta filväljaren och sedan exportera igen. Prova engelska och kontrollera
   att även rapporttexten är engelsk. Ingen registrering ska påverkas av exporten.
10. Prova stort systemtypsnitt och en liten skärm: text och knappar ska gå att läsa och nå
    genom att rulla. Rapportera eventuella avklippta texter eller för trånga kontroller.

Tryck **Ändra** på en post och testa ett slut före starten eller längden 0 minuter:
appen ska visa ett felmeddelande och behålla det tidigare sparade värdet.

Radera testposterna med bekräftelse när du är klar. Avinstallera inte appen för att
uppdatera den om du vill behålla registreringarna; installera nästa APK över den befintliga.
