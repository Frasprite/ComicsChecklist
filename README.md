Comics Checklist
================

This project is my first Android application published on Google Play store.<br>
The idea behind this app is to provide a list of comics release (as far as possible): it simply scrape web pages for comics data.

You will find some code about listed libraries / framework / language:
* [Kotlin](https://kotlinlang.org/)
* [Room](https://developer.android.com/topic/libraries/architecture/room.html)
* [ViewModels](https://developer.android.com/reference/android/arch/lifecycle/ViewModel.html)
* [LiveData](https://developer.android.com/reference/android/arch/lifecycle/LiveData.html)
* [DataBinding](https://developer.android.com/topic/libraries/data-binding/index.html)
* [jsoup](https://jsoup.org/)
* [AppIntro](https://github.com/apl-devs/AppIntro)
* [Android-job](https://github.com/evernote/android-job)
* [Joda-Time](http://www.joda.org/joda-time/)
* Reactive Extensions: [RxJava](https://github.com/ReactiveX/RxJava) and [RxAndroid](https://github.com/ReactiveX/RxAndroid)
* [DatePicker Timeline](https://github.com/badoualy/datepicker-timeline)
* [Transitions Everywhere](https://github.com/andkulikov/Transitions-Everywhere)

### Features

The project contains two screens: one showing a list of items and a detail view.
The keys features are:

* Download data from web
* Show data on a list
* Persist data after app is closed

#### Android UI composition

The UI of the app is composed by:
* A main activity that handles navigation.
* A detail activity, which is used only if app is running on device with a screen minor then 600dp available width or in portrait.
* A fragment to display the list of items.
* A fragment to display item details.
* **NavigationDrawer** which show items available editors and options
* **BottomNavigationView** which show menu options on bottom
* **SwipeRefreshLayout** is used to launch a manual search
* **RecyclerView** as list display (coupled with Android Architecture above)
* **CardView** for a lovely sight and separation of content
* **SearchView** widget for fast search on DB

#### Back office

This is a description of detailed operation developed so far:
* The app use a service [IntentService](https://developer.android.com/reference/android/app/IntentService.html) to search data from the web.
* All data is stocked on SQL database (using *Room*, from [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/index.html)).

#### Pattern used so far
The app uses:
* Model-View-ViewModel (MVVM) pattern for the presentation layer: [MVVM](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel).
* [Repository pattern](https://medium.com/corebuild-software/android-repository-pattern-using-rx-room-bac6c65d7385), which handle all communications with DAO.

#### Google Play Store description (italian only)

Con **Comics Checklist** potete consultare sul vostro smartphone tutte le uscite mensili delle seguenti case editrici presenti in Italia:
- **Panini Comics** (che include **Marvel** e **Planet Manga**)
- **Star Comics**
- **Sergio Bonelli**
- **RW Edizioni**

Inoltre è possibile:
* annotare le uscite da comprare
* filtrare le serie che preferite
* inserire nel calendario un evento per ricordarvi l'uscita di un fumetto
* impostare un pro-memoria tramite notifica push

L'applicazione richiede solo una connessione alle reti mobili / Wi-Fi, ma soprattutto <i>non vi importuna con nessun banner pubblicitario</i>!

<u>Tutti i marchi, i loghi e i simboli presenti nell'applicazione sono dei rispettivi proprietari</u>.<br>
Purple Soc. Coop. non è in alcun modo affiliata ai siti utilizzati come feed di informazioni.<br>
L'applicazione è da intendersi come metodo aggiuntivo per la fruizione dei contenuti diffusi dai rispettivi proprietari.

Sviluppata da un'idea di Francesco Bevilacqua, fondatore e admin di [www.opengeek.it](http://www.opengeek.it).
