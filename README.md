# Craft Nook

A production-ready Android inventory management application built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **Hilt Dependency Injection**, following **MVVM + Clean Architecture** principles.

## Project Architecture

This project follows a three-layer clean architecture pattern:

```
app/
├── src/main/kotlin/com/example/craftnook/
│   ├── data/                 # Data Layer
│   │   ├── database/         # Room entities and DAOs
│   │   ├── network/          # Retrofit API interfaces
│   │   ├── repository/       # Repository implementations
│   │   └── di/              # Data layer dependency injection
│   │
│   ├── domain/               # Domain Layer (Business Logic)
│   │   ├── model/           # Data models and domain objects
│   │   ├── usecase/         # Use cases / business logic
│   │   └── repository/      # Repository interfaces
│   │
│   ├── ui/                   # Presentation Layer
│   │   ├── screen/          # Jetpack Compose screens
│   │   ├── viewmodel/       # ViewModels
│   │   ├── component/       # Reusable composables
│   │   ├── theme/           # Material 3 theming
│   │   ├── navigation/      # Navigation setup
│   │   └── di/              # UI layer dependency injection
│   │
│   └── MyApplication.kt      # Application class with Hilt setup
│
└── src/test/                 # Unit tests
```

### Layer Responsibilities

#### **Data Layer** (`data/`)
- Manages all data sources (local database, remote API)
- Room database setup with entities and DAOs
- Retrofit API clients and network calls
- Repository implementations that abstract data sources
- Dependency injection for data layer components

**Key Components:**
- `Entity` classes for Room database tables
- `Dao` interfaces for database operations
- `Api` interfaces for REST endpoints
- `Repository` implementations combining Room + Retrofit

#### **Domain Layer** (`domain/`)
- Contains business logic and use cases
- Independent of Android framework
- Defines repository interfaces (contracts)
- Data models used across the app
- Can be tested without any Android dependencies

**Key Components:**
- `UseCase` classes for business operations
- `Model` data classes
- `Repository` interfaces (contracts)

#### **UI Layer** (`ui/`)
- Jetpack Compose screens and components
- ViewModels for state management
- Navigation setup with Navigation Compose
- Material 3 theming and styling
- Dependency injection for UI components

**Key Components:**
- `Screen` composables for full screens
- `ViewModel` classes for UI state
- `Component` composables for reusable UI elements
- `Theme` setup for Material Design 3

---

## Technology Stack

### UI & Presentation
- **Jetpack Compose** 1.5.4 - Modern declarative UI framework
- **Material 3** - Latest Google Material Design guidelines
- **Navigation Compose** - Type-safe navigation

### Architecture & State Management
- **MVVM** - Model-View-ViewModel pattern
- **ViewModel** - Lifecycle-aware state management
- **StateFlow** - Reactive state container

### Data & Persistence
- **Room Database** 2.6.0 - Local SQLite database
- **Retrofit** 2.9.0 - Type-safe REST client
- **OkHttp** - HTTP client with interceptors

### Dependency Injection
- **Hilt** 2.48 - Dependency injection framework
- **Hilt Navigation Compose** - ViewModel injection in Compose

### Coroutines
- **Kotlin Coroutines** 1.7.3 - Asynchronous programming
- **Coroutine Test** - Testing support

### Testing
- **JUnit 4** - Unit testing framework
- **Mockk** - Mocking library for Kotlin
- **Espresso** - UI testing framework
- **Compose UI Testing** - Compose-specific UI tests

---

## Project Structure Details

### `data/database/`
```kotlin
// Entity - Maps to a Room table
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String
)

// DAO - Database Access Object
@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles")
    fun getAll(): Flow<List<ArticleEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: ArticleEntity)
}

// Database - Room database configuration
@Database(entities = [ArticleEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
```

### `data/repository/`
```kotlin
// Repository Implementation
class ArticleRepositoryImpl(
    private val articleApi: ArticleApi,
    private val articleDao: ArticleDao
) : ArticleRepository {
    override fun getArticles(): Flow<List<Article>> {
        return articleDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
```

### `domain/usecase/`
```kotlin
// Use Case - Business Logic
class GetArticlesUseCase(
    private val repository: ArticleRepository
) {
    operator fun invoke(): Flow<List<Article>> {
        return repository.getArticles()
    }
}
```

### `ui/viewmodel/`
```kotlin
// ViewModel - State Management
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val getArticlesUseCase: GetArticlesUseCase
) : ViewModel() {
    
    val articles = getArticlesUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
}
```

### `ui/screen/`
```kotlin
// Compose Screen
@Composable
fun ArticleScreen(
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    
    LazyColumn {
        items(articles) { article ->
            ArticleCard(article)
        }
    }
}
```

---

## Dependency Injection Setup

### Hilt Configuration

**Application Class:**
```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

**Module Example:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideArticleRepository(
        api: ArticleApi,
        dao: ArticleDao
    ): ArticleRepository {
        return ArticleRepositoryImpl(api, dao)
    }
}
```

---

## Build Configuration

### Dependencies Overview

| Dependency | Purpose | Version |
|------------|---------|---------|
| `androidx.compose.ui:ui` | Compose UI framework | 1.5.4 |
| `androidx.compose.material3:material3` | Material Design 3 | 1.1.2 |
| `androidx.room:room-runtime` | Local database | 2.6.0 |
| `com.google.dagger:hilt-android` | Dependency injection | 2.48 |
| `com.squareup.retrofit2:retrofit` | REST client | 2.9.0 |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | ViewModel | 2.6.1 |

---

## Getting Started

### Prerequisites
- Android Studio Koala or newer
- JDK 17 or newer
- Android SDK 34
- Minimum API Level 24

### Setup Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device

### Creating New Features

1. **Create Domain Model** (`domain/model/`)
2. **Define Repository Interface** (`domain/repository/`)
3. **Create Use Case** (`domain/usecase/`)
4. **Implement Repository** (`data/repository/`)
5. **Create Room Entity & DAO** (`data/database/`)
6. **Create API Interface** (`data/network/`)
7. **Add ViewModel** (`ui/viewmodel/`)
8. **Build Compose Screen** (`ui/screen/`)
9. **Wire Up Navigation**

---

## Testing

### Unit Testing
```bash
./gradlew test
```

### UI Testing
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
```bash
./gradlew testDebugUnitTest --tests "*" --coverage
```

---

## Gradle Commands

| Command | Purpose |
|---------|---------|
| `./gradlew build` | Build release APK |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew installDebug` | Install on connected device |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests |

---

## Best Practices

### Architecture
- ✅ Keep layers independent and testable
- ✅ Use interfaces for abstraction
- ✅ Inject dependencies via Hilt
- ✅ Use coroutines for async operations

### UI Development
- ✅ Use `StateFlow` for state management
- ✅ Hoist state to ViewModel
- ✅ Create reusable composables
- ✅ Use Material 3 components

### Database
- ✅ Use Room for type-safe database access
- ✅ Return `Flow<>` from DAOs for reactive updates
- ✅ Use migrations for schema changes

### Testing
- ✅ Test use cases and repositories
- ✅ Mock external dependencies
- ✅ Test UI state in ViewModels

---

## Resources

- [Android Architecture Guide](https://developer.android.com/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

---

## License

This project is licensed under the MIT License - see the LICENSE file for details.
