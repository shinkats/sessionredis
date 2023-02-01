package com.example.sessionredis.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true) // for @PreAuthorize, @Secured
class WebSecurityConfig(
    private val jsonRequestAuthenticationProvider: JsonRequestAuthenticationProvider,
    private val objectMapper: ObjectMapper
) : WebSecurityConfigurerAdapter() {

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/images/**", "/js/**", "/css/**")
    }

    override fun configure(http: HttpSecurity) {
        val jsonAuthFilter = JsonRequestAuthenticationFilter(objectMapper)
        jsonAuthFilter.setRequiresAuthenticationRequestMatcher(AntPathRequestMatcher("/api/login", "POST"))
        jsonAuthFilter.setSessionAuthenticationStrategy(ChangeSessionIdAuthenticationStrategy()) // session fixation対策. これがないとsignup時にhttpServletRequest.changeSessionId()が必要
        jsonAuthFilter.setAuthenticationSuccessHandler { _, response, _ -> response.status = 200 }
        jsonAuthFilter.setAuthenticationManager(authenticationManagerBean())
        http.addFilter(jsonAuthFilter)

        http.logout()
            .logoutUrl("/api/logout")
            .invalidateHttpSession(true)
            .logoutSuccessHandler { _, response, _ -> response.status = 200 }
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(jsonRequestAuthenticationProvider)
    }

    companion object {
        const val IS_AUTHENTICATED_ANONYMOUSLY = "IS_AUTHENTICATED_ANONYMOUSLY"
        const val IS_AUTHENTICATED_REMEMBERED = "IS_AUTHENTICATED_REMEMBERED"
        const val IS_AUTHENTICATED_FULLY = "IS_AUTHENTICATED_FULLY"

        const val ROLE_NORMAL = "ROLE_NORMAL"
        const val ROLE_PREMIUM = "ROLE_PREMIUM"
    }
}
