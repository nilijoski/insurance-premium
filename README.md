![Build Status](https://github.com/nilijoski/insurance-premium/actions/workflows/maven.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21%2B-orange)

# Versicherungsbeitragsrechner

Die Anwendung berechnet die Versicherungsprämie basierend auf **jährlicher Kilometerleistung**, **Fahrzeugtyp** und dem **Bundesland der Zulassung**.  
Das Bundesland wird anhand der eingegebenen Postleitzahl ermittelt.

---

## Designentscheidungen und zukünftige Verbesserungen

In meiner Lösung habe ich mich entschieden, eine zusätzliche Datenbanktabelle anzulegen, 
in der alle Postleitzahlen ihrem jeweiligen Bundesland zugeordnet werden. Diese Tabelle ist zwar nicht zwingend erforderlich, 
ermöglicht jedoch eine deutlich effizientere Ermittlung des Bundeslandes: Anstatt bei jeder Eingabe erneut die CSV-Datei 
zu durchsuchen, genügt ein direkter Datenbankzugriff. Die CSV wird lediglich einmal zu Beginn der Anwendung geladen und danach nicht mehr verwendet.

Die Faktoren für die Bundesländer habe ich aufgrund der kurzen Entwicklungszeit frei definiert. Eine bevorzugte Lösung wäre es jedoch, 
eine CSV-Datei mit den tatsächlichen Regionalklassen – beispielsweise von der ADAC-Webseite – zu erstellen. 
Für jedes Bundesland könnte dann ein Durchschnittswert über alle Bezirke berechnet und gespeichert werden. 
Damit wäre der Faktor pro Bundesland, wie in den Anforderungen vorgesehen, einheitlich und gleichzeitig näher an der Realität. 
Noch genauer wäre es, direkt mit individuellen Faktoren auf Bezirksebene zu arbeiten, anstatt Durchschnittswerte zu verwenden.

Da der Schwerpunkt meiner Arbeit auf dem Backend lag, wollte ich dennoch eine attraktive Benutzeroberfläche bereitstellen. 
Die GUI habe ich daher größtenteils mithilfe von KI generiert und anschließend an die Anforderungen angepasst.

---

## Funktionsweise

Zum Starten der App wird mindestens **Java 21** (JDK oder JRE) auf dem lokalen Rechner benötigt.  
Mit Maven wird ein sogenanntes **Fat-Jar** erzeugt, das die gesamte Anwendung samt aller Abhängigkeiten enthält.  
Dadurch kann die App direkt über die Kommandozeile gestartet werden:

java -jar insurance-premium-1.0-SNAPSHOT-shaded.jar

Nach dem Start läuft ein eingebetteter HTTP-Server auf Port 8080.  
Das Vorgehen zur Nutzung:

1. Der Nutzer öffnet im Browser die Adresse: http://localhost:8080.
2. Er wählt:
    - Kilometer pro Jahr (Slider),
    - Fahrzeugtyp (Dropdown),
    - Postleitzahl (Textfeld).
3. Die Anwendung prüft über /resolve-postcode, ob die Postleitzahl einem Bundesland zugeordnet werden kann:
    - Wenn ja: Bundesland und Faktor werden angezeigt, der Button „Berechnen“ wird freigegeben.
    - Wenn nein: Der Button bleibt deaktiviert, und ein Hinweis erscheint.
4. Klickt der Nutzer auf „Berechnen“:
    - Das Frontend sendet einen POST /calculate Request mit JSON-Daten.
    - Das Backend validiert die Postleitzahl und berechnet den Beitrag nach folgender Formel:

      Beitrag = Kilometerfaktor × Fahrzeugfaktor × Bundeslandfaktor

    - Das Ergebnis wird als JSON zurückgegeben und im Browser angezeigt.
5. Jede gültige Berechnung über die Web-Oberfläche wird in einer H2-Datenbank gespeichert.  
   Über die API durchgeführte Berechnungen werden nicht gespeichert.

---

## API-Endpunkte

Es gibt zwei API-Endpunkte:

1. /resolve-postcode  
   Bestimmt das Bundesland zur angegebenen Postleitzahl.  
   Beispiel:

   curl "http://localhost:8080/resolve-postcode?pc=50667"

   Beispiel-Antwort:
   {
   "found": true,
   "state": "Nordrhein-Westfalen",
   "stateFactor": 1.15
   }

2. /calculate  
   Berechnet die Versicherungsprämie anhand der Eingaben.  
   Beispiel:

   curl -X POST "http://localhost:8080/calculate"      -H "Content-Type: application/json"      -d '{
   "kilometers": 12000,
   "vehicleType": "suv",
   "postcode": "50667"
   }'

   Beispiel-Antwort:
   {
   "premium": 2.07,
   "state": "Nordrhein-Westfalen",
   "kilometerFactor": 1.5,
   "vehicleFactor": 1.2,
   "stateFactor": 1.15,
   "inputKilometers": 12000,
   "inputVehicleType": "suv",
   "inputPostcode": "50667"
   }

---

## Kommunikation der Services

Die Services innerhalb der Anwendung kommunizieren synchron innerhalb der JVM.  
Die Aufrufkette sieht wie folgt aus:

- Controller → Service → DAO
    - PremiumController nimmt HTTP-Anfragen entgegen,
    - ruft PremiumService auf, um den Beitrag zu berechnen,
    - ruft ApplicationService auf, um das Ergebnis zu speichern,
    - nutzt DAOs (ApplicationDAO, PostcodeRegionDAO) für den Datenbankzugriff.

Datenfluss:
- Eingaben (JSON) werden in Java-Objekte konvertiert.
- Services berechnen und validieren die Daten.
- DAOs speichern Daten per Hibernate in der Datenbank.
- Ergebnisse werden wieder als JSON an den Client zurückgegeben.

Fehlerbehandlung:
- Ist keine Zuordnung der Postleitzahl möglich, wird kein Bundesland geliefert und der Controller gibt HTTP 400 zurück.

---

## Architektur

Die Anwendung folgt einem klaren Schichtenmodell:

1. Controller-Layer
    - Enthält die Jetty-Servlets (PremiumController, ResolvePostcodeController).
    - Bereitstellung der Endpunkte /calculate, /resolve-postcode und der statischen HTML-Dateien.

2. Service-Layer
    - PremiumService: Fachlogik zur Berechnung der Faktoren und Beiträge.
    - ApplicationService: Verwaltung und Speicherung von Berechnungen.

3. DAO-Layer
    - Verantwortlich für den Datenbankzugriff über Hibernate.
    - ApplicationDAO und PostcodeRegionDAO.

4. Model-Layer
    - Entities: Application, PostcodeRegion.
    - DTOs: CalculationResponse, ErrorResponse.

5. Utility-Layer
    - PostcodeCsvImporter: Importiert Postleitzahlen und Bundesländer aus der CSV beim Start.
    - HibernateUtil: Initialisiert die Hibernate SessionFactory.

---

## Eingesetzte Technologien

Jetty (Embedded Webserver)
- Läuft als eingebetteter HTTP-Server.
- Stellt sowohl die REST-API als auch die Weboberfläche zur Verfügung.

Hibernate (ORM)
- Übernimmt die Abbildung von Java-Objekten auf Datenbanktabellen.
- Spart manuelle SQL-Abfragen und verwaltet Transaktionen.

H2 (Embedded Database)
- Eine leichtgewichtige relationale Datenbank, die eingebettet läuft.
- Alle Daten werden in einer lokalen Datei gespeichert (insurance-premium-db.mv.db).
- Kein separater DB-Server notwendig.

Jackson
- Wandelt JSON-Daten in Java-Objekte und umgekehrt.
- Erleichtert die Kommunikation zwischen Frontend und Backend.

Maven
- Build- und Dependency-Management-Tool.
- Lädt automatisch alle benötigten Bibliotheken (Jetty, Hibernate, Jackson, JUnit).

Fat-Jar
- Ein ausführbares JAR, das alle Klassen und Abhängigkeiten enthält.
- Vorteil: Die Anwendung kann mit einem einzigen Befehl gestartet werden:
  java -jar insurance-premium-1.0-SNAPSHOT-shaded.jar

---