@file:Suppress("unused")

package finalexercise.begin

interface Context {
    fun getName(): String
}

class ContextImpl : Context {
    init {
        println("CREATE: ContextImpl")
    }

    override fun getName(): String = "Patrik"
}

interface MovieRepository {
    fun getGreetings(): String
}

class MovieRepositoryImpl(private val context: Context) : MovieRepository {
    init {
        println("CREATE: MovieRepositoryImpl")
    }

    override fun getGreetings(): String = "Hey ${context.getName()}"
}

fun interface GetMovie {
    suspend operator fun invoke(movieId: String): String
}

class GetMovieUseCase (
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

class GreetingsHolder {
    init {
        println("CREATE: Greetings holder")
    }

    var greetings: String = ""
}

class LoginRepository(private val context: Context) {
    init {
        println("CREATE: LoginRepository")
    }

    fun doStuff() {
        println("Login: ${context.getName()}")
    }
}


fun main(args: Array<String>) {

}