package no.nav.cv.eures.infrastructure.database

import com.zaxxer.hikari.HikariDataSource
import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration
import io.micronaut.configuration.jdbc.hikari.DatasourceFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.*
import no.nav.vault.jdbc.hikaricp.HikariCPVaultUtil
import org.slf4j.LoggerFactory
import javax.annotation.PreDestroy


@Factory
@Requires(notEnv = [ "test", "local" ])
@Replaces(factory = DatasourceFactory::class)
class VaultDatasourceFactory(
        @Value("\${db.vault.path}") private val vaultPath: String,
        @Value("\${db.vault.username}") private val username: String,
        private val applicationContext: ApplicationContext
) : AutoCloseable {

    companion object {
        private val LOG = LoggerFactory.getLogger(DatasourceFactory::class.java)
    }

    private val dataSources: MutableList<HikariDataSource> = mutableListOf()


    @Context
    @EachBean(DatasourceConfiguration::class)
    fun vaultDataSource(datasourceConfiguration: DatasourceConfiguration ): HikariDataSource {
        val ds = HikariCPVaultUtil.createHikariDataSourceWithVaultIntegration(datasourceConfiguration, vaultPath, username)
        dataSources.add(ds)
        return ds
    }

    @Override
    @PreDestroy
    override fun close() {
        for (dataSource in dataSources) {
            try {
                dataSource.close();
            } catch (e: Exception) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Error closing data source [" + dataSource + "]: " + e.message, e);
                }
            }
        }
    }

}