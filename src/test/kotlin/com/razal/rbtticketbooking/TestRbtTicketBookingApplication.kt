package com.razal.rbtticketbooking

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<RbtTicketBookingApplication>().with(TestcontainersConfiguration::class).run(*args)
}
