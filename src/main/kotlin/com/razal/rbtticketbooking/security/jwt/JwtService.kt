package com.razal.rbtticketbooking.security.jwt

import RBT_LOGGER
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.razal.rbtticketbooking.shared.config.AppProperties
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class JwtService(
    private val appProps: AppProperties
) {
    private val signer = MACSigner(appProps.jwt.secret.toByteArray())
    private val verifier = MACVerifier(appProps.jwt.secret.toByteArray())

    fun issue(username: String, roles: Collection<String>): String {
        val now = Instant.now()
        val exp = now.plus(appProps.jwt.expirationMin, ChronoUnit.MINUTES)
        val claims = JWTClaimsSet.Builder()
            .subject(username)
            .claim("roles", roles)
            .issueTime(Date.from(now))
            .issuer(appProps.jwt.issuer)
            .expirationTime(Date.from(exp))
            .build()
        val jwt = SignedJWT(JWSHeader.Builder(JWSAlgorithm.HS256).build(), claims)
        jwt.sign(signer)
        RBT_LOGGER.debug("Generated JWT for subject={}, exp={}", username, exp)
        return jwt.serialize()
    }

    fun claimsIfValid(token: String): JWTClaimsSet? {
        return try {
            val jwt = SignedJWT.parse(token)

            if (jwt.header.algorithm != JWSAlgorithm.HS256) return null

            if (!jwt.verify(verifier)) return null

            val claimsSet = jwt.jwtClaimsSet
            if (claimsSet.issuer != appProps.jwt.issuer) return null
            val exp = claimsSet.expirationTime?.toInstant() ?: return null
            if (exp.isBefore(Instant.now())) return null

            claimsSet
        } catch (_: Exception) {
            null
        }
    }
}