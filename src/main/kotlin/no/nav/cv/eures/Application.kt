package no.nav.cv.eures

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {

        Thread.sleep(15_000)
        Micronaut.build()
                .packages("no.nav.cv")
                .mainClass(Application.javaClass)
                .start()
    }
}