package koriit.kotlin.myapp.dao

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.spyk
import koriit.kotlin.myapp.domain.Entity

fun newEntityDAOMock(jackson: ObjectMapper): EntityDAO {
    val dao = spyk(EntityDAO())

    val data = jackson.readValue(
        {}.javaClass.getResourceAsStream("/entities_data.json"),
        object : TypeReference<List<Entity>>() {}
    )!!

    every { dao.loadEntities() } returns data

    return dao
}
