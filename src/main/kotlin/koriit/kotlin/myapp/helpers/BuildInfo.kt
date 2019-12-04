package koriit.kotlin.myapp.helpers

import com.fasterxml.jackson.annotation.JsonProperty

data class BuildInfo(
    @JsonProperty("build.date")
    val buildDate: String = "",
    @JsonProperty("custom")
    val custom: Map<String, String> = emptyMap(),
    @JsonProperty("dependencies")
    val dependencies: List<String> = listOf(),
    @JsonProperty("java.vendor")
    val javaVendor: String = "",
    @JsonProperty("java.version")
    val javaVersion: String = "",
    @JsonProperty("os.arch")
    val osArch: String = "",
    @JsonProperty("os.name")
    val osName: String = "",
    @JsonProperty("os.version")
    val osVersion: String = "",
    @JsonProperty("project.name")
    val projectName: String = "",
    @JsonProperty("project.version")
    val projectVersion: String = "",
    @JsonProperty("source.compatibility")
    val sourceCompatibility: String = "",
    @JsonProperty("target.compatibility")
    val targetCompatibility: String = "",
    @JsonProperty("user.name")
    val userName: String = ""
)
