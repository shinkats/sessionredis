package com.example.sessionredis.security

import com.fasterxml.jackson.annotation.JsonCreator

data class EmailAndPasswordJsonRequest
@JsonCreator constructor(val email: String, val password: String)
