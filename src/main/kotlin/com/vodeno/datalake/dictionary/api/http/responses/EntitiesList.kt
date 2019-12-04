package com.vodeno.datalake.dictionary.api.http.responses

import com.vodeno.datalake.dictionary.domain.Entity

data class EntitiesList(
    val entities: List<Entity>
)
