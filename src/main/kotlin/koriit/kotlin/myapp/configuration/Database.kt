package koriit.kotlin.myapp.configuration

import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import korrit.kotlin.kodein.application.ApplicationEvents.Start
import korrit.kotlin.kodein.application.on
import kotliquery.Connection
import kotliquery.Session
import kotliquery.TransactionalSession
import org.flywaydb.core.Flyway
import org.kodein.di.Kodein
import org.kodein.di.direct
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val database = Kodein.Module("database") {

    bind<HikariDataSource>() with singleton {
        HikariDataSource().apply {
            jdbcUrl = "jdbc:h2:./database.h2;CIPHER=AES"
            username = "sa"
            password = "qwerty qwerty"
        }
    }

    on(Start) {
        Flyway.configure()
            .dataSource(direct.instance())
            .load()
            .migrate()
    }
}

fun <T> sessionOf(dataSource: DataSource, returnGeneratedKey: Boolean = false, operation: (Session) -> T): T {
    return Session(Connection(dataSource.connection), returnGeneratedKey).use {
        operation(it)
    }
}

fun <T> transactionOf(dataSource: DataSource, returnGeneratedKey: Boolean = false, operation: (TransactionalSession) -> T): T {
    return Session(Connection(dataSource.connection), returnGeneratedKey).use {
        it.transaction(operation)
    }
}
