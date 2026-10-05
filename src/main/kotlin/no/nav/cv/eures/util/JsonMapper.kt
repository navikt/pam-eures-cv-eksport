package no.nav.cv.eures.util

import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

fun jsonMapper(): JsonMapper = JsonMapper.builder()
    .configureForJackson2()
    .addModule(KotlinModule.Builder().build())
    .build()
