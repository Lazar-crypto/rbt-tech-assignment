package com.razal.rbtticketbooking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class RbtTicketBookingApplication

fun main(args: Array<String>) {
    runApplication<RbtTicketBookingApplication>(*args)
}
