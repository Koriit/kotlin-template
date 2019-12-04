package com.vodeno.datalake.dictionary.configuration.spec

import com.uchuhimo.konf.ConfigSpec

object ApplicationConfig : ConfigSpec("") {
    val env by required<String>()
    val modelService by required<String>()
}
