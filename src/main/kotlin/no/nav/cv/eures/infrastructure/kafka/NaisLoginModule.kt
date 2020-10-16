package no.nav.cv.eures.infrastructure.kafka

import org.apache.kafka.common.security.plain.internals.PlainSaslServerProvider
import javax.security.auth.Subject
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.spi.LoginModule

class NaisLoginModule : LoginModule {

    init {
        PlainSaslServerProvider.initialize()
    }

    override fun login() = true
    override fun commit() = true
    override fun logout() = true
    override fun abort() = false

    override fun initialize(subject: Subject?, callbackHandler: CallbackHandler?, sharedState: MutableMap<String, *>?, options: MutableMap<String, *>?) {
        System.getenv("KAFKA_USER")
                ?.run {
                    subject?.publicCredentials?.add(this)
                }
        System.getenv("KAFKA_PWD")
                ?.run {
                    subject?.privateCredentials?.add(this)
                }
    }

}