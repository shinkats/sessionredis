package com.example.sessionredis

import com.example.sessionredis.entity.User
import com.example.sessionredis.repository.UserRepository
import com.example.sessionredis.security.EmailAndPasswordJsonRequest
import com.example.sessionredis.security.LoginUser
import com.example.sessionredis.security.WebSecurityConfig.Companion.IS_AUTHENTICATED_FULLY
import com.example.sessionredis.security.WebSecurityConfig.Companion.ROLE_NORMAL
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException


@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@RestController
class Controller(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @PostMapping(path = ["/api/signup"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun signup(@RequestBody body: EmailAndPasswordJsonRequest): String {
        val password = passwordEncoder.encode(body.password)
        val user = userRepository.save(User(email = body.email, password = password))
        // ログイン済とみなす
        val loginUser = LoginUser(user.id!!, user.roles.map { SimpleGrantedAuthority(it) })
        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(loginUser, password, loginUser.authorities)
//        httpServletRequest.changeSessionId() // session fixation対策
        return """{ "id": ${user.id} }"""
    }

    @GetMapping("/api/non-personal")
    fun nonPersonal(@AuthenticationPrincipal loginUser: LoginUser?): String {
        return if (loginUser == null) {
            "everyone can see. not logged in."
        } else {
            "everyone can see. logged in."
        }
    }

    @Secured(IS_AUTHENTICATED_FULLY) // ログインしていればアクセス可能
    @GetMapping("/api/personal/user")
    fun personalUser(@AuthenticationPrincipal loginUser: LoginUser): User =
        userRepository.findById(loginUser.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    @PreAuthorize("hasRole('$ROLE_NORMAL')") // ログイン時にDBから取得した権限に指定のものが含まれていればアクセス可能
    @GetMapping(path = ["/api/personal/user"], params = ["role"])
    fun personalUserWithRole(@AuthenticationPrincipal loginUser: LoginUser): User =
        userRepository.findById(loginUser.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

    /**
     * POST/PUT/DELETEを実行する際に、CSRF TOKEをHTTPヘッダーに X-CSRF-TOKEN: {CSRF TOKEN} のように設定する必要がある。
     * そのため初めてPOST等を実行する前に、このAPIを呼び出してセッションを（なければ）作成しCSRF TOKENを取得する。
     */
    @GetMapping(path = ["/api/csrf-token"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun csrfToken(csrfToken: CsrfToken): String {
        return """{ "token": "${csrfToken.token}" }"""
    }
    // CsrfTokenにDIしてくれるため、以下のようなコードは不要
//    fun csrfToken(httpServletRequest: HttpServletRequest): String {
//        val csrfToken = httpServletRequest.getAttribute(CsrfToken::class.java.name) as CsrfToken
//        return """{ "token": "${csrfToken.token}" }"""
//    }
}
