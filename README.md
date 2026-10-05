# HEAR Local Transcription

Android app scaffold for live, local-first microphone transcription with English and Tamil enabled first. The current build uses a local preview transcription engine so the app can be shipped and tested immediately; the real Sherpa-ONNX engine can be added back through a local AAR once that artifact is available.

## Privacy model

- Microphone audio is read from `AudioRecord` and handled only inside the app process.
- The current preview build does not upload audio and does not require network access.
- No audio upload API, analytics SDK, or remote transcription endpoint is included.
- The home-screen widget reads the latest transcript from private app storage.

## Build

Open this folder in Android Studio, let Gradle sync, then run the `app` target on an Android device.

## Language package

The **Download language package** button enables the local preview package in private app storage. When the real Sherpa-ONNX AAR is added, this flow can be switched back to downloading and validating model files.

## Widget

Add **HEAR Local Transcription** from Android's widget picker. It shows the last transcript text and opens the app when tapped.
