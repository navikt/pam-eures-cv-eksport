package no.nav.cv.eures.scheduled

import io.micrometer.core.instrument.Meter
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusNamingConvention
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfig {

    /**
     * Micrometer 1.14+ fjerner `_total` suffixet fra metrikker som ikke er counter.
     * Dette knekker kompabilitet på tvers av noen jobber for datastories. (e.g. "cv.eures.eksport.antall.samtykker.total").
     * TODO: Fjernes etter at Eures-teamet har gått over til ny metrikkløsning i metabase (ca høsten 2026)
     */
    @Bean
    fun prometheusGaugeTotalSuffixCustomizer(): MeterRegistryCustomizer<PrometheusMeterRegistry> {
        return MeterRegistryCustomizer { registry ->
            registry.config().namingConvention(object : PrometheusNamingConvention() {
                override fun name(name: String, type: Meter.Type, baseUnit: String?): String {
                    val prometheusName = super.name(name, type, baseUnit)
                    return if (type == Meter.Type.GAUGE && name.endsWith(".total") && !prometheusName.endsWith("_total")) {
                        "${prometheusName}_total"
                    } else {
                        prometheusName
                    }
                }
            })
        }
    }
}
