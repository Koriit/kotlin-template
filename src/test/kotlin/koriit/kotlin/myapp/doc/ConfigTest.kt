package koriit.kotlin.myapp.doc

import com.uchuhimo.konf.Config
import java.io.File
import koriit.kotlin.myapp.MyApplication
import koriit.kotlin.slf4j.logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

internal class ConfigTest : KodeinAware {

    data class ConfigProp(
        val name: String,
        val description: String,
        val type: String,
        val required: Boolean,
        val default: String,
        val envVarName: String,
        val propertyName: String
    )

    override val kodein = MyApplication

    private val config: Config by instance()

    private val log = logger {}

    @Test
    fun `doc should match code`() {
        val source = analyzeConfig(config)
        val documentation = readDocumentation("src/docs/antora/modules/ROOT/pages/config.adoc")

        val errors = validate(source, documentation)

        if (errors.isNotEmpty()) {
            errors.forEach {
                log.error(it)
            }

            fail("There are ${errors.size} validation errors!")
        } else {
            log.info("OK!")
        }
    }

    private fun analyzeConfig(config: Config): List<ConfigProp> {
        return config.specs
            .map { spec ->
                spec.items.map { item ->
                    val prefix = if (spec.prefix != "") "${spec.prefix}." else ""
                    val type = item.type.rawClass.simpleName
                    val default = config[item]?.toString() ?: ""
                    val prop = prefix + item.name
                    val envVar = prop.toUpperCase().replace(".", "_")
                    val name = prop.split(".").joinToString(" ") {
                        val str = StringBuilder()
                        str.append(it[0])
                        for (i in 1 until it.length) {
                            // split camel case by inserting space between lower and upper case letters
                            if (it[i - 1].isLowerCase() && it[i].isUpperCase()) {
                                str.append(" ")
                            }
                            str.append(it[i])
                        }
                        str.toString().capitalize()
                    }

                    ConfigProp(name, item.description, type, item.isRequired, default, envVar, prop)
                }
            }
            .flatten()
    }

    private fun readDocumentation(docPath: String): List<ConfigProp> {
        return File(docPath)
            .readText()
            .substringAfter("|===")
            .substringBefore("|===")
            .lines()
            .run {
                val params: MutableList<MutableList<String>> = mutableListOf()
                // omit leading blank lines, header and trailing blank lines
                for (j in 2 until this.size - 2) {
                    // each config param is delimited with blank line
                    if (this[j].trim().isBlank()) {
                        params.add(mutableListOf())
                    } else {
                        params[params.size - 1].add(this[j].removePrefix("|").trim())
                    }
                }
                params
            }
            .map {
                ConfigProp(
                    name = it[0],
                    description = it[1],
                    type = it[2],
                    required = it[3].toUpperCase() == "YES" || it[3].toBoolean(),
                    default = it[4],
                    envVarName = it[5],
                    propertyName = it[6]
                )
            }
    }

    private fun validate(source: List<ConfigProp>, documentation: List<ConfigProp>): List<String> {
        val errors = mutableListOf<String>()

        val unmatched = source.map { it.propertyName }.toMutableSet()
        for (doc in documentation) {
            val src = source.find { it.propertyName == doc.propertyName }
            if (src == null) {
                errors.add("Unknown config param in the doc: ${doc.propertyName}")
                continue
            }
            unmatched.remove(doc.propertyName)

            fun addError(err: String) = errors.add("In param ${doc.propertyName}: $err")

            if (!doc.name.equals(src.name, ignoreCase = true)) addError("doc's name doesn't match source: \"${doc.name}\" != \"${src.name}\"")
            if (!doc.description.contains(src.description)) addError("doc's description doesn't contain source: \"${src.description}\"")
            if (doc.type != src.type) addError("doc's type doesn't match source: \"${doc.type}\" != \"${src.type}\"")
            if (doc.required != src.required) addError("doc's required doesn't match source: \"${doc.required}\" != \"${src.required}\"")
            if (doc.default != src.default) addError("doc's default doesn't match source: \"${doc.default}\" != \"${src.default}\"")
            if (doc.envVarName != src.envVarName) addError("doc's envVarName doesn't match source: \"${doc.envVarName}\" != \"${src.envVarName}\"")
            if (doc.propertyName != src.propertyName) addError("doc's propertyName doesn't match source: \"${doc.propertyName}\" != \"${src.propertyName}\"")
        }

        for (propertyName in unmatched) {
            val param = source.find { it.propertyName == propertyName }!!
            errors.add(
                """Doc is missing property $propertyName: $param
                ||${param.component1()}
                ||${param.component2()}
                ||${param.component3()}
                ||${param.component4()}
                ||${param.component5()}
                ||${param.component6()}
                ||${param.component7()}
            """.trimMargin()
            )
        }

        return errors
    }
}
