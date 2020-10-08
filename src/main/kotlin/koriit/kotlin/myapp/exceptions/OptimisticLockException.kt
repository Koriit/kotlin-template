package koriit.kotlin.myapp.exceptions

class OptimisticLockException(message: String, cause: Throwable? = null) : PersistenceException(message, cause)
