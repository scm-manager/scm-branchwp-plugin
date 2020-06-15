---
title: Konfiguration
---
Dazu muss im Repository unter "Einstellungen" im Tab "Branch-Berechtigungen" die Checkbox aktiviert werden. Danach kann nur noch anhand der definierten Regeln auf diesem Repository geschrieben werden.

![BranchWP Überblick](assets/overview.png)

### Berechtigung hinzufügen
Nach Aktivieren der Schreib-Berechtigungen wird der Bereich "Berechtigung hinzufügen" sichtbar. In ihm können Benutzer und/oder Gruppen für Branches berechtigt (oder ausgeschlossen) werden. Im Eingabefeld "Branch" kann ein Branch-Name oder Muster angegeben werden. Diese Muster können mit user, mail und username ersetzt werden.

Zum Beispiel:

* feature/*
* {mail}/*
* {username}/feature*
* {mail}
* {username}/*

Eine DENY Berechtigung wiegt stärker als eine ALLOW Berechtigung und kann diese außer Kraft setzen. Hierdurch kann nach einem generellen Berechtigen ein erneuter Ausschluss für bestimmter Teile ermöglicht werden.
