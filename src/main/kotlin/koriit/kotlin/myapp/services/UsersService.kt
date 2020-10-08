package koriit.kotlin.myapp.services

import javax.sql.DataSource
import koriit.kotlin.myapp.configuration.sessionOf
import koriit.kotlin.myapp.dao.UsersDAO
import koriit.kotlin.myapp.domain.NewUser
import koriit.kotlin.myapp.domain.User
import koriit.kotlin.myapp.exceptions.UserNotFoundException

class UsersService(
    private val usersDAO: UsersDAO,
    private val db: DataSource
) {

    fun getUser(id: Long): User {
        return sessionOf(db) { session ->
            usersDAO.getById(session, id) ?: throw UserNotFoundException("id=$id")
        }
    }

    fun getUsers(page: Int, pageSize: Int): List<User> {
        return sessionOf(db) { session ->
            usersDAO.getAll(session, page, pageSize)
        }
    }

    fun getUserByLogin(login: String): User {
        return sessionOf(db) { session ->
            usersDAO.getByLogin(session, login) ?: throw UserNotFoundException("login=$login")
        }
    }

    fun registerUser(user: NewUser): User {
        return sessionOf(db) { session ->
            val newUser = User(
                id = -1,
                login = user.login,
                passwordHash = user.password,
                name = user.name,
                age = user.age
            )

            usersDAO.insert(session, newUser)
        }
    }

    fun update(user: User) {
        sessionOf(db) { session ->
            usersDAO.update(session, user)
        }
    }
}
