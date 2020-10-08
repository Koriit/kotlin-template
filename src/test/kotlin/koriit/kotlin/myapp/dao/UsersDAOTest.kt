package koriit.kotlin.myapp.dao

import java.util.UUID
import javax.sql.DataSource
import koriit.kotlin.myapp.TestApplication
import koriit.kotlin.myapp.configuration.sessionOf
import koriit.kotlin.myapp.domain.User
import koriit.kotlin.myapp.exceptions.DuplicateUserException
import koriit.kotlin.myapp.exceptions.OptimisticLockException
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotliquery.Session
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

internal class UsersDAOTest : KodeinAware {

    override val kodein = TestApplication()

    private val dao: UsersDAO by instance()

    private val db: DataSource by instance()

    @Test
    fun `should insert new users`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val newUser = User(0, "name", 18, "login", "pass")
            val createdUser = dao.insert(session, newUser)
            assertNotEquals(newUser.id, createdUser.id)
            assertEquals(createdUser, newUser.copy(id = createdUser.id, lastUpdate = createdUser.lastUpdate))

            val user = dao.getById(session, createdUser.id)
            assertEquals(createdUser, user)
        }
    }

    @Test
    fun `should fetch users by login`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val createdUser = session.insertNewUser()

            val user = dao.getByLogin(session, createdUser.login)
            assertEquals(createdUser, user)
        }
    }

    @Test
    fun `should fetch all users`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val createdUser = session.insertNewUser()

            val users = dao.getAll(session, 0, 1000)
            assertEquals(createdUser, users.first { it.id == createdUser.id })
        }
    }

    @Test
    fun `should fetch users by id`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val createdUser = session.insertNewUser()

            val user = dao.getById(session, createdUser.id)
            assertEquals(createdUser, user)
        }
    }

    @Test
    fun `should update user fields`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val user = session.insertNewUser()

            val update = user.copy(name = "new", age = 20, login = "new", passwordHash = "new")
            dao.update(session, update)

            val updated = dao.getById(session, user.id)!!

            assertEquals(user.login, updated.login)
            assertEquals(user.passwordHash, updated.passwordHash)

            assertEquals(update.name, updated.name)
            assertEquals(update.age, updated.age)
        }
    }

    @Test
    fun `should validate login unique constraint`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val user = session.insertNewUser()

            assertThrows<DuplicateUserException> {
                dao.insert(session, user)
            }
        }
    }

    @Test
    fun `should implement optimistic locking`() {
        sessionOf(db, returnGeneratedKey = true) { session ->
            val user = session.insertNewUser()

            dao.update(session, user)

            assertThrows<OptimisticLockException> {
                dao.update(session, user)
            }
        }
    }

    private fun Session.insertNewUser(): User {
        val newUser = User(0, "name", 18, "login" + UUID.randomUUID(), "pass")
        return dao.insert(this, newUser)
    }
}
