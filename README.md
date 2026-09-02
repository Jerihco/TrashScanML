# TrashScanML

**TrashScan** is an Android application that uses a Convolutional Neural Network (CNN), deployed on-device via TensorFlow Lite, to classify waste in real time from a phone camera. It identifies waste as **plastic, glass, metal, paper, or cardboard**, then provides disposal guidance and recycling tips — with gamified community features to encourage continued use.

## Features

- **On-device ML inference** — a CNN model (trained on TrashNet plus locally collected images from junkshops in San Pedro, Laguna) bundled as TensorFlow Lite (`app/src/main/ml/model.tflite`), classifying waste into plastic, glass, metal, paper, or cardboard
- **AI-generated disposal guidance** via the **Gemini API**, including recycling tips, recycling rate, and decomposition time for the detected material
- **Camera & Gallery scanning** — scan waste live or classify a photo from the gallery
- **User authentication** via Firebase Auth (Login / Register)
- **Cloud data** via Firebase Firestore and Firebase Functions
- **App security** via Firebase App Check
- **Image hosting/upload** via Cloudinary
- **Gamified engagement**: achievement badges, progress/usage statistics, and a community feed with comments to encourage continued participation
- **History tracking** of previously scanned items
- **Onboarding**: Welcome and Tutorial screens for first-time users
- **Info tab** with educational content on each waste category and proper disposal methods


## Tech Stack

| Category | Technology |
|---|---|
| Language | Kotlin, Java |
| ML | TensorFlow Lite (CNN, trained on TrashNet + local dataset) |
| Generative AI | Gemini API (via Firebase Generative AI SDK) |
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

- `WelcomeActivity` — introduces TrashScan with a "Get Started" button
- `TutorialActivity` — step-by-step guide on how to use the app
- `LoginActivity` / `RegisterActivity` — authentication
- `HomeActivity` — main dashboard with "Use Camera" and "View Gallery" actions, plus nav bar (Home, Camera, Gallery, Info)
- `MainActivity` — core ML feature screen (camera capture)
- `GalleryActivity` — displays previously captured/scanned images
- `ResultActivity` — shows detected waste type, description, disposal instructions, recycling tips, recycling rate, and decomposition time
- `CommentsActivity` — community feed / comments on posts
- `InfoActivity` — educational content on each waste category (Paper, Plastic, Metal, Glass, etc.)

### User roles

- **General Users** — scan and identify waste for proper disposal
- **Waste Personnel** — involved in waste collection, sorting, and disposal; help validate and improve the system

## Model

- **Architecture:** Convolutional Neural Network (CNN)
- **Classes:** plastic, glass, metal, paper, cardboard
- **Training data:** [TrashNet](https://github.com/garythung/trashnet) dataset, augmented with locally collected images from junkshops in San Pedro, Laguna, to better reflect waste commonly found in the Philippines
- **Reference benchmarks from related literature:** CNN-based waste classifiers in prior studies report accuracy in the 80–99% range depending on dataset and architecture (e.g. Sami et al., 2020 — 90%; Bobulski & Kubanek — 99.92% on a 4-category plastic subset)

