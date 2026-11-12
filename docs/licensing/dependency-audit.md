# Dependency- und Distributions-Audit – Solawi Bid

**Stand:** 2026-11-12 · **Status:** nachvollziehbare Quellcode-Vorprüfung, **kein vollständiger SBOM-/Artefakt-Audit**  
**Referenzcode:** `doctor-smith/solawi-bid` `main` @ `8d8038ab6f9a8236b421441294d2801b0858a264`  
**Abhängigkeiten:** [GitHub Issue #15](https://github.com/doctor-smith/ai-command/issues/15), [Jira SMA-606](https://solawi-management.atlassian.net/browse/SMA-606), [Lizenz- und Contributor-Strategie](strategy-and-contributor-audit.md).

## 1. Zusammenfassung

**Kritischer Distributions-Blocker:** Das Backend bindet im Gradle-Katalog `mysql:mysql-connector-java` **8.0.33** ein (`implementation(libs.mysql.connector.java)`). Das Backend-Dockerfile ruft `:solawi-bid-backend:buildFatJar` auf und übernimmt `build/libs
