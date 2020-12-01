package no.nav.cv.eures.cv

import org.apache.kafka.common.security.plain.internals.PlainSaslServerProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.security.auth.Subject
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.login.LoginException
import javax.security.auth.spi.LoginModule

class NaisLoginModule : LoginModule {
    companion object {
        val log: Logger = LoggerFactory.getLogger(NaisLoginModule::class.java)
        init {
            PlainSaslServerProvider.initialize()
        }
    }

    override fun initialize(subject: Subject, callbackHandler: CallbackHandler,
                            sharedState: Map<String?, *>?, options: Map<String?, *>?) {
        val username = System.getenv("KAFKA_SERVICE_USER")
        log.info("Initialising kafka with user: $username")
        if (username != null) {

            subject.publicCredentials.add(username)
        }
        val password = System.getenv("KAFKA_SERVICE_PASSWORD")
        log.info("Continuing intializing kafka ${password?.length ?: "null"}")
        if (password != null) {
            subject.privateCredentials.add(password.trim())
        }
    }

    override fun login(): Boolean = true

    override fun logout(): Boolean = true

    override fun commit(): Boolean = true

    override fun abort(): Boolean = false
}