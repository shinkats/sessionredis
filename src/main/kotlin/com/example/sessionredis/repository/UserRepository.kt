package com.example.sessionredis.repository

import com.example.sessionredis.entity.User
import org.springframework.data.repository.CrudRepository
import java.util.*

interface UserRepository : CrudRepository<User, Int> {
    fun findByEmail(email: String): Optional<User>
}
