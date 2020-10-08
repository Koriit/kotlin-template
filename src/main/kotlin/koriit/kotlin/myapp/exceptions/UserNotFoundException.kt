package koriit.kotlin.myapp.exceptions

class UserNotFoundException(user: String, cause: Throwable? = null) : RuntimeException("Could not find user: $user", cause)
