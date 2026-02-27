---
name: android-kotlin-development
description: Develop native Android apps with Kotlin. Covers MVVM with Jetpack, Compose for modern UI, Retrofit for API calls, Room for local storage, and navigation architecture.
---

# Android Kotlin Development

Build production-ready native Android applications with Kotlin, leveraging modern Jetpack libraries for architecture, UI, networking, and data persistence.

## When to Use This Skill

- Building native Android applications with Kotlin
- Implementing MVVM architecture with Jetpack Compose
- Setting up dependency injection with Hilt
- Managing network requests with Retrofit
- Implementing local data storage with Room
- Building type-safe navigation
- Handling coroutines and asynchronous operations
- Testing Android components

## Core Architecture

### MVVM Pattern with Jetpack

**ViewModel with StateFlow:**

```kotlin
@HiltViewModel
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    init {
        loadNews()
    }
    
    private fun loadNews() {
        viewModelScope.launch {
            try {
                val news = newsRepository.getNews()
                _uiState.value = UiState.Success(news)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class UiState {
    object Loading : UiState()
    data class Success(val news: List<NewsItem>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Dependency Injection with Hilt

**Module Setup:**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Singleton
    @Provides
    fun provideNewsApi(retrofit: Retrofit): NewsApi {
        return retrofit.create(NewsApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Singleton
    @Provides
    fun provideNewsRepository(
        newsApi: NewsApi,
        newsDao: NewsDao
    ): NewsRepository {
        return NewsRepositoryImpl(newsApi, newsDao)
    }
}
```

**Application Setup:**

```kotlin
@HiltAndroidApp
class MyApplication : Application()
```

### Networking with Retrofit

**API Definition:**

```kotlin
interface NewsApi {
    @GET("articles")
    suspend fun getArticles(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<ArticleDto>>
    
    @POST("articles")
    suspend fun createArticle(@Body article: ArticleDto): ArticleDto
    
    @DELETE("articles/{id}")
    suspend fun deleteArticle(@Path("id") id: String)
}
```

**Repository Pattern:**

```kotlin
interface NewsRepository {
    suspend fun getNews(): List<NewsItem>
    suspend fun createArticle(title: String, content: String): NewsItem
    fun observeNews(): Flow<List<NewsItem>>
}

class NewsRepositoryImpl(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao
) : NewsRepository {
    
    override suspend fun getNews(): List<NewsItem> {
        return try {
            val articles = newsApi.getArticles()
            val items = articles.data.map { it.toEntity() }
            newsDao.insertAll(items)
            items
        } catch (e: Exception) {
            newsDao.getAllNews()
        }
    }
    
    override fun observeNews(): Flow<List<NewsItem>> {
        return newsDao.observeAllNews()
    }
    
    override suspend fun createArticle(title: String, content: String): NewsItem {
        val article = ArticleDto(title = title, content = content)
        val created = newsApi.createArticle(article)
        return created.toEntity().also { newsDao.insert(it) }
    }
}
```

### Local Storage with Room

**Entity Definition:**

```kotlin
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
```

**DAO Interface:**

```kotlin
@Dao
interface NewsDao {
    
    @Query("SELECT * FROM articles ORDER BY created_at DESC")
    suspend fun getAllNews(): List<ArticleEntity>
    
    @Query("SELECT * FROM articles ORDER BY created_at DESC")
    fun observeAllNews(): Flow<List<ArticleEntity>>
    
    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: String): ArticleEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: ArticleEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)
    
    @Delete
    suspend fun delete(article: ArticleEntity)
    
    @Query("DELETE FROM articles WHERE created_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
```

**Database Setup:**

```kotlin
@Database(
    entities = [ArticleEntity::class, UserEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
    abstract fun userDao(): UserDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
    
    @Singleton
    @Provides
    fun provideNewsDao(database: AppDatabase): NewsDao {
        return database.newsDao()
    }
}
```

### Type-Safe Navigation

**Route Definition:**

```kotlin
sealed class Route(val route: String) {
    object Home : Route("home")
    object ArticleDetail : Route("article/{articleId}") {
        fun createRoute(articleId: String) = "article/$articleId"
    }
    object Search : Route("search")
}
```

**Navigation Setup:**

```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Route.Home.route
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onArticleClick = { articleId ->
                    navController.navigate(Route.ArticleDetail.createRoute(articleId))
                }
            )
        }
        
        composable(
            route = Route.ArticleDetail.route,
            arguments = listOf(
                navArgument("articleId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getString("articleId") ?: return@composable
            ArticleDetailScreen(articleId = articleId)
        }
        
        composable(Route.Search.route) {
            SearchScreen()
        }
    }
}
```

### Coroutines and Async Operations

**Proper Coroutine Usage:**

```kotlin
@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val repository: NewsRepository
) : ViewModel() {
    
    private val _articles = MutableStateFlow<List<NewsItem>>(emptyList())
    val articles = _articles.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()
    
    fun loadArticles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _articles.value = repository.getNews()
            } catch (e: Exception) {
                _errorMessage.emit(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteArticle(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteArticle(id)
                _articles.value = _articles.value.filter { it.id != id }
            } catch (e: Exception) {
                _errorMessage.emit("Failed to delete article")
            }
        }
    }
}
```

## UI Implementation with Compose

**Screen Composition:**

```kotlin
@Composable
fun HomeScreen(
    viewModel: NewsViewModel = hiltViewModel(),
    onArticleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("News") },
                actions = {
                    IconButton(onClick = { viewModel.refreshNews() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items((uiState as UiState.Success).news) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { onArticleClick(article.id) }
                        )
                    }
                }
            }
            is UiState.Error -> {
                ErrorScreen(message = (uiState as UiState.Error).message)
            }
        }
    }
}
```

## Best Practices

1. **State Management**: Use `StateFlow` for UI state, `SharedFlow` for one-time events
2. **Lifecycle Safety**: Leverage `viewModelScope` to cancel coroutines automatically
3. **Dependency Injection**: Use Hilt for all dependencies
4. **Repository Pattern**: Abstract data sources behind repositories
5. **Error Handling**: Implement proper exception handling with sealed classes
6. **Configuration Changes**: Use `rememberSaveable` for Compose state
7. **Testing**: Structure code for testability with dependency injection
8. **Performance**: Use `LazyColumn` for lists, avoid blocking operations

## Common Patterns

- Offline-first architecture with Room + Retrofit
- Pull-to-refresh with coroutines
- Pagination with `Flow` and state management
- Search with debouncing
- Error recovery and retry logic

## Resources

- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Retrofit HTTP Client](https://square.github.io/retrofit/)
