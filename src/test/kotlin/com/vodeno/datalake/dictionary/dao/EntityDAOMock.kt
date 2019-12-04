package com.vodeno.datalake.dictionary.dao

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vodeno.datalake.dictionary.domain.Entity
import io.mockk.every
import io.mockk.spyk

fun newEntityDAOMock(jackson: ObjectMapper): EntityDAO {
    val dao = spyk(EntityDAO())

    val data = jackson.readValue(
        {}.javaClass.getResourceAsStream("/entities_data.json"),
        object : TypeReference<List<Entity>>() {}
    )!!

    every { dao.loadEntities() } returns data

    return dao
}
