# ViDown
![Android](https://img.shields.io/badge/android-green)
![Kotlin](https://img.shields.io/badge/kotlin-grey)
![minSdk](https://img.shields.io/badge/minSdk-22-green)
![targetSdk](https://img.shields.io/badge/targetSdk-34-blue)

This is an android app built in using kotlin and following the recommended android best practices. The project is a simple app to download social media posts such as Instagram posts. The app currently 
supports downloading media from Youtube,Instagram and Tiktok. The main focus of this app is to learn how to download files from an url and save them in the app's storage. The download process is done using 
[WorkManager](https://developer.android.com/guide/background/persistent/getting-started) to run the process in the background, it supports the continuation of the download when the process stopped for 
some reasons.


## Overview
The app's UI is simple, it implements [Material Design 3](https://m3.material.io/) and uses  [the navigation component](https://developer.android.com/guide/navigation) to navigate between screens. 

![App](https://github.com/BBlueCoder/ViDown/blob/master/resources/app.gif)



## Tech-Stack
This app takes advantage of best practices and uses popular android libraries.

* Tech-Stack
  *  [Kotlin](https://developer.android.com/kotlin/first)
  *  [Coroutines](https://developer.android.com/kotlin/coroutines)
  *  [Flow](https://developer.android.com/kotlin/flow)
  *  [WorkManager](https://developer.android.com/guide/background/persistent/getting-started)
  *  [Navigation Component](https://developer.android.com/guide/navigation)
  *  [Material Design 3](https://m3.material.io/)
  *  [Paging 3](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)
  *  [Room](https://developer.android.com/training/data-storage/room)
  *  [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
  *  [Retrofit](https://square.github.io/retrofit/)
  *  [Glide](https://github.com/bumptech/glide)
  *  [ExoPlayer](https://github.com/google/ExoPlayer)
  *  [Ffmpeg kit](https://github.com/arthenica/ffmpeg-kit/tree/main/android)
  *  [YouDownloaderLib](https://github.com/RaIsseMa/YouDownloaderDemo)

## Architecture
ViDown follows the recommended Android architecture, employing industry best practices to provide a scalable and maintainable codebase. It uses a combination of Model-View-ViewModel (MVVM) architecture, 
dependency injection, and other architectural patterns to ensure the app's robustness and flexibility.

### App Structure 
![App Structure](https://github.com/BBlueCoder/ViDown/blob/master/resources/app_structure.png)

The app is separated into various packages to ensure the easiness for reaching a component.

### App Architecture 
The app implements MVVM architecture. The app has 3 layers, presentation layer, domain layer and data layer.

![App Acrhitecure](https://github.com/BBlueCoder/ViDown/blob/master/resources/layers.png)

### Database Scheme
![DB scheme](https://github.com/BBlueCoder/ViDown/blob/master/resources/db_scheme.png)

## Download Process
To download media, the app uses WorkManager to handle the background work. The process is simple, whenever the user chooses a media to download, it is added to the db with a PENDING state, the app checks 
if the WorkManager is already running or not, if not it launches the Worker. The WorkManager fetch the PENDING or INRPOGRESS media from the db and add it to a queue, then it iterates over the queue, download
each media until the queue is empty. The worker continuously update the downloading data of the current item, if the worker is stopped for some reasons, the next time the worker launched will continue downloading
the media from the point it was stopped.
![Download Process](https://github.com/BBlueCoder/ViDown/blob/master/resources/download_process_diagram.png)

## Feedback
We highly value your feedback as it helps us continually improve ViDown. Please don't hesitate to reach out to us with your comments, suggestions, or any issues you encounter while using the app.

## Contributing
Contributions are always welcome!
