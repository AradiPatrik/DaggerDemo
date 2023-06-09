package example

import dagger.Binds
import dagger.Component
import dagger.Module
import javax.inject.Inject
import javax.inject.Scope
import javax.inject.Singleton

@Singleton
class Config @Inject constructor(private val context: Context) {
    fun getAppNameWithVersion() = context.getAppName() + "1.0.0"
}

class Context @Inject constructor() {
    fun getAppName() = "app_name"
}

fun interface GetGreetings {
    operator fun invoke(): String
}

class GetGreetingsUseCase @Inject constructor(
    private val config: Config,
    private val context: Context
): GetGreetings {
    override fun invoke(): String {
        return config.getAppNameWithVersion() + " " + context.getAppName()
    }
}

@Module
interface GreetingBindingsModule {
    @Binds
    fun bindGreetings(impl: GetGreetingsUseCase): GetGreetings
}

@Singleton
class MyViewModel @Inject constructor(private val getGreetings: GetGreetings) {
    fun useGreetingsUseCase() { println(getGreetings()) }
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenScope

@Component(modules = [
    GreetingBindingsModule::class,
])
@Singleton
interface AppComponent: GreetingsProvider {
    val myViewModel: MyViewModel
}

interface GreetingsProvider {
    val getGreetings: GetGreetings
}

@Component(
    dependencies = [GreetingsProvider::class]
)
@ScreenScope
interface LoginComponent {
    val loginScreen: LoginScreen
}

@ScreenScope
class LoginScreen @Inject constructor(private val loginVM: LoginVM) {
    fun show() = loginVM.showGreetings()
}

class LoginVM @Inject constructor(private val getGreetings: GetGreetings) {
    fun showGreetings() = getGreetings().let(::println)
}

fun main() {
    val appComponent = DaggerAppComponent.create()
    val loginComponent = DaggerLoginComponent.builder().greetingsProvider(appComponent).build()
    loginComponent.loginScreen.show()
}