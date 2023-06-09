@file:Suppress("unused")

package finalexercise.end

import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Subcomponent
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Scope
import javax.inject.Singleton

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class FeatureScoped

interface Context {
    fun getName(): String
}

class ContextImpl @Inject constructor() : Context {
    init {
        println("CREATE: ContextImpl")
    }

    override fun getName(): String = "Patrik"
}

interface MovieRepository {
    fun getGreetings(): String
}

class MovieRepositoryImpl @Inject constructor(private val context: Context) : MovieRepository {
    init {
        println("CREATE: MovieRepositoryImpl")
    }

    override fun getGreetings(): String = "Hey ${context.getName()}"
}

@Module
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindRepository(impl: MovieRepositoryImpl): MovieRepository
}

interface CommonProvider {
    val appContext: Context
}

@Component
@Singleton
interface CommonComponent : CommonProvider {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): CommonComponent
    }
}

interface DataProvider {
    val movieRepository: MovieRepository
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MovieId

fun interface GetMovie {
    suspend operator fun invoke(movieId: String): String
}

class GetMovieUseCase @Inject constructor(
    private val repository: MovieRepository,
    private val greetingsHolder: GreetingsHolder,
) : GetMovie {
    init {
        println("CREATE: GetMovieUseCase")
    }

    override suspend fun invoke(movieId: String) = if (greetingsHolder.greetings.isEmpty()) {
        repository.getGreetings() + movieId
    } else {
        greetingsHolder.greetings
    }
}

@FeatureScoped
class GreetingsHolder @Inject constructor() {
    init {
        println("CREATE: Greetings holder")
    }

    var greetings: String = ""
}

@Module
abstract class MovieDetailsModule {
    @Binds
    abstract fun bindGetMovieUseCase(useCase: GetMovieUseCase): GetMovie
}

interface MovieDetailsProvider

@FeatureScoped
@Component(
    dependencies = [DataProvider::class, CommonProvider::class],
    modules = [MovieDetailsModule::class]
)
interface MovieDetailsComponent : MovieDetailsProvider {
    @get:MovieId
    val movieId: String

    val getMovie: GetMovie

    @Component.Factory
    interface Factory {
        fun create(
            dataProvider: DataProvider,
            commonProvider: CommonProvider,
            @BindsInstance @MovieId movieId: String
        ): MovieDetailsComponent
    }
}

@Singleton
@Component(
    dependencies = [CommonProvider::class],
    modules = [DataModule::class]
)
interface DataComponent : DataProvider

interface AppProvider : DataProvider, CommonProvider

@Module(subcomponents = [LoginComponent::class])
interface SubcomponentsModule

@Singleton
@Component(
    dependencies = [DataProvider::class, CommonProvider::class],
    modules = [SubcomponentsModule::class]
)
interface AppComponent : AppProvider {
    val loginComponentFactory: LoginComponent.Factory
}

@FeatureScoped
class LoginRepository @Inject constructor(private val context: Context) {
    init {
        println("CREATE: LoginRepository")
    }

    fun doStuff() {
        println("Login: ${context.getName()}")

    }
}

@Subcomponent
@FeatureScoped
interface LoginComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginComponent
    }

    val loginRepository: LoginRepository
}


fun main(args: Array<String>) {
    val commonProvider = DaggerCommonComponent.factory().create(ContextImpl())

    val appComponent = DaggerAppComponent.builder()
        .commonProvider(commonProvider)
        .dataProvider(
            DaggerDataComponent.builder()
                .commonProvider(commonProvider)
                .build()
        )
        .build()

    val movieDetailsComponent = DaggerMovieDetailsComponent.factory()
        .create(appComponent, appComponent, "42")

    val getMovie = movieDetailsComponent.getMovie

    val getMovie2 = movieDetailsComponent.getMovie

    appComponent.loginComponentFactory.create()
        .loginRepository.doStuff()
}