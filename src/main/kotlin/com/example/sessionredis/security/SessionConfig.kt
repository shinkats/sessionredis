package com.example.sessionredis.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules

@Configuration
class SessionConfig {
    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer(redisCacheObjectMapper())
    }

    private fun redisCacheObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper
            .registerModule(JavaTimeModule())
            .registerModule(ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
            .registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .activateDefaultTyping(
                objectMapper.polymorphicTypeValidator,
                DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY
            )
        GenericJackson2JsonRedisSerializer.registerNullValueSerializer(objectMapper, null)
        return objectMapper
    }
}
