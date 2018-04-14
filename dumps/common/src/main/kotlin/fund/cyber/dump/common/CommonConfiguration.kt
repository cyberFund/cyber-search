package fund.cyber.dump.common

import fund.cyber.search.model.chains.Chain
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonConfiguration(
        private val chain: Chain
) {

    @Bean
    fun metricsCommonTags(): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer { registry -> registry.config().commonTags("chain", chain.name) }
    }
}
