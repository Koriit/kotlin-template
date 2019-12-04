package koriit.kotlin.myapp.services

import koriit.kotlin.myapp.dao.EntityDAO
import koriit.kotlin.myapp.domain.Entity
import koriit.kotlin.myapp.exceptions.ItemNotFoundException

class EntityService(
    dao: EntityDAO
) {

    private val cache: MutableMap<Long, Entity>

    init {
        cache = dao.loadEntities().associateBy { it.id }.toMutableMap()
    }

    fun getEntity(id: Long): Entity {
        return cache[id] ?: throw ItemNotFoundException("Could not find entity id=$id")
    }

    fun getEntities(): List<Entity> {
        return cache.values.toList()
    }

    fun getEntityByCode(code: String): Entity {
        return cache.values.find { it.code == code }
            ?: throw ItemNotFoundException("Could not find entity code=$code")
    }

    fun addEntity(entity: Entity): Long {
        // + some extra validation
        val newId = cache.size + 1L
        cache[newId] = entity.copy(id = newId)

        return newId
    }

    fun updateEntity(id: Long, entity: Entity) {
        cache[id] = getEntity(id).copy(code = entity.code)
    }
}
