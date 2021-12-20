package com.example.sessionredis.security

import com.fasterxml.jackson.annotation.JsonCreator
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class LoginUser
@JsonCreator constructor(val id: Int, val authorities: List<SimpleGrantedAuthority>)
