# Solyton MySQL-Dump-Pseudonymisierer (lokales CLI)

Kleines Python-3.10+-Programm ohne zusätzliche Python-Pakete. Transformiert `mysqldump`-Dateien (`.sql` oder `.sql.gz`) und erhält Tabellen-/Spaltenstruktur, Fremdschlüssel und fachliche Zahlenwerte. Für den Solyton-Dev-Dump ist `solyton-config.json` beigefügt.

**Achtung:** Dies ist **Pseudonymisierung**, keine garantiert irreversible Anonymisierung. Insbesondere bleiben interne `BINARY(16)`-IDs und Zeitstempel bestehen, um Zusammenhänge und fachliche Debugging-Szenarien zu erhalten. Die anonymisierte Ausgabe darf **nicht ungeprüft veröffentlicht** werden. Original-Dumps und lokale Ausgaben enthalten möglicherweise personenbezogene Daten; nicht in Git committen oder unkontrolliert an Dritte verteilen.

## Verwendung

```
python3 anonymize_mysql.py /pfad/test-dump.sql --config solyton-config.json --audit
```

Das erste Kommando zeigt nur Tabellen, klassifizierte Spalten und ungeprüfte Textfelder an, **keine Nutzdaten**. Die Verarbeitung stoppt bei nicht erkannten, nichtleeren Textfeldern, bis in der Konfiguration eine Behandlung oder begründete Freigabe gesetzt ist.

```
export ANONYMIZER_KEY="$(openssl rand -hex 32)"   # optional, für wiederholbare Zuordnung über Läufe
python3 anonymize_mysql.py /pfad/test-dump.sql \
  --config solyton-config.json \
  --output /pfad/test-dump-anonymized.sql
```

Ohne `ANONYMIZER_KEY` wird pro Durchlauf ein zufälliger Schlüssel erzeugt: Die Zuordnungen bleiben **innerhalb desselben Durchlaufs** konsistent, sind aber zwischen zwei Läufen verschieden. Den Schlüssel nicht mit dem Dump teilen und nicht in Git speichern. Bei identischem Schlüssel + identischer Eingabe entstehen dieselben Pseudonyme.

Die Quelldatei wird **nie überschrieben**. Vorhandene Zieldateien werden ebenfalls nicht überschrieben. Die Ausgabedatei wird mit Zugriffsrechten `0600` angelegt (soweit vom Betriebssystem unterstützt). Ein Fehler führt nicht zu einer teilweise veröffentlichten Zieldatei.

## Was wird ersetzt?

- `users.username` → `user-1@solyton.org`, `user-2@solyton.org`, ... (für `BINARY(16)`-User-IDs; numerische IDs behalten ihre Zahl im Alias). Identische Login-/E-Mail-Werte werden auch in anderen Tabellen konsistent ersetzt.
- E-Mail-Adressen in weiteren Feldern → deterministisch generierte Adressen mit `@solyton.org`.
- Bankkonten: IBAN → DE-Testwert mit gültiger Prüfziffer, nicht routbare BLZ `00000000`; BIC → syntaktischer Testwert; Gläubiger-ID → Prüfziffer-konsistenter Testwert. **Nicht für reale Zahlungen verwenden.**
- Profil: Vor-/Nachname, Kontoinhaber, Adressen, Telefon, IP, URLs etc. → Kunstwerte.
- Externe `webling_id` → stabile Ersatznummer, damit tabellenübergreifende Beziehungen erhalten bleiben (0 wird als Sentinel nicht ersetzt).
- Tokens, Passwörter, Notizen, Freitexte und potentiell sensible SEPA-Zwecke → unbrauchbar gemacht oder redigiert.
- SEPA-Mandats- und Nachrichtenreferenzen → stabile eindeutige Testreferenzen statt einheitlichem `[REDACTED]`, damit UNIQUE-Constraints möglichst erhalten bleiben.
- Binary IDs und FKs (`BINARY(16)`) bleiben **unverändert**, damit Verweise in der Anwendung funktionieren; das ist bewusst ein Datenschutz-Rest-Risiko.

Das vorhandene `solyton-config.json` wurde gegen das bereitgestellte **Dev-Dump-Schema mit 61 Tabellen** geprüft. Bei neuen Tabellen/Spalten zuerst Audit durchführen und Regeln prüfen. `allow_columns` heißt ausdrücklich: **Wert bleibt erhalten**. Freigaben gehören in den Code-Review.

## Grenzen und Tests

Unterstützt die übliche mysqldump-Struktur mit `CREATE TABLE` und `INSERT INTO ... VALUES`, auch mit Binär-UUIDs als `_binary '...'`, wobei Nicht-UTF8-Bytes verlustfrei erhalten bleiben. Nicht unterstützte Daten-SQL-Formen (etwa `UPDATE`, Trigger, Stored Procedures) werden aus Sicherheitsgründen abgelehnt. Große Dumps sollten mit `--skip-extended-insert` exportiert werden, falls ein einzelnes Statement über 128 MiB groß wird.

```
python3 -m unittest -v test_anonymize_mysql.py
```

Vor dem Einsatz durch Entwickler: **lokal in eine isolierte Testdatenbank importieren**, Anwendung starten, UNIQUE-/FK-Constraints und Fachprozesse prüfen. Der erfolgreiche SQL-Text-Durchlauf ersetzt **keinen MySQL-Importtest**. Sonderzeichen, freie Texte, Namen in untypischen Fachspalten und personenbezogene Informationen in Zeitstempeln müssen vor Weitergabe bewertet werden. Dies ist keine Rechtsberatung oder Datenschutz-Zertifizierung.
