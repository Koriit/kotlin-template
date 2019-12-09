package koriit.kotlin.myapp

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.yaml
import io.ktor.server.testing.TestApplicationEngine
import koriit.kotlin.myapp.api.http.serverConfig
import koriit.kotlin.myapp.clients.model.ModelClient
import koriit.kotlin.myapp.clients.model.newModelClientHttpMock
import koriit.kotlin.myapp.configuration.spec.ApplicationConfig.Apis.Model
import koriit.kotlin.myapp.dao.EntityDAO
import koriit.kotlin.myapp.dao.newEntityDAOMock
import koriit.kotlin.slf4j.logger
import koriit.kotlin.slf4j.mdc.correlation.correlateThread
import org.kodein.di.Copy
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.slf4j.Logger.ROOT_LOGGER_NAME
import org.slf4j.LoggerFactory

private val log = logger {}

const val MODULE_TEST = "test"

val testConfiguration = Kodein.Module(MODULE_TEST) {

    bind<Config>(overrides = true) with eagerSingleton {
        val config = (overriddenInstance() as Config).from.yaml.resource("config-test.yaml")
        log.info("{}", config)
        config
    }

    bind<EntityDAO>(overrides = true) with singleton {
        newEntityDAOMock(instance())
    }

    bind<ModelClient>(overrides = true) with singleton {
        val config: Config = instance()

        ModelClient(httpClient = newModelClientHttpMock(), serviceUrl = config[Model.service])
    }

    bind<TestApplicationEngine>() with singleton {
        TestApplicationEngine(serverConfig())
    }
}

@Suppress("TestFunctionName")
fun TestApplication(extraConfig: Kodein.MainBuilder.() -> Unit = {}) = Kodein {
    correlateThread()
    val rootLogger = LoggerFactory.getLogger(ROOT_LOGGER_NAME) as Logger
    rootLogger.level = Level.DEBUG

    extend(MyApplication, copy = Copy.All)
    import(testConfiguration, allowOverride = true)

    extraConfig()
}
