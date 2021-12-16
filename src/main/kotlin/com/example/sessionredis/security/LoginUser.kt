package com.example.sessionredis.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

class LoginUser(val id: Int, email: String, roles: List<String>) :
    User(email, null, roles.map { SimpleGrantedAuthority(it) })
