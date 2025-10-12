# AI教师系统Android开发指南

## 1. 开发环境搭建

### 1.1 开发工具
- **Android Studio**：最新稳定版本
- **JDK**：OpenJDK 17
- **Kotlin**：1.9.0+
- **Gradle**：8.0+

### 1.2 项目配置
```gradle
// build.gradle (Project)
buildscript {
    ext.kotlin_version = "1.9.0"
    dependencies {
        classpath "com.android.tools.build:gradle:8.0.2"
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        classpath "com.google.dagger:hilt-android-gradle-plugin:2.47"
    }
}

// build.gradle (Module: app)
android {
    compileSdk 34
    
    defaultConfig {
        applicationId "com.aiteacher.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion "1.5.3"
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
}
```

### 1.3 依赖库配置
```gradle
dependencies {
    // Compose BOM
    implementation platform('androidx.compose:compose-bom:2023.08.00')
    
    // Compose UI
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.7.2'
    
    // Navigation
    implementation 'androidx.navigation:navigation-compose:2.7.0'
    
    // ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    
    // Hilt
    implementation 'com.google.dagger:hilt-android:2.47'
    kapt 'com.google.dagger:hilt-compiler:2.47'
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // Network
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Database
    implementation 'androidx.room:room-runtime:2.5.0'
    implementation 'androidx.room:room-ktx:2.5.0'
    kapt 'androidx.room:room-compiler:2.5.0'
    
    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    implementation 'com.github.bumptech.glide:compose:1.0.0-beta01'
    
    // Camera
    implementation 'androidx.camera:camera-core:1.3.0'
    implementation 'androidx.camera:camera-camera2:1.3.0'
    implementation 'androidx.camera:camera-lifecycle:1.3.0'
    implementation 'androidx.camera:camera-view:1.3.0'
    
    // Speech
    implementation 'androidx.speech:speech:1.0.0'
    
    // Permissions
    implementation 'com.google.accompanist:accompanist-permissions:0.30.1'
    
    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.compose.ui:ui-test-junit4'
}
```

## 2. 项目架构设计

### 2.1 Clean Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   Compose   │  │ ViewModel   │  │ Navigation  │        │
│  │     UI     │  │             │  │             │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │   UseCase  │  │   Model     │  │ Repository  │        │
│  │             │  │             │  │ Interface   │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Repository  │  │ DataSource  │  │   Local    │        │
│  │             │  │             │  │  Storage   │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 目录结构
```
app/src/main/java/com/aiteacher/
├── app/                        # Application类
│   └── AITeacherApplication.kt
├── data/                       # 数据层
│   ├── local/                  # 本地数据源
│   │   ├── database/           # 数据库
│   │   ├── preferences/        # 偏好设置
│   │   └── cache/              # 缓存
│   ├── remote/                 # 远程数据源
│   │   ├── api/                # API接口
│   │   ├── dto/                # 数据传输对象
│   │   └── interceptor/         # 拦截器
│   └── repository/              # 数据仓库实现
├── domain/                     # 业务逻辑层
│   ├── model/                  # 业务模型
│   ├── usecase/                # 用例
│   └── repository/             # 仓库接口
├── presentation/               # 表现层
│   ├── ui/                     # UI组件
│   │   ├── theme/              # 主题
│   │   ├── components/         # 通用组件
│   │   ├── screen/             # 屏幕
│   │   └── widget/             # 小部件
│   ├── viewmodel/              # ViewModel
│   └── navigation/             # 导航
├── di/                         # 依赖注入
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
└── utils/                      # 工具类
    ├── Constants.kt
    ├── Extensions.kt
    └── Validators.kt
```

## 3. 核心功能实现

### 3.1 用户认证模块

#### 3.1.1 数据模型
```kotlin
// domain/model/User.kt
data class User(
    val id: String,
    val username: String,
    val email: String,
    val userType: UserType,
    val profile: UserProfile?
)

enum class UserType {
    STUDENT, PARENT, TEACHER
}

data class UserProfile(
    val name: String,
    val avatar: String?,
    val grade: String?,
    val school: String?
)
```

#### 3.1.2 Repository接口
```kotlin
// domain/repository/AuthRepository.kt
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun register(user: User): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<String>
    suspend fun getCurrentUser(): User?
    suspend fun saveUser(user: User)
    suspend fun clearUser()
}
```

#### 3.1.3 UseCase
```kotlin
// domain/usecase/LoginUseCase.kt
class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return authRepository.login(username, password)
    }
}
```

#### 3.1.4 ViewModel
```kotlin
// presentation/viewmodel/LoginViewModel.kt
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        user = user
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val user: User? = null,
    val error: String? = null
)
```

#### 3.1.5 UI界面
```kotlin
// presentation/ui/screen/LoginScreen.kt
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "AI教师",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.login(username, password) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("登录")
            }
        }
        
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
```

### 3.2 学习模块

#### 3.2.1 数据模型
```kotlin
// domain/model/Learning.kt
data class LearningPlan(
    val id: String,
    val studentId: String,
    val subject: String,
    val gradeLevel: String,
    val lessons: List<Lesson>,
    val status: PlanStatus
)

data class Lesson(
    val id: String,
    val title: String,
    val content: String,
    val exercises: List<Exercise>,
    val duration: Int // 分钟
)

data class Exercise(
    val id: String,
    val question: String,
    val options: List<String>?,
    val correctAnswer: String,
    val explanation: String
)

enum class PlanStatus {
    ACTIVE, PAUSED, COMPLETED
}
```

#### 3.2.2 Repository接口
```kotlin
// domain/repository/LearningRepository.kt
interface LearningRepository {
    suspend fun getLearningPlan(studentId: String): Result<LearningPlan>
    suspend fun startLesson(lessonId: String): Result<Lesson>
    suspend fun submitAnswer(answer: Answer): Result<AnswerResult>
    suspend fun getLearningProgress(studentId: String): Result<LearningProgress>
    suspend fun saveLearningProgress(progress: LearningProgress)
}
```

#### 3.2.3 UseCase
```kotlin
// domain/usecase/StartLessonUseCase.kt
class StartLessonUseCase(
    private val learningRepository: LearningRepository
) {
    suspend operator fun invoke(lessonId: String): Result<Lesson> {
        return learningRepository.startLesson(lessonId)
    }
}
```

#### 3.2.4 ViewModel
```kotlin
// presentation/viewmodel/LearningViewModel.kt
@HiltViewModel
class LearningViewModel @Inject constructor(
    private val startLessonUseCase: StartLessonUseCase,
    private val submitAnswerUseCase: SubmitAnswerUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LearningUiState())
    val uiState: StateFlow<LearningUiState> = _uiState.asStateFlow()
    
    fun startLesson(lessonId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            startLessonUseCase(lessonId)
                .onSuccess { lesson ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLesson = lesson,
                        currentExerciseIndex = 0
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }
    
    fun submitAnswer(answer: String) {
        viewModelScope.launch {
            val currentLesson = _uiState.value.currentLesson ?: return@launch
            val currentIndex = _uiState.value.currentExerciseIndex
            
            if (currentIndex < currentLesson.exercises.size) {
                val exercise = currentLesson.exercises[currentIndex]
                val answerData = Answer(
                    exerciseId = exercise.id,
                    studentAnswer = answer,
                    timestamp = System.currentTimeMillis()
                )
                
                submitAnswerUseCase(answerData)
                    .onSuccess { result ->
                        _uiState.value = _uiState.value.copy(
                            currentAnswerResult = result,
                            currentExerciseIndex = currentIndex + 1
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message
                        )
                    }
            }
        }
    }
}

data class LearningUiState(
    val isLoading: Boolean = false,
    val currentLesson: Lesson? = null,
    val currentExerciseIndex: Int = 0,
    val currentAnswerResult: AnswerResult? = null,
    val error: String? = null
)
```

#### 3.2.5 UI界面
```kotlin
// presentation/ui/screen/LearningScreen.kt
@Composable
fun LearningScreen(
    lessonId: String,
    onLessonComplete: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(lessonId) {
        viewModel.startLesson(lessonId)
    }
    
    LaunchedEffect(uiState.currentExerciseIndex) {
        val lesson = uiState.currentLesson
        if (lesson != null && uiState.currentExerciseIndex >= lesson.exercises.size) {
            onLessonComplete()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 学习进度条
        LearningProgressBar(
            progress = uiState.currentExerciseIndex.toFloat() / 
                     (uiState.currentLesson?.exercises?.size ?: 1)
        )
        
        // 学习内容
        uiState.currentLesson?.let { lesson ->
            if (uiState.currentExerciseIndex < lesson.exercises.size) {
                val exercise = lesson.exercises[uiState.currentExerciseIndex]
                
                ExerciseCard(
                    exercise = exercise,
                    onAnswerSubmit = viewModel::submitAnswer,
                    answerResult = uiState.currentAnswerResult
                )
            }
        }
        
        // AI助手
        AIAssistant(
            onSendMessage = { message ->
                // 处理AI交互
            }
        )
    }
}
```

### 3.3 AI交互模块

#### 3.3.1 数据模型
```kotlin
// domain/model/AIInteraction.kt
data class AIMessage(
    val id: String,
    val content: String,
    val sender: MessageSender,
    val timestamp: Long,
    val type: MessageType
)

enum class MessageSender {
    USER, AI
}

enum class MessageType {
    TEXT, IMAGE, VOICE
}

data class AIResponse(
    val message: String,
    val suggestions: List<String>?,
    val actions: List<AIAction>?
)

data class AIAction(
    val type: ActionType,
    val data: Map<String, Any>
)

enum class ActionType {
    SHOW_EXPLANATION, NEXT_EXERCISE, HINT
}
```

#### 3.3.2 Repository接口
```kotlin
// domain/repository/AIInteractionRepository.kt
interface AIInteractionRepository {
    suspend fun sendMessage(message: String, studentId: String): Result<AIResponse>
    suspend fun uploadImage(image: ByteArray, studentId: String): Result<ImageAnalysisResult>
    suspend fun startVoiceInteraction(studentId: String): Result<VoiceSession>
    suspend fun processVoiceInput(audio: ByteArray, sessionId: String): Result<VoiceResult>
}
```

#### 3.3.3 UseCase
```kotlin
// domain/usecase/SendMessageUseCase.kt
class SendMessageUseCase(
    private val aiRepository: AIInteractionRepository
) {
    suspend operator fun invoke(message: String, studentId: String): Result<AIResponse> {
        return aiRepository.sendMessage(message, studentId)
    }
}
```

#### 3.3.4 ViewModel
```kotlin
// presentation/viewmodel/AIInteractionViewModel.kt
@HiltViewModel
class AIInteractionViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<AIMessage>>(emptyList())
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun sendMessage(message: String, studentId: String) {
        viewModelScope.launch {
            // 添加用户消息
            val userMessage = AIMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                sender = MessageSender.USER,
                timestamp = System.currentTimeMillis(),
                type = MessageType.TEXT
            )
            _messages.value = _messages.value + userMessage
            
            _isLoading.value = true
            
            sendMessageUseCase(message, studentId)
                .onSuccess { response ->
                    val aiMessage = AIMessage(
                        id = UUID.randomUUID().toString(),
                        content = response.message,
                        sender = MessageSender.AI,
                        timestamp = System.currentTimeMillis(),
                        type = MessageType.TEXT
                    )
                    _messages.value = _messages.value + aiMessage
                }
                .onFailure { error ->
                    // 处理错误
                }
            
            _isLoading.value = false
        }
    }
}
```

#### 3.3.5 UI界面
```kotlin
// presentation/ui/screen/AIAssistantScreen.kt
@Composable
fun AIAssistantScreen(
    studentId: String,
    viewModel: AIInteractionViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 消息列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        // 输入框
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("输入消息...") },
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText, studentId)
                        messageText = ""
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("发送")
                }
            }
        }
    }
}
```

## 4. 数据层实现

### 4.1 数据库设计
```kotlin
// data/local/database/entities/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val userType: String,
    val profile: String? // JSON string
)

// data/local/database/entities/LearningRecordEntity.kt
@Entity(tableName = "learning_records")
data class LearningRecordEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val lessonId: String,
    val startTime: Long,
    val endTime: Long?,
    val progress: Float,
    val score: Float?,
    val status: String
)
```

### 4.2 DAO接口
```kotlin
// data/local/database/dao/UserDao.kt
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

// data/local/database/dao/LearningRecordDao.kt
@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records WHERE studentId = :studentId")
    suspend fun getLearningRecordsByStudent(studentId: String): List<LearningRecordEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningRecord(record: LearningRecordEntity)
    
    @Update
    suspend fun updateLearningRecord(record: LearningRecordEntity)
}
```

### 4.3 数据库配置
```kotlin
// data/local/database/AITeacherDatabase.kt
@Database(
    entities = [
        UserEntity::class,
        LearningRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AITeacherDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun learningRecordDao(): LearningRecordDao
}

// data/local/database/Converters.kt
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }
    
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}
```

### 4.4 Repository实现
```kotlin
// data/repository/AuthRepositoryImpl.kt
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao
) : AuthRepository {
    
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(username, password))
            val user = response.toUser()
            userDao.insertUser(user.toEntity())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): User? {
        return userDao.getUserById("current")?.toUser()
    }
    
    override suspend fun saveUser(user: User) {
        userDao.insertUser(user.toEntity())
    }
    
    override suspend fun clearUser() {
        userDao.clearUsers()
    }
}
```

## 5. 网络层实现

### 5.1 API接口
```kotlin
// data/remote/api/AuthApi.kt
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse
    
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse
}

// data/remote/api/LearningApi.kt
interface LearningApi {
    @GET("learning/plan/{studentId}")
    suspend fun getLearningPlan(@Path("studentId") studentId: String): LearningPlanResponse
    
    @POST("learning/lesson/start")
    suspend fun startLesson(@Body request: StartLessonRequest): StartLessonResponse
    
    @POST("learning/answer/submit")
    suspend fun submitAnswer(@Body request: SubmitAnswerRequest): SubmitAnswerResponse
}

// data/remote/api/AIApi.kt
interface AIApi {
    @POST("ai/chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
    
    @Multipart
    @POST("ai/image/analyze")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part,
        @Part("studentId") studentId: RequestBody
    ): ImageAnalysisResponse
    
    @Multipart
    @POST("ai/voice/process")
    suspend fun processVoice(
        @Part audio: MultipartBody.Part,
        @Part("studentId") studentId: RequestBody
    ): VoiceProcessResponse
}
```

### 5.2 网络配置
```kotlin
// data/remote/interceptor/AuthInterceptor.kt
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenManager.getToken()
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        return chain.proceed(newRequest)
    }
}

// di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.aiteacher.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
    
    @Provides
    fun provideLearningApi(retrofit: Retrofit): LearningApi {
        return retrofit.create(LearningApi::class.java)
    }
    
    @Provides
    fun provideAIApi(retrofit: Retrofit): AIApi {
        return retrofit.create(AIApi::class.java)
    }
}
```

## 6. 依赖注入配置

### 6.1 数据库模块
```kotlin
// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AITeacherDatabase {
        return Room.databaseBuilder(
            context,
            AITeacherDatabase::class.java,
            "ai_teacher_database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AITeacherDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideLearningRecordDao(database: AITeacherDatabase): LearningRecordDao {
        return database.learningRecordDao()
    }
}
```

### 6.2 Repository模块
```kotlin
// di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        userDao: UserDao
    ): AuthRepository {
        return AuthRepositoryImpl(authApi, userDao)
    }
    
    @Provides
    @Singleton
    fun provideLearningRepository(
        learningApi: LearningApi,
        learningRecordDao: LearningRecordDao
    ): LearningRepository {
        return LearningRepositoryImpl(learningApi, learningRecordDao)
    }
    
    @Provides
    @Singleton
    fun provideAIInteractionRepository(
        aiApi: AIApi
    ): AIInteractionRepository {
        return AIInteractionRepositoryImpl(aiApi)
    }
}
```

## 7. 测试策略

### 7.1 单元测试
```kotlin
// test/domain/usecase/LoginUseCaseTest.kt
@ExtendWith(MockKExtension::class)
class LoginUseCaseTest {
    
    @MockK
    private lateinit var authRepository: AuthRepository
    
    private lateinit var loginUseCase: LoginUseCase
    
    @BeforeEach
    fun setup() {
        loginUseCase = LoginUseCase(authRepository)
    }
    
    @Test
    fun `login with valid credentials should return success`() = runTest {
        // Given
        val username = "testuser"
        val password = "testpass"
        val expectedUser = User("1", username, "test@example.com", UserType.STUDENT, null)
        
        coEvery { authRepository.login(username, password) } returns Result.success(expectedUser)
        
        // When
        val result = loginUseCase(username, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
    }
    
    @Test
    fun `login with invalid credentials should return failure`() = runTest {
        // Given
        val username = "testuser"
        val password = "wrongpass"
        
        coEvery { authRepository.login(username, password) } returns Result.failure(Exception("Invalid credentials"))
        
        // When
        val result = loginUseCase(username, password)
        
        // Then
        assertTrue(result.isFailure)
    }
}
```

### 7.2 UI测试
```kotlin
// androidTest/presentation/ui/LoginScreenTest.kt
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun loginScreen_displaysCorrectly() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockk<LoginViewModel>()
            )
        }
        
        composeTestRule.onNodeWithText("AI教师").assertIsDisplayed()
        composeTestRule.onNodeWithText("用户名").assertIsDisplayed()
        composeTestRule.onNodeWithText("密码").assertIsDisplayed()
        composeTestRule.onNodeWithText("登录").assertIsDisplayed()
    }
    
    @Test
    fun loginButton_clickedWithValidInput_callsLogin() {
        val mockViewModel = mockk<LoginViewModel>(relaxed = true)
        
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                viewModel = mockViewModel
            )
        }
        
        composeTestRule.onNodeWithText("用户名").performTextInput("testuser")
        composeTestRule.onNodeWithText("密码").performTextInput("testpass")
        composeTestRule.onNodeWithText("登录").performClick()
        
        verify { mockViewModel.login("testuser", "testpass") }
    }
}
```

## 8. 性能优化

### 8.1 图片加载优化
```kotlin
// utils/ImageLoader.kt
object ImageLoader {
    fun loadImage(
        url: String,
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        contentScale: ContentScale = ContentScale.Crop
    ): @Composable () -> Unit {
        return {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.error)
            )
        }
    }
}
```

### 8.2 网络请求优化
```kotlin
// data/remote/cache/NetworkCache.kt
@Singleton
class NetworkCache @Inject constructor(
    private val cache: LruCache<String, Any>
) {
    
    fun <T> get(key: String): T? {
        return cache.get(key) as? T
    }
    
    fun put(key: String, value: Any) {
        cache.put(key, value)
    }
    
    fun remove(key: String) {
        cache.remove(key)
    }
    
    fun clear() {
        cache.evictAll()
    }
}
```

### 8.3 内存管理
```kotlin
// utils/MemoryManager.kt
object MemoryManager {
    
    fun clearImageCache() {
        Glide.get(LocalContext.current).clearMemory()
    }
    
    fun trimMemory(level: Int) {
        when (level) {
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> {
                // UI隐藏时清理
            }
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> {
                // 内存紧张时清理
                clearImageCache()
            }
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
                // 内存严重不足时清理
                clearImageCache()
            }
        }
    }
}
```

## 9. 发布准备

### 9.1 签名配置
```gradle
// build.gradle (Module: app)
android {
    signingConfigs {
        release {
            storeFile file('keystore.jks')
            storePassword 'your_store_password'
            keyAlias 'your_key_alias'
            keyPassword 'your_key_password'
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 9.2 混淆配置
```proguard
# proguard-rules.pro
-keep class com.aiteacher.** { *; }
-keep class com.google.gson.** { *; }
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }

# 保留Gson序列化
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
```

### 9.3 版本管理
```gradle
// build.gradle (Module: app)
android {
    defaultConfig {
        versionCode getVersionCode()
        versionName getVersionName()
    }
}

def getVersionCode() {
    return System.getenv("BUILD_NUMBER")?.toInteger() ?: 1
}

def getVersionName() {
    return System.getenv("VERSION_NAME") ?: "1.0.0"
}
```

这份Android开发指南涵盖了从项目搭建到发布的全过程，包括架构设计、核心功能实现、数据层、网络层、测试策略、性能优化等各个方面。按照这个指南，您可以逐步实现AI教师系统的Android应用。
