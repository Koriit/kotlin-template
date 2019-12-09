package koriit.kotlin.myapp.configuration.spec

import com.uchuhimo.konf.ConfigSpec

object ApplicationConfig : ConfigSpec("") {
    object Apis : ConfigSpec() {
        object Model : ConfigSpec() {
            val service by required<String>()
        }
    }
}
