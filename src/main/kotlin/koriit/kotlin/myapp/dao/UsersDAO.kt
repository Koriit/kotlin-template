package koriit.kotlin.myapp.dao

import java.sql.SQLIntegrityConstraintViolationException
import java.time.OffsetDateTime
import java.time.OffsetDateTime.now
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit.MILLIS
import koriit.kotlin.myapp.domain.User
import koriit.kotlin.myapp.exceptions.DuplicateUserException
import koriit.kotlin.myapp.exceptions.OptimisticLockException
import kotliquery.Row
import kotliquery.Session
import kotliquery.queryOf

class UsersDAO {

    companion object {
        private const val UNIQUE_CONSTRAINT_VIOLATION_STATE = "23505"
    }

    fun getByLogin(session: Session, login: String): User? {
        val select = queryOf(
            "SELECT id, login, password_hash, name, age, last_update FROM users WHERE login = :login",
            mapOf(
                "login" to login
            )
        )
            .map(::toMember)
            .asSingle

        return session.run(select)
    }

    fun getById(session: Session, id: Long): User? {
        val select = queryOf(
            "SELECT id, login, password_hash, name, age, last_update FROM users WHERE id = :id",
            mapOf(
                "id" to id
            )
        )
            .map(::toMember)
            .asSingle

        return session.run(select)
    }

    fun getAll(session: Session, page: Int, pageSize: Int): List<User> {
        val select = queryOf(
            statement = "SELECT id, login, password_hash, name, age, last_update FROM users LIMIT :limit OFFSET :offset",
            paramMap = mapOf(
                "limit" to pageSize,
                "offset" to page * pageSize
            )
        )
            .map(::toMember)
            .asList

        return session.run(select)
    }

    fun insert(session: Session, user: User): User {
        val insert = queryOf(
            statement = """
                        INSERT INTO users (login, password_hash, name, age, last_update)
                        VALUES (:login, :password_hash, :name, :age, :last_update)
                        """,
            paramMap = mapOf(
                "login" to user.login,
                "password_hash" to user.passwordHash,
                "name" to user.name,
                "age" to user.age,
                "last_update" to user.lastUpdate.asUTCMillis()
            )
        ).asUpdateAndReturnGeneratedKey

        val id = try {
            session.run(insert)!!
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.sqlState == UNIQUE_CONSTRAINT_VIOLATION_STATE) throw DuplicateUserException("User '${user.login}' already exists", e)
            else throw e
        }

        return user.copy(id = id, lastUpdate = user.lastUpdate.asUTCMillis())
    }

    fun update(session: Session, user: User) {
        val update = queryOf(
            statement = """
                        UPDATE users 
                        SET name = :name, age = :age, last_update = :new_update
                        WHERE id = :id AND last_update = :last_update
                        """,
            paramMap = mapOf(
                "name" to user.name,
                "age" to user.age,
                "new_update" to now().asUTCMillis(),
                "last_update" to user.lastUpdate.asUTCMillis(),
                "id" to user.id
            )
        ).asUpdate

        val affectedRows = session.run(update)

        if (affectedRows == 0) {
            throw OptimisticLockException("Row with id '${user.id}' was concurrently updated by another session")
        }
    }

    private fun OffsetDateTime.asUTCMillis() = truncatedTo(MILLIS).withOffsetSameInstant(UTC)

    private fun toMember(row: Row) = User(
        id = row.long("id"),
        name = row.string("name"),
        age = row.int("age"),
        login = row.string("login"),
        passwordHash = row.string("password_hash"),
        lastUpdate = OffsetDateTime.ofInstant(row.instant("last_update"), UTC)
    )
}
