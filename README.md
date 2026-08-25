# SkateTerrain

> ## ⚠️ Deprecated - not maintained
> This is an old school project, kept online for archival purposes only.
> It is **no longer maintained**, receives no updates or security fixes, and
> the credentials it once used have been revoked. Do not use it in production.
> Issues and pull requests will not be reviewed.

An Android app for mapping skate spots. You browse a Google Map, drop a marker on
a spot, give it a type (park, ledge, rail, stair, variety), a description and a
photo, and it shows up in a list next to the map.

Built as a student project in 2019-2021 with Java and the Google Maps Android SDK.

## Features

- Google Maps view with your current location
- Add a spot by placing a marker, with type, description and a photo from storage
- List of saved spots next to the map; tapping one centers the map on it
- Spots are stored locally on the device (SharedPreferences + Gson)

## Tech stack

- Java, Android SDK (min 23, target 28)
- Google Play Services Maps
- Firebase (Realtime Database / Storage / Auth dependencies were wired in but the
  app never really used them)
- Gradle 3.4.1 / the old `com.android.support` libraries

Because of the ancient Gradle and Android Gradle Plugin versions, this will not
build on a current Android Studio without upgrading the build files first.

## Running it yourself

The project needs two credential files that are **not** in this repository
(and are listed in `.gitignore`). Templates live in `config-templates/`.

1. **Google Maps API key** - create one in the
   [Google Cloud console](https://developers.google.com/maps/documentation/android-sdk/get-api-key),
   restricted to the Android package name and your signing certificate:

   ```bash
   cp config-templates/google_maps_api.example.xml app/src/debug/res/values/google_maps_api.xml
   ```

   Then replace `YOUR_GOOGLE_MAPS_API_KEY` with your key. Do the same for
   `app/src/release/res/values/google_maps_api.xml` if you make a release build.

2. **Firebase config** - create a Firebase project, register an Android app with
   package name `com.example.skateterrain`, and download its `google-services.json`
   into `app/`. `config-templates/google-services.example.json` shows the shape of
   the file.

3. Build and run:

   ```bash
   ./gradlew assembleDebug
   ```

## A note on secrets

An earlier version of this repository had a Google Maps API key and a Firebase
`google-services.json` committed to it. Those files have been purged from the
entire git history and the keys they contained have been revoked, so the values
you may still find in old forks or clones are dead.

If you fork this: never commit `google_maps_api.xml`, `google-services.json`,
`local.properties` or any keystore. Always restrict Google API keys by package
name and SHA-1 fingerprint in the Cloud console, and lock down your Firebase
security rules - client-side API keys in an Android app are extractable from the
APK, so restrictions and rules are what actually protect you.
