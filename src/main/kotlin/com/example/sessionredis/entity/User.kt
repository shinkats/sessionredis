package com.example.sessionredis.entity

import com.example.sessionredis.jpa.converter.JpaConverterJson
import com.example.sessionredis.security.WebSecurityConfig
import javax.persistence.*

@Entity(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(nullable = false, unique = true)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Convert(converter = JpaConverterJson::class)
    @Column(nullable = false, columnDefinition = "json")
    var roles: List<String> = listOf(WebSecurityConfig.ROLE_NORMAL)
)
