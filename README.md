# MLwithTensorFlowLite

An Android application that combines on-device machine learning (TensorFlow Lite) with a Firebase-backed social/content experience — user accounts, a home feed, image galleries, comments, and result history.

## Features

- **On-device ML inference** using a bundled TensorFlow Lite model (`app/src/main/ml/model.tflite`)
- **User authentication** via Firebase Auth (Login / Register screens)
- **Cloud data** via Firebase Firestore and Firebase Functions
- **Image hosting/upload** via Cloudinary
- **Generative AI integration** (Firebase Generative AI SDK)
- **Social features**: posts, comments, and a home feed
- **History tracking** of past results/predictions
- **Onboarding**: Welcome and Tutorial screens for first-time users

<!-- TODO: Add a 2-3 sentence description of what the app actually predicts/does with the ML model, e.g. "Users take a photo of X, and the app classifies it as Y using an on-device TFLite model." -->

## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin, Java |
| ML | TensorFlow Lite |
| Backend | Firebase (Auth, Firestore, Functions, App Check) |
| Networking | Volley, OkHttp |
| Media | Cloudinary |
| Build | Gradle (Kotlin DSL), Version Catalog (`libs.versions.toml`) |

## Project Structure

```
MLwithTensorFlowLite/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mlwithtensorflowlite/   # Activities, adapters
│   │   │   ├── ml/model.tflite                            # TFLite model
│   │   │   └── res/                                        # Layouts, drawables
│   │   └── androidTest/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
└── settings.gradle.kts
```

### Key screens

- `WelcomeActivity` — onboarding entry point
- `LoginActivity` / `RegisterActivity` — authentication
- `HomeActivity` — main feed
- `MainActivity` — core ML feature screen
- `GalleryActivity` — image selection/browsing
- `TutorialActivity` — guided walkthrough
- `CommentsActivity` — post comments
- `ResultActivity` — ML inference results
- `InfoActivity` — app/model information


