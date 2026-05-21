# NewsHeadlines - Android TV News App

NewsHeadlines is a modern Android TV application built to provide users with the latest news headlines from around the world. It features a TV-optimized user interface and supports offline viewing by caching news data locally.

## Features

- **TV-Optimized UI**: Designed specifically for large screens using Jetpack Compose for TV.
- **Offline Support**: Uses Room database to cache articles for offline access.
- **Dynamic Refresh**: Long-press the **Down** directional key to refresh the news feed with the latest headlines.
- **Performance Optimized**: Uses stable keys in `LazyColumn` to minimize recompositions.
- **Resilient Networking**: Exponential backoff retry mechanism for API calls.
- **Image Caching**: Efficient loading and caching of article images using Coil.

---

## Setup Instructions

To set up the project from scratch, follow these steps:

### 1. Prerequisites
- **Android Studio**: Ladybug (2024.2.1) or newer.
- **JDK**: Java 17.
- **API Key**: Obtain a free API key from [NewsAPI.org](https://newsapi.org/).

### 2. Configure API Key
The app uses a `local.properties` file to securely manage the API key.
1. Create a `local.properties` file in the root directory (if not present).
2. Add your key:
   ```properties
   NEWS_API_KEY=your_actual_api_key_here
   ```

### 3. Build and Run
1. Open the project in Android Studio.
2. Select an Android TV Emulator (API 34+) or a physical device.
3. Click **Run**.

---

## Usage Instructions

Once the app is running on your Android TV device:

- **Navigate**: Use the **Up** and **Down** directional keys on your remote to scroll through the news headlines.
- **View Details**: Press the **Center (Select)** button on a focused article to expand it and read the full description and content.
- **Refresh Feed**: To manually fetch the latest news, **long-press the Down** directional key. A toast message will indicate if there was an error.
- **Offline Access**: You can continue to browse previously loaded news articles even when the device is offline.

---

## Project Structure

The project follows **Clean Architecture** principles with a modular package structure:

```text
com.dhruvpatel.tvnews
├── common             # Cross-cutting concerns (Network Utils, Resource wrappers)
├── data               # Data Layer (API, Database, Repositories, Mappers)
│   ├── local          # Room Database, DAOs, Entities
│   ├── remote         # Retrofit API Service, DTOs
│   └── repository     # Repository Implementations
├── di                 # Dependency Injection (Hilt Modules)
├── domain             # Domain Layer (Business Logic)
│   ├── model          # Domain Models
│   ├── repository     # Repository Interfaces
│   └── usecase        # Use Cases
└── presentation       # UI Layer (MVVM)
    ├── news           # News Feature (Screens, ViewModels, Components)
    └── ui.theme       # Compose Theme (Colors, Typography)
```

---

## Key Implementations & Code Documentation

### 1. Clean Architecture & MVVM
The app is divided into three layers to ensure separation of concerns:
- **Domain Layer**: Contains the core business logic and models. It is independent of other layers.
- **Data Layer**: Manages data from multiple sources (API and Local Cache).
- **Presentation Layer**: Handles UI logic using ViewModels and Jetpack Compose.

### 2. Concurrency & Thread Safety
In `NewsRepositoryImpl`, a **`Mutex`** is used to synchronize refresh operations:
```kotlin
private val refreshMutex = Mutex()

override suspend fun fetchTopNews() {
   refreshMutex.withLock {
      // Fetch and update cache safely
   }
}
```
This prevents race conditions when multiple refresh triggers occur simultaneously.

### 3. TV-Optimized Interaction
The UI leverages `androidx.tv.material` and handles D-pad events. The manual refresh is implemented via a custom long-press detection on the `Down` key:
```kotlin
onKeyEvent { keyEvent ->
   if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      // Handle long press for refresh
   }
   false
}
```

### 4. Robust UI Text Handling
To maintain a clean separation between business logic and UI, the app implements a `UiText` sealed class. This allows the `ViewModel` to emit string resources (`R.string.id`) or dynamic strings without requiring a `Context` reference, ensuring the code remains testable and leak-free.

---

## Data Flow

The app follows a **Unidirectional Data Flow (UDF)** with **Offline-First** strategy:

1. **API Fetch**: `NewsRepository` fetches data from `NewsApiService`.
2. **Mapping**: Remote DTOs are mapped to Local Entities.
3. **Persistence**: Entities are saved into `Room` database (the Single Source of Truth).
4. **Observation**: `NewsRepository` exposes a `Flow<List<Article>>` from the database.
5. **Consumption**: `ViewModel` collects the flow and updates `NewsState`.
6. **Rendering**: `NewsScreen` observes the state and renders the UI.

---

## Optimizations

### 1. Parallel Fetching
When refreshing news, the app fetches multiple categories (Business, Technology, Science) in parallel using `async` and `awaitAll` to reduce total latency:
```kotlin
val results = categories.map { category ->
   async { apiService.getTopHeadlines(category = category, ...) }
}.awaitAll()
```

### 2. Offline-First Approach
The UI always observes the local database. Network fetches only update the cache, ensuring the app is always functional even with intermittent connectivity.

### 3. List Performance
`LazyColumn` uses stable keys for items, which allows Compose to skip recomposition for unchanged items during data updates:
```kotlin
items(state.articles, key = { it.url }) { article ->
   ArticleItem(article = article)
}
```

---

## Network Retry Approaches

The app implements a resilient networking strategy using **Exponential Backoff**:

### `retry` Utility
Located in `common/network/NetworkUtils.kt`, this function retries failed network operations:
```kotlin
suspend fun <T> retry(
   times: Int = 3,
   initialDelay: Long = 1000,
   factor: Double = 2.0,
   block: suspend () -> T
): T
```
- Only retries for `IOException` (network loss) and `HttpException`.
- Increases delay exponentially (1s, 2s, 4s...) up to a maximum.

### `safeApiCall`
Wraps API calls to catch exceptions and map them to user-friendly error messages, handling specific HTTP codes like `429` (Too Many Requests) or `500` (Server Error).

---

## License
MIT License.
