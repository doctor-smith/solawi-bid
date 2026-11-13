# Dependency- und Distributions-Audit – Solawi Bid

**Stand:** 2026-11-12 · **Status:** technische Quellcode-Vorprüfung, kein vollständiger Runtime-/Artefakt-Audit.  
**Geprüfter Stand:** `doctor-smith/solawi-bid` `main` @ `8d8038ab6f9a8236b421441294d2801b0858a264`.  
**Links:** [GitHub Issue #15](https://github.com/doctor-smith/ai-command/issues/15), [Jira SMA-606](https://solawi-management.atlassian.net/browse/SMA-606), [Lizenz-/Contributor-Strategie](strategy-and-contributor-audit.md).

## Ergebnis

**Vor einer PolyForm-basierten Docker-/JAR-Distribution bleibt SMA-606 ein Blocker.** Der Backend-Build enthält `implementation(libs.mysql.connector.java)`; der Gradle-Katalog setzt `mysql:mysql-connector-java` auf **8.0.33**. Das Backend-Dockerfile baut mit `:solawi-bid-backend:buildFatJar` ein Fat-JAR und kopiert es als `/app/server.jar` in das Container-Image. Die **tatsächliche** Zusammensetzung des JAR/Images ist noch nicht durch einen Build geprüft.

Oracle/Maven Central führen Connector/J 8.0.33 unter **GPLv2 mit Universal FOSS Exception v1.0**. Die Universal FOSS Exception hilft nicht ohne Weiteres bei einer Distribution unter der geplanten PolyForm-Noncommercial-Lizenz (keine OSI-Open-Source-Lizenz). Eine verbindliche Entscheidung über konkrete Kombination, Linking und Vertrieb erfordert rechtliche Prüfung. Ein ausschließlich eigener SaaS-Betrieb **ohne Weitergabe** unterscheidet sich von Self-Hosting, Downloads und On-Premises.

## Erster Dependency-Bestand

| Komponente | Im Projekt gefunden | Lizenz / Risiko | Status |
| --- | --- | --- | --- |
| **MySQL Connector/J 8.0.33** | `gradle/libs.versions.toml`, `solawi-bid-backend/build.gradle.kts` | GPLv2 + Universal FOSS Exception; **kritisch bei Weitergabe** | Artifaktprüfung und SMA-606 offen |
| **H2 2.2.220** | Gradle-Katalog und Backend `implementation(libs.h2)` | Dual MPL 2.0 / EPL 1.0; notices und gewählte Lizenz prüfen | Offen |
| **Font Awesome Free 6.1.1** | `solawi-bid-frontend/src/main/resources/assets/fontawesome/` | Icons CC BY 4.0, Fonts SIL OFL 1.1, Code MIT | `LICENSE.txt` ist vorhanden, mitzuliefern und Attribution prüfen |
| **Lets-Plot Kotlin/JS 4.10.0** | Frontend `implementation("org.jetbrains.lets-plot:lets-plot-kotlin-js:4.10.0")`; Evaluationskomponente importiert Lets-Plot | tatsächlich aufgelöste Module und Lizenzen prüfen | Derzeit **noch im Code**, Entfernung geplant |
| **Chart.js** | In der aktuellen Quellcode-Prüfung nicht nachgewiesen | Geplant; MIT-Lizenz nach Aufnahme mitliefern | **Noch nicht** als Ist-Dependency zählen |
| **Weitere JVM- und Kotlin-Dependencies** | Ktor, Exposed, Logging, JWT, Playwright u. a. | Lizenz hängt von aufgelösten Artefakten/Versionen ab | Transitive Runtime- und Testauflösung ausstehend |
| **npm/Webpack/JS-Bundles** | Kotlin/JS-/Webpack-Build | Kein vollständiger npm-/Bundle-Audit aus Source-Manifests ableitbar | Tatsächlich ausgelieferte Bundles und Transitives untersuchen |
| **Container-Basisimages und Frontend-Assets** | Dockerfiles, Font-Awesome-Ressourcen | Separate Distribution und Hinweise | Image-/Asset-Inventar offen |

Quellcodefunde sind **keine** vollständige Stückliste. Insbesondere die transitive Auflösung, mitgelieferte Assets, generierte JavaScript-Bundles und Basisimages fehlen noch im geprüften Bestand.

## Optionen für SMA-606

| Variante | Vorteil | Notwendige Prüfung |
| --- | --- | --- |
| **SaaS-only** | Keine kundenseitige Backend-Weitergabe | Deployment- und Kundenmodell nachweisen; gesonderte Rechtsprüfung |
| **MySQL-Connector nicht im Fat-JAR bündeln** | Entflechtung möglich | Classpath, externe Treiberlieferung, Docker-Vertrieb und rechtliche Kopplung prüfen |
| **MariaDB Connector/J** | Laut MariaDB unter LGPL v2.1 oder später | MySQL-Version, JDBC-URLs/Driver, Exposed/Migrationen, Pooling, Integrationstests und LGPL-Redistributionspflichten prüfen |
| **Kommerzielle Oracle-Lizenz** | Möglicher proprietärer Distributionsweg | Vertrag, Umfang, Version, Artefakte und Vertriebskanäle bestätigen |
| **Anderer Treiber / andere Datenbank** | Technische Entkopplung | Kompatibilität, Migrationsaufwand und neue Lizenzen prüfen |

**Empfehlung für technische Evaluation:** MariaDB Connector/J als getesteten Prototypen mit Oracle-Vertragsoption vergleichen. Noch **keinen** Treiberwechsel ohne erfolgreiche DB-Tests beschließen. Die Entscheidung ist abhängig vom Zielmarkt (SaaS vs. Self-Hosting).

## Reproduzierbare Prüfungen (noch NICHT durchgeführt)

```bash
# Exakte aufgelöste Backend-Runtime und Treiber-Herkunft
./gradlew :solawi-bid-backend:dependencies --configuration runtimeClasspath
./gradlew :solawi-bid-backend:dependencyInsight --configuration runtimeClasspath --dependency mysql-connector-java

# Erzeugtes Fat-JAR prüfen
./gradlew :solawi-bid-backend:buildFatJar
find solawi-bid-backend/build/libs -type f -name '*-all.jar' -print
# <JAR> durch tatsächlichen Dateinamen aus der vorigen Ausgabe ersetzen:
jar tf <JAR> | grep -E '^com/mysql/cj/jdbc/Driver.class$|^com/mysql/cj/'

# Docker-Image aufbauen und geliefertes JAR extern prüfen
docker build -f infrastructure/docker/Dockerfile-Backend -t solawi-bid-backend:license-audit .
container_id=$(docker create solawi-bid-backend:license-audit)
docker cp "$container_id":/app/server.jar ./server-from-image.jar
docker rm "$container_id"
jar tf ./server-from-image.jar | grep -E '^com/mysql/cj/jdbc/Driver.class$|^com/mysql/cj/'
```

Falls `dependencyInsight` den historischen Maven-Koordinatennamen nicht findet, nach `mysql-connector-j` und `com.mysql` suchen; Relocation dokumentieren. **Build-Ausgabe, Hashes, JAR-Inhaltsliste und den jeweiligen Exit-Status** festhalten. Ein einzelner fehlgeschlagener Grep ist kein belastbarer Negativnachweis.

Danach Gradle/JVM (Runtime, Test, Build), Kotlin/JS, npm/webpack, statische JS-/Icon-/Font-Dateien und Container-Basisimages inventarisieren. Eine maschinenlesbare SBOM (SPDX oder CycloneDX) und final geprüfte `THIRD_PARTY_NOTICES` müssen aus **realen** aufgelösten/ausgelieferten Komponenten entstehen. Ergebnisse manuell gegen Originallizenzen prüfen.

## Gate vor Lizenzumstellung

- [ ] Alle Distributionskanäle und Empfänger konkret dokumentiert.
- [ ] Runtime-Abhängigkeitsgraph mit Versionen und Lizenztexten ermittelt.
- [ ] Fat-JAR und Docker-JAR auf Connector/J geprüft.
- [ ] SMA-606-Option rechtlich/technisch entschieden, umgesetzt und DB-getestet.
- [ ] Gegebenenfalls LGPL-Vorgaben beim MariaDB-Connector eingehalten (Hinweise, Bibliotheksaustausch bzw. sonstige Packaging-Pflichten überprüfen).
- [ ] Client-Bundles, Icons, Fonts, Images, sonstige Assets und indirekte Dependencies erfasst.
- [ ] `THIRD_PARTY_NOTICES` mit erforderlichen Lizenztexten/Attributionen erstellt.
- [ ] Keine Änderung der Haupt-`LICENSE`, bis Contributor-Bereinigung und technische Freigaben abgeschlossen sind.

## Quellen

- [Maven Central: MySQL Connector/J 8.0.33 und Lizenzmetadaten](https://central.sonatype.com/artifact/com.mysql/mysql-connector-j/8.0.33)
- [Oracle: Lizenzierungsoptionen für MySQL-Connectoren](https://www.mysql.com/about/legal/licensing/oem/)
- [MariaDB Connector/J: Lizenz und Kompatibilitätsinformationen](https://mariadb.com/docs/connectors/mariadb-connector-j/about-mariadb-connector-j)
- [Font Awesome: eingecheckte Lizenz](../../solawi-bid-frontend/src/main/resources/assets/fontawesome/LICENSE.txt)
- [PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0)

