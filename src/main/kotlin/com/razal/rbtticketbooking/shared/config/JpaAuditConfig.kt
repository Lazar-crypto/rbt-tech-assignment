package com.razal.rbtticketbooking.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.DateTimeProvider
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor
import java.util.Optional

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "offsetDateTimeProvider")
class JpaAuditConfig {

    @Bean
    fun offsetDateTimeProvider(): DateTimeProvider = DateTimeProvider {
        Optional.of(OffsetDateTime.now() as TemporalAccessor)
    }
}