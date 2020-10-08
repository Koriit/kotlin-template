package koriit.kotlin.myapp.exceptions

open class PersistenceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
