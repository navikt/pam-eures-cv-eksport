package no.nav.cv

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.nav.cv")
                .mainClass(Application.javaClass)
                .start()
    }
}