package koriit.kotlin.myapp.services

import koriit.kotlin.myapp.TestApplication
import koriit.kotlin.myapp.domain.Entity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.kodein.di.KodeinAware
import org.kodein.di.direct
import org.kodein.di.generic.instance
import org.kodein.di.newInstance

class EntityServiceTest : KodeinAware {

    override val kodein = TestApplication()

    private val service: EntityService by instance()

    @Test
    fun getEntities() {
        val entities = service.getEntities()

        assertThat(entities, hasSize(5))
    }

    @Test
    fun getEntity() {
        val item = service.getEntity(50)

        assertThat(item.id, equalTo(50L))
        assertThat(item.code, equalTo("ENTITY_50"))
    }

    @Test
    fun getEntityByCode() {
        val item = service.getEntityByCode("ENTITY_50")

        assertThat(item.id, equalTo(50L))
        assertThat(item.code, equalTo("ENTITY_50"))
    }

    @Test
    fun addEntity() {
        val service = direct.newInstance { EntityService(instance()) }

        val entity = Entity(1337, "1337")
        val entityId = service.addEntity(entity)

        assertThat(entityId, not(entity.id))

        val itemById = service.getEntity(entityId)
        assertThat(itemById.id, equalTo(entityId))
        assertThat(itemById.code, equalTo("1337"))

        val itemByCode = service.getEntityByCode("1337")
        assertThat(itemByCode.id, equalTo(entityId))
        assertThat(itemByCode.code, equalTo("1337"))
    }
}
