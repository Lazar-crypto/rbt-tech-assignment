package com.razal.rbtticketbooking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
class RbtTicketBookingApplication

fun main(args: Array<String>) {
    runApplication<RbtTicketBookingApplication>(*args)
}
