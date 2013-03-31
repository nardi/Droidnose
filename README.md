Oke, hier zijn een paar dingen die je moet weten om hier wat van te begrijpen:

Ik doe alles in Java, ook GUI-dingen die je normaal gezien in XML zou doen.
Dit omdat ik het in eerste instantie schreef voor Inleiding Programmeren, en dacht
dat het leuk zou zijn om zoveel mogelijk in Java te doen. De layout-code enzo is
dus een beetje een zooitje (omdat je een beetje chaotisch dingen doet om het er
goed uit te laten zien), maar ik ben er wel een beetje aan gewend nu.

Voor datum- en tijd-gerelateerde dingen gebruik ik m'n eigen wrapper om de standaard
Java klassen, weer omdat het leuk was voor Inleiding Programmeren, maar ook omdat
de standaard Java Date en Calendar enzo echt niet mee om te gaan zijn. Ik was al
eens begonnen met het te vervangen door Joda-time, maar dat bleek lastiger dan verwacht.
Gelukkig is m'n eigen JSON-parser al vervangen door de standaard meegeleverde :P

Na een tijdje begon ik steeds meer dingen asynchroon te moeten doen, waardoor de
algemene program-flow een beetje vreemd is op sommige plekken. Omdat asynchroon
programmeren in Java niet erg leuk is heb ik (o.a.) een Callback-klasse toegevoegd
die best wel goed werkt, maar eigenlijk moet ik nog eens een heleboel omschrijven
om deze te gebruiken, om het wat netter te maken. Ook vindt de netwerkactiviteit
nu plaats in één locatie, en hoeft dus eigenlijk alleen dat in een aparte thread
uitgevoerd te worden. Vroeger was dit niet zo, waar nog genoeg overblijfselen van
te vinden zijn.

TimetableActivity is nu redelijk StudentTimetable-specifiek, om andere roosters
toe te voegen moet eigenlijk alleen deze opgesplitst te worden, dingen als Timetable
en de UI-elementen kunnen gewoon herbruikt worden (hoewel met een nieuwe API
Timetable waarschijnlijk ook wat aangepast moet worden).