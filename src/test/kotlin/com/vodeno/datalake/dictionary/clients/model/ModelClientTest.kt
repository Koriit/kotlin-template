package com.vodeno.datalake.dictionary.clients.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.vodeno.datalake.dictionary.TestApplication
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

internal class ModelClientTest : KodeinAware {

    override val kodein = TestApplication()

    private val client: ModelClient by instance()
    private val jackson: ObjectMapper by instance()

    @Test
    fun `should fetch table models`() {
        val modelsResponse = client.getModels()

        val model = jackson.readTree(modelsResponse)

        assertTrue(model.has("nodes"))

        val nodes = model["nodes"]

        assertTrue(nodes.isArray)
        assertThat(nodes.size(), greaterThan(0))
        assertTrue(nodes[0]["name"].isTextual)

        val modelName = nodes[0]["name"].asText()
        val modelResponse = client.getModel(modelName)
        val tableModel = jackson.readTree(modelResponse)

        assertThat(tableModel["name"].asText(), equalTo(modelName))
    }
}
