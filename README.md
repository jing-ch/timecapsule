# Time Capsule

> An Android app for locking away messages, photos, and videos until a future date you pick, then getting a notification the moment they unlock.

<!-- Badges -->
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Java-007396?logo=openjdk&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)
![Backend](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)
![minSDK](https://img.shields.io/badge/minSDK-27-green)

Time Capsule lets you bundle text, images, and videos into a capsule and seal it with a future unlock date. Until that date arrives the capsule stays hidden and shows only a live countdown. When the time comes, a server-driven push notification delivers it straight to your phone. You can keep a capsule private or share it with friends to open together later.

---

## Demo / Screenshots

**▶ [Watch the demo video](https://youtu.be/qCn4zFBbpJ4)**

<!-- TODO: Add your screenshots / GIF here. Suggested captures:
     1. Capsule list with locked + unlocked cards and the search/filter bar
     2. The 3-step create flow
     3. A locked capsule showing the live countdown
     4. An unlocked capsule detail view with media
     5. Friends / sharing screen
-->

| Capsule List | Create Flow | Locked Countdown | Unlocked Detail |
| :---: | :---: | :---: | :---: |
| _screenshot_ | _screenshot_ | _screenshot_ | _screenshot_ |

---

## Features

- **Authentication.** Email/password sign-up and login via Firebase Authentication, with a required display name (nickname) set on first login.
- **3-step capsule creation.** A guided flow with a progress indicator: media + title, then message + location, then unlock time + privacy.
- **Photos & videos.** Attach multiple images and videos, uploaded to Firebase Storage. Images respect EXIF rotation and videos render an extracted first-frame thumbnail.
- **Time-locked capsules.** Locked capsules hide their contents and display a **live countdown** that updates every second until the unlock moment.
- **Scheduled unlock notifications.** A Cloud Function polls every minute and sends an FCM push the instant a capsule unlocks, with a tap that deep-links straight to the capsule.
- **Friend sharing.** Add friends by email, then share "public" capsules with selected friends. Recipients see them in their own list with a *Shared* badge.
- **Location tagging.** Auto-capture the current location (reverse-geocoded to a place name) via Fused Location Provider, or type a location manually.
- **Search & filter.** Real-time title search plus All / Locked / Unlocked filter chips over a list sorted newest-first.
- **Safe deletion.** Owner-only deletion with confirmation. Recipients of shared capsules cannot delete them.

---

## Tech Stack

| Layer | Technology |
| --- | --- |
| **Language** | Java 11 |
| **UI** | XML layouts, Material Design Components, ViewBinding, Lottie animations |
| **Architecture** | MVVM (ViewModel + LiveData) with a Repository layer |
| **Auth** | Firebase Authentication |
| **Database** | Cloud Firestore |
| **Media Storage** | Firebase Cloud Storage |
| **Push Notifications** | Firebase Cloud Messaging (FCM) + Cloud Functions (Node.js) |
| **Location** | Google Play Services (FusedLocationProviderClient + Geocoder) |
| **Image Loading** | Picasso |
| **Build** | Gradle (Kotlin DSL), Android Gradle Plugin 9.0.1 |

**SDK targets:** `minSdk 27` · `targetSdk 36` · `compileSdk 36`

---

## Architecture

The app follows the **MVVM** pattern with a single source of truth for data access in the repository layer. The UI observes `LiveData` exposed by ViewModels and never talks to Firebase directly.

```
┌──────────────┐     observes      ┌──────────────┐     calls      ┌──────────────────┐
│   View       │ ◀──────────────── │  ViewModel   │ ─────────────▶ │   Repository     │
│ Activities / │   (LiveData)      │  (UI state + │   (callbacks)  │  (Firestore /    │
│  Adapters    │ ────────────────▶ │   logic)     │ ◀───────────── │   Storage I/O)   │
└──────────────┘   user actions    └──────────────┘     data       └────────┬─────────┘
                                                                             │
                                                          ┌──────────────────┼──────────────────┐
                                                          ▼                  ▼                  ▼
                                                   Firebase Auth     Cloud Firestore     Cloud Storage
                                                          ▲
                                                          │ FCM push (deep link)
                                                   ┌──────┴───────┐
                                                   │ Cloud Function│  ← scheduled poll (every 1 min)
                                                   └──────────────┘
```

- **View.** `Activities` and RecyclerView `Adapters` render state and forward user actions. No business logic.
- **ViewModel.** Holds UI state and orchestrates use cases, survives configuration changes, and exposes results and errors as `LiveData`. A `BaseViewModel` centralizes the repository handle and error channel.
- **Repository.** Singletons (`CapsuleRepository`, `FriendRepository`) that own all Firestore and Storage queries, keeping data access testable and out of the UI.
- **Backend.** A scheduled **Cloud Function** decouples unlock delivery from the client. It runs server-side every minute, so capsules unlock and notify reliably even when the app is closed.

### How scheduled unlocking works

The hardest product requirement was delivering a capsule at a future time, reliably, even when the app isn't running. Time Capsule solves this server-side rather than with fragile on-device alarms:

1. On each launch and token refresh, `MyFirebaseMessagingService` syncs the device's FCM token to `users/{uid}/fcmToken`.
2. The Cloud Function (`functions/index.js`) runs on a **1-minute schedule**, querying Firestore for capsules where `unlocked == false` and `unlockTime <= now`.
3. For each match it sends an FCM message carrying the `capsuleId`, then flips `unlocked = true` so it fires exactly once.
4. The notification's deep link (`timecapsule://capsule/{capsuleId}`) opens the capsule detail screen directly on tap.

---

## Project Structure

```
timecapsule/
├── TimeCapsule/                         # Android app (Gradle project)
│   └── app/src/main/java/edu/northeastern/timecapsule/
│       ├── MainActivity.java            # Home: capsule list, search, status filters
│       ├── auth/                        # Login, Register, Nickname setup
│       ├── ui/
│       │   ├── create/                  # 3-step capsule creation flow
│       │   ├── read/                    # Capsule list & detail (countdown / media)
│       │   ├── friends/                 # Add / remove / list friends
│       │   └── account/                 # Account settings
│       ├── viewmodel/                   # BaseViewModel + Auth/List/Detail/Create VMs
│       ├── repository/                  # CapsuleRepository, FriendRepository (Firestore/Storage)
│       ├── model/                       # Capsule, Friend data classes
│       ├── adapter/                     # RecyclerView + friend-selection adapters
│       ├── notifications/               # MyFirebaseMessagingService (FCM)
│       └── utils/                       # LocationHelper, DateUtils, ErrorHandler
├── functions/                           # Cloud Function: scheduled unlock notifications
└── docs/                                # Product spec: tech stack, user flow
```

---

## User Flow

1. **Sign up / log in**, then set a nickname on first login.
2. **Capsule list (home).** Unlocked capsules surface first. Search by title or filter by lock status.
3. **Create a capsule** in three steps:
   - **Step 1.** Add photos/videos (optional) and a title.
   - **Step 2.** Write your message (up to 1,000 chars) and tag a location.
   - **Step 3.** Pick the unlock date and time (must be in the future) and choose **Private** or share with friends.
4. **While locked**, the capsule shows only its title and a live countdown.
5. **On unlock**, a push notification arrives. Tapping it opens the full capsule (text, media, location, timestamp).

---

## Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 11
- A Firebase project with **Authentication**, **Firestore**, **Storage**, and **Cloud Messaging** enabled

### Setup
1. **Clone the repo**
   ```bash
   git clone https://github.com/<your-username>/timecapsule.git
   cd timecapsule
   ```
2. **Add your Firebase config.**
   Create your own Firebase project, then download `google-services.json` and place it in `TimeCapsule/app/`.
   > Note: `google-services.json` is intentionally git-ignored, so it is never committed.
3. **Open & run.**
   Open the `TimeCapsule/` folder in Android Studio, let Gradle sync, and run on an emulator or device (API 27+).

### (Optional) Deploy the unlock-notifications backend
```bash
cd functions
npm install
firebase deploy --only functions
```

---

## Technical Highlights

- **Server-driven scheduled delivery.** Unlock notifications run on a scheduled Cloud Function plus FCM rather than device alarms, so capsules unlock reliably even when the app is closed. An idempotent `unlocked` flag guarantees exactly-once delivery.
- **Clean MVVM with a repository layer.** Strict separation of View, ViewModel, and Repository keeps Firebase access centralized, configuration-change-safe, and unit-test friendly.
- **Robust media pipeline.** Multi-file image/video upload to Cloud Storage namespaced per `user/capsule`, with EXIF-aware image rendering and video first-frame thumbnailing for previews.
- **Sharing model in Firestore.** Capsules carry a `sharedWithUserIds` array. A recipient's list is assembled by querying both owned capsules and capsules shared with them, with delete permissions enforced by ownership.
- **Graceful location handling.** Fused Location Provider with reverse geocoding to a human-readable place, falling back to raw coordinates, and a manual-entry path when permissions are denied.
- **Deep-linked navigation.** A `timecapsule://capsule/{id}` scheme routes notification taps directly to the relevant capsule.

---

> Built as a CS5520 (Mobile Application Development) project at Northeastern University.
