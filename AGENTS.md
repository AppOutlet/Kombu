# 🍣 Kombu - Application Analysis

> **⚠️ Important**: This document should be updated whenever architectural changes occur in the project, including:
> - Adding/removing modules
> - Changing platform targets
> - Major dependency updates
> - Architectural pattern changes
> - New build configurations
> - Package structure modifications

## Project Overview

**Kombu** is a modern, cross-platform analytics dashboard built with Kotlin Multiplatform (KMP), designed to provide an alternative frontend interface for Umami Analytics. The application showcases privacy-friendly analytics across multiple platforms while demonstrating best practices in cross-platform development.

- **Developer**: [AppOutlet](https://appoutlet.dev)
- **Status**: Early development stage (work in progress)
- **Core Technology**: Kotlin Multiplatform with Jetpack Compose
- **Primary Use Case**: Analytics dashboard for [Umami Kotlin](https://github.com/AppOutlet/umami-kotlin) library

---

## Architecture Overview

### Multi-Platform Architecture

Kombu follows a shared-code architecture where business logic and UI components are written once and deployed to multiple platforms:

```
┌─────────────────────────────────────────────────┐
│              kombu-shared (Core)                │
│  - Common UI (Jetpack Compose Multiplatform)   │
│  - Business Logic                               │
│  - Platform Abstractions                        │
└─────────────────────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┬─────────────┐
        │             │             │             │
┌───────▼──────┐ ┌───▼────────┐ ┌──▼──────────┐ ┌▼────────────┐
│   Android    │ │  Desktop   │ │     iOS     │ │ Web (Wasm)  │
│  (Android)   │ │   (JVM)    │ │  (Native)   │ │  (wasmJs)   │
└──────────────┘ └────────────┘ └─────────────┘ └─────────────┘
```

---

## Module Structure

### 1. `kombu-shared` (Core Module)

**Purpose**: Contains all shared code including UI, business logic, and platform abstractions.

**Target Platforms**:
- Android (`androidMain`)
- JVM Desktop (`jvmMain`)
- iOS (`iosMain` - arm64 & simulator)
- Web/WASM (`wasmJsMain`, `webMain`)

**Key Components**:

#### Source Sets

```
kombu-shared/src/
├── commonMain/          # Shared code for all platforms
│   ├── kotlin/
│   │   └── dev/appoutlet/
│   │       ├── App.kt           # Main Compose UI
│   │       ├── Platform.kt      # Platform abstraction interface
│   │       └── Greeting.kt      # Example business logic
│   └── composeResources/        # Shared resources (images, strings)
│
├── androidMain/         # Android-specific implementations
│   └── kotlin/dev/appoutlet/
│       └── Platform.android.kt  # Android platform implementation
│
├── iosMain/             # iOS-specific implementations
│   └── kotlin/dev/appoutlet/
│       ├── Platform.ios.kt      # iOS platform implementation
│       └── MainViewController.kt # iOS entry point
│
├── jvmMain/             # Desktop JVM-specific implementations
│   └── kotlin/dev/appoutlet/
│       └── Platform.jvm.kt      # JVM platform implementation
│
├── wasmJsMain/          # WASM-specific implementations
│   └── kotlin/dev/appoutlet/
│       └── Platform.wasmJs.kt   # WASM platform implementation
│
├── webMain/             # Web-specific entry point
│   └── kotlin/dev/appoutlet/
│       └── main.kt              # Web application entry
│
└── commonTest/          # Shared unit tests
    └── kotlin/dev/appoutlet/
        └── ComposeAppCommonTest.kt
```

#### Technology Stack

**UI Framework**:
- Jetpack Compose Multiplatform (Material 3)
- Compose Runtime, Foundation, and UI
- Lifecycle ViewModel & Runtime Compose

**Build Configuration**:
- Gradle Kotlin DSL
- Kotlin 2.2.20
- Android target SDK: 36, min SDK: 28
- JVM target: 11

---

### 2. `android` (Android Application Module)

**Purpose**: Android-specific application wrapper.

**Structure**:
```
android/
├── src/
│   ├── main/
│   │   └── java/dev/appoutlet/kombu/
│   │       └── MainActivity.kt      # Android entry point
│   ├── androidTest/                 # Instrumented tests
│   └── test/                        # Unit tests
├── build.gradle.kts
└── proguard-rules.pro
```

**Key Features**:
- `MainActivity`: Standard Android activity with edge-to-edge display
- Uses `ComponentActivity` with Compose integration
- Imports shared `App()` composable from `kombu-shared`
- Package: `dev.appoutlet.kombu`

**Dependencies**:
- `kombu-shared` module
- AndroidX Activity Compose
- Compose Compiler plugin

---

### 3. `kombu-desktop` (Desktop Application Module)

**Purpose**: Desktop (Windows, macOS, Linux) application using JVM.

**Structure**:
```
kombu-desktop/
├── src/
│   └── main/
│       └── kotlin/dev/appoutlet/kombu/
│           └── Main.kt              # Desktop entry point
└── build.gradle.kts
```

**Key Features**:
- Window-based application using Compose Desktop
- Main class: `dev.appoutlet.kombu.Kombu`
- Distributable formats: DMG (macOS), MSI (Windows), DEB (Linux)
- Hot reload support via Compose plugin

**Entry Point** (`Main.kt`):
```kotlin
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kombu",
    ) {
        App()
    }
}
```

---

### 4. `kombu-ios` (iOS Application)

**Purpose**: iOS native application wrapper.

**Structure**:
```
kombu-ios/
├── Kombu/
│   ├── iOSApp.swift            # SwiftUI app entry
│   ├── ContentView.swift       # View bridging to Compose
│   ├── Assets.xcassets/        # App icons and assets
│   ├── Preview Content/
│   └── Info.plist
├── Configuration/
│   └── Config.xcconfig
└── Kombu.xcodeproj/
```

**Key Features**:
- SwiftUI app with `UIViewControllerRepresentable` bridge
- Imports `KombuShared` framework
- Calls `MainViewController()` from shared Kotlin code
- Standard iOS project structure with Xcode project files

**Bridge Implementation** (`ContentView.swift`):
```swift
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
}
```

---

## Platform Abstraction Pattern

Kombu uses Kotlin's `expect/actual` mechanism for platform-specific implementations:

### Interface Definition (Common)
```kotlin
interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
```

### Platform Implementations

| Platform | Implementation | Returns |
|----------|----------------|---------|
| Android | `AndroidPlatform` | "Android {SDK_VERSION}" |
| iOS | `IOSPlatform` | "{systemName} {systemVersion}" |
| JVM | `JVMPlatform` | "Java {java.version}" |
| WASM | `WasmPlatform` | "Web with Kotlin/Wasm" |

---

## UI Architecture

### Compose Multiplatform UI

**Main Application** (`App.kt`):
- Material 3 theming
- State management with `remember` and `mutableStateOf`
- Animated visibility for content transitions
- Safe content padding for proper insets
- Resource management via Compose Resources

**Current UI Features**:
- Interactive button with state toggle
- Animated content visibility
- Platform detection display
- Image resources (Compose Multiplatform logo)

---

## Build System & Dependencies

### Root Build Configuration

**Gradle Version Management**:
- Gradle 8.12.0+
- Kotlin 2.2.20
- JVM args: `-Xmx2048M`
- Configuration cache enabled

**Applied Plugins** (Root):
- Android Application/Library
- Kotlin Multiplatform
- Compose & Compose Compiler
- BuildKonfig (configuration)
- Kover (code coverage)
- Mokkery (mocking)
- SQLDelight (database)
- Sentry (error tracking)
- Git Hooks

### Key Dependencies

**Composition & UI**:
- Jetpack Compose 1.9.0
- Material 3 Adaptive Navigation
- Lifecycle ViewModel & Runtime Compose 2.9.4

**Networking & Data**:
- Ktor 3.3.1 (HTTP client)
- Kotlinx Serialization
- Umami Analytics SDK 0.1.12

**Utilities**:
- Coil 3.3.0 (image loading)
- Koin 4.1.1 (dependency injection)
- Coroutines 1.10.2
- SQLDelight 2.1.0 (database)
- Kermit 2.0.8 (logging)

**Platform-Specific**:
- Android: Activity Compose 1.11.0
- iOS: Native drivers for SQLDelight
- Desktop: Coroutines Swing, SQLite driver
- Web: WASM experimental support

---

## Development Configuration

### Gradle Properties

**Build Optimizations**:
- Configuration cache enabled
- Non-transitive R classes (Android)
- Kotlin daemon with 2GB heap
- Incremental compilation

**Platform Settings**:
- Android source set layout v2
- C-interop commonization enabled
- Native cache disabled (for development)
- WASM experimental features enabled

**Code Style**:
- Official Kotlin code style

---

## Project Structure Details

### Directory Layout

```
Kombu/
├── android/                    # Android app module
├── kombu-desktop/              # Desktop (JVM) app module
├── kombu-ios/                  # iOS app with Xcode project
├── kombu-shared/               # Shared Kotlin Multiplatform module
├── gradle/                     # Gradle wrapper and version catalogs
├── kotlin-js-store/            # JS/WASM artifacts
├── build.gradle.kts            # Root build configuration
├── settings.gradle.kts         # Project settings and modules
├── gradle.properties           # Build properties
├── local.properties            # Local SDK paths (gitignored)
├── README.md                   # Project documentation
└── .gitignore                  # Git ignore rules
```

### Version Catalog (`gradle/libs.versions.toml`)

Centralized dependency management with:
- 40+ library versions
- 15+ plugin configurations
- Dependency bundles (e.g., BouncyCastle)

---

## Current Implementation Status

### ✅ Implemented

1. **Core Architecture**:
   - Multi-platform project setup
   - Shared UI module with Compose
   - Platform abstraction layer
   - Build system configuration

2. **Platform Support**:
   - Android app with Material You
   - Desktop app (Windows, macOS, Linux)
   - iOS app with SwiftUI bridge
   - Web app with WASM support

3. **Development Infrastructure**:
   - Gradle build system with version catalog
   - Git hooks integration
   - Code style configuration
   - Resource management

### 🚧 In Development

Based on the early-stage warning in README, the following are likely planned:

1. **Analytics Integration**:
   - Umami Kotlin SDK integration
   - Dashboard UI components
   - Data visualization

2. **Core Features**:
   - Authentication system
   - Data persistence (SQLDelight)
   - Network layer (Ktor)
   - Navigation system

3. **Production Readiness**:
   - Error tracking (Sentry)
   - Logging infrastructure (Kermit)
   - Testing suite (Kotest, Mokkery)
   - CI/CD pipeline

---

## Technology Highlights

### Kotlin Multiplatform

**Benefits**:
- Single codebase for all platforms
- Native performance on each platform
- Platform-specific optimizations via expect/actual
- Shared business logic and UI

**Target Platforms**:
- Android (ARM, x86)
- iOS (arm64, simulator arm64)
- Desktop (JVM - Windows, macOS, Linux)
- Web (WASM via wasmJs)

### Compose Multiplatform

**Features**:
- Declarative UI framework
- Material 3 design system
- Hot reload support (desktop)
- Shared UI components across platforms
- Resource management system

---

## Build & Distribution

### Android
- APK/AAB generation
- ProGuard rules configured
- Min SDK: 28 (Android 9.0)
- Target SDK: 36 (Android 14+)

### Desktop
- **Formats**: DMG (macOS), MSI (Windows), DEB (Linux)
- **Package**: `dev.appoutlet.kombu`
- **Version**: 1.0.0

### iOS
- Xcode project configuration
- Framework: `KombuShared` (static)
- Architectures: arm64, simulator arm64

### Web
- WASM binary execution
- Browser-based deployment
- Experimental Compose for Web

---

## Development Workflow

### Prerequisites
- JDK 11+ for Android and Desktop
- Xcode (for iOS development)
- Android Studio or IntelliJ IDEA
- Kotlin 2.2.20+

### Build Commands

```bash
# Build all platforms
./gradlew build

# Run Android
./gradlew :android:installDebug

# Run Desktop
./gradlew :kombu-desktop:run

# Build iOS framework
./gradlew :kombu-shared:linkDebugFrameworkIosSimulatorArm64

# Build WASM
./gradlew :kombu-shared:wasmJsBrowserDistribution
```

---

## Code Organization Principles

### Separation of Concerns

1. **Common Layer** (`commonMain`):
   - UI components (Compose)
   - Business logic
   - Data models
   - Platform interfaces (expect)

2. **Platform Layer** (`*Main`):
   - Platform-specific implementations (actual)
   - Native API integrations
   - Platform-specific UI adjustments

3. **Application Layer** (`android`, `kombu-desktop`, `kombu-ios`):
   - Platform app configuration
   - Entry points
   - Platform-specific dependencies

### Testing Strategy

- **Common Tests** (`commonTest`): Shared business logic tests
- **Android Tests** (`androidTest`): Instrumented Android tests
- **Unit Tests** (`test`): Platform-specific unit tests
- **Mocking**: Mokkery for multiplatform mocking
- **Assertions**: Kotest assertions

---

## Future Considerations

### Planned Dependencies (From libs.versions.toml)

1. **Authentication**: Supabase Auth integration
2. **Monetization**: RevenueCat purchases
3. **Rich Text**: Rich text editor with Coil integration
4. **Security**: Cryptography providers for each platform
5. **Animations**: Kottie for Lottie animations
6. **Web Content**: Ksoup for HTML parsing, WebView component
7. **Material Design**: Material Kolor for dynamic theming

### Scalability Features

- Adaptive navigation for different screen sizes
- SQLDelight for efficient data persistence
- Ktor for robust networking
- Koin for dependency injection at scale
- Sentry for production monitoring

---

## Package Structure

**Base Package**: `dev.appoutlet`
- **Android**: `dev.appoutlet.kombu`
- **Desktop**: `dev.appoutlet.kombu`
- **iOS**: `KombuShared` framework
- **Shared**: `dev.appoutlet`

---

## Git Configuration

### Ignored Files
- Build artifacts (`build/`, `*.apk`, `*.aab`)
- IDE files (`.idea/`, `.gradle/`, `.kotlin/`)
- Local configuration (`local.properties`)
- Platform-specific user data (`xcuserdata/`)
- Database files (`*.db`)
- Logs and caches (`*.log`, `cache/`)
- Secrets (`*.jks`, service account JSONs)

---

## Summary

Kombu is a well-architected, modern Kotlin Multiplatform application showcasing best practices in cross-platform development. While still in early stages, the project demonstrates:

- **Solid Foundation**: Proper multi-module architecture with clear separation of concerns
- **Modern Tech Stack**: Latest versions of Kotlin, Compose, and related libraries
- **Cross-Platform First**: True code sharing across Android, iOS, Desktop, and Web
- **Production-Ready Setup**: Comprehensive tooling for testing, monitoring, and deployment
- **Extensible Design**: Platform abstraction patterns allowing easy addition of new platforms

The application serves dual purposes: as a functional analytics dashboard for Umami and as a reference implementation for Kotlin Multiplatform development with Compose.

---

**Last Updated**: Based on project analysis as of 2025-11-16
**Project Status**: 👷‍♀️ Early Development Stage
**Maintainer**: [AppOutlet](https://appoutlet.dev)
