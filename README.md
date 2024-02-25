# WeatherApp
This is a simple weather application with sign up and sign in feature. It uses a network based provider to fetch user's location that will be used to determine the weather in the user's current location.

* Firebase Auth is used for Sign up and sign in feature.
* Room and Sqlite for data persistence.
* Location Manager to fetch current location.
* Koin for managing dependencies.
* Retrofit and Gson for API calls and Serialization
* Mockk is used for mocking classes for unit test.
* MVVM is the architecture used which also applies one-way data flow on the redux pattern.
* LiveData and Flow is used to manage states and events.
* OpenWeather API is used to determine the current weather.
* Glide is used to fetch and cache images.

## How to set API KEY

* On the root directory, Create a build.properties file.
* Inside the file add this line ```api_key=\"<api_key>\"```
* Note: Ensure to put the api key between the two \\" since it will not build if the syntax is incorrect.
* Run clean project and rebuild project to ensure BuildConfig is updated with API Key.

Project was build using 'Android Studio Hedgehog | 2023.1.1 Patch 2'
