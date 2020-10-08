package koriit.kotlin.myapp.exceptions

/**
 * Thrown when operation would result in duplicated user.
 */
class DuplicateUserException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
