package com.razal.rbtticketbooking.security.service

import com.razal.rbtticketbooking.shared.config.AppProperties
import org.springframework.stereotype.Service

@Service
class RoleService(private val props: AppProperties) {

    fun rolesFor(username: String): List<String> =
        if (props.adminUsers.contains(username)) listOf("ADMIN","USER") else listOf("USER")
}