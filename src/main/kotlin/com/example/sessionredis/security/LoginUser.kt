package com.example.sessionredis.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.io.Serializable

class LoginUser(val id: Int, roles: List<String>) : Serializable {
    val authorities = roles.map { SimpleGrantedAuthority(it) }
}
