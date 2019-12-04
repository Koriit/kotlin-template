package koriit.kotlin.myapp.dao

import java.util.UUID
import koriit.kotlin.myapp.domain.Entity

class EntityDAO {

    fun loadEntities(): List<Entity> {
        return (1..100L).map { Entity(id = it, code = UUID.randomUUID().toString()) }
    }
}
