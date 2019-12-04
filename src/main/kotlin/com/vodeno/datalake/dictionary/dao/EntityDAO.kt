package com.vodeno.datalake.dictionary.dao

import com.vodeno.datalake.dictionary.domain.Entity
import java.util.UUID

class EntityDAO {

    fun loadEntities(): List<Entity> {
        return (1..100L).map { Entity(id = it, code = UUID.randomUUID().toString()) }
    }
}
