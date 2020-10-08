package koriit.kotlin.myapp.api.http.responses

import koriit.kotlin.myapp.domain.User

data class UserDetails(
    val id: Long,
    val login: String,
    val name: String,
    val age: Int
) {
    constructor(user: User) : this(user.id, user.login, user.name, user.age)
}
