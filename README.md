# BLUETHINGS
BlueThings Project - Android Mobile Application based on Bluetooth technology.

L’applicazione (Progetto Bluethings) consiste in una piattaforma-adattatore per 
applicazioni che utilizzano la tecnologia Bluetooth sui dispositivi Android, sfruttando le API offerte da 
Android Developers.
Utilizzando il Design Pattern Adapter importato dall’ingegneria del software, di fatto, è stato possibile creare 
una piattaforma Software su cui agganciare Attività differenti, gestibili secondo la logica dei Fragments a 
livello implementativo.
Su questa piattaforma, sono state realizzate tre Attività, che sfruttano la tecnologia bluetooth per la
comunicazione con dispositivi remoti:

▪ Chat Activity:
Attività di chat-bluetooth con un dispositivo remoto. Questo applicativo permette di conversare 
attraverso il bluetooth con utenti di altri dispositivi, mantenendo in persistenza le conversazioni.

▪ Picture-Chat Activity:
Attività di scambio di immagini con un dispositivo remoto. Questo applicativo permette di scambiare 
immagini, attraverso il bluetooth, accedendo alla galleria del proprio dispositivo, e di salvare in 
persistenza le immagini preferite provenienti dai dispositivi remoti.

▪ Walkie Talkie Activity:

Attività di scambio di registrazioni audio. Questo applicativo permette di scambiare registrazioni audio 
tra dispositivi remoti, attraverso il bluetooth. Gli audio vengono direttamente registrati tramite 
l’interazione con l’interfaccia utente (utilizzo di Buttons/Image Buttons).
Per tutte le attività, è stato implementato un algoritmo di trasferimento dati comune che fa uso delle API 
Android BluetoothConnectionServer e di Broadcast Receiver, algoritmo che trasforma i tipi di dato in ingresso 
in sequenze di byte, e li invia tramite interfacce di I/O (bluetooth socket).
L’applicazione piattaforma prevede inoltre il salvataggio persistente di una lista di contatti, la possibilità di 
scelta di un avatar e nicknames da associare al proprio account, o a quello di dispositivi remoti.
La persistenza è implementata tramite la classe SQLiteOpenHelper.
