package koriit.kotlin.myapp.domain

import java.time.OffsetDateTime
import java.time.OffsetDateTime.now

data class User(
    val id: Long,
    val name: String,
    val age: Int,
    val login: String,
    val passwordHash: String?,
    val lastUpdate: OffsetDateTime = now()
)

data class NewUser(
    val name: String,
    val age: Int,
    val login: String,
    val password: String
)
