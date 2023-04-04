package ca.aversa.insessionservice.context

import ca.aversa.insessionservice.config.AppConfig
import ca.aversa.insessionservice.model.UserRole
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.gson.Gson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.security.InvalidParameterException
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthFilter(

    appConfig: AppConfig,
    private val gson: Gson
): Filter {

    // TODO Change namespace to auth0 tenant domain once custom domain has been set up
    private val claimsNamespace = "https://auth.aversa.ca/"
    private val bearerTokenPrefix = "Bearer "
    private val allowedUnauthenticatedPaths = setOf(
        "/health",
        "/auth/sms-gateway",
        "/invitation/clinician",
        "/invitation/approve",
        "/invitation/reject",
    )
    private val jwkProvider: JwkProvider = UrlJwkProvider(appConfig.auth0Tenant)
    private val keyIdToPublicKeyMap = mutableMapOf<String, RSAPublicKey>()
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val httpServletRequest = request as HttpServletRequest
        val httpServletResponse = response as HttpServletResponse

        try {
            // Check if request is for unauthenticated paths such as health endpoint
            allowedUnauthenticatedPaths.forEach {path ->
                if (httpServletRequest.requestURI.contains(path)) {
                    chain!!.doFilter(httpServletRequest, httpServletResponse)

                    return
                }
            }

            val authorizationHeaderValue: String? = httpServletRequest.getHeader("Authorization")

            if(authorizationHeaderValue == null || !authorizationHeaderValue.startsWith(bearerTokenPrefix)) {
                setUnauthorizedResponse(httpServletResponse)

                return
            }

            // Extract and decode JWT
            val token = authorizationHeaderValue.replace(bearerTokenPrefix, "")
            val decodedJWT = decodeJwt(token)

            // Initialize public key and algorithm
            val publicKey = getPublicKey(decodedJWT)
            val algorithm = Algorithm.RSA256(publicKey, null as RSAPrivateKey?)

            // Verify JWT
            val verifier = JWT.require(algorithm)
                .withIssuer(decodedJWT.issuer)
                .build()
            verifier.verify(decodedJWT)

            // Create and set the context to request attribute
            val context = createContext(decodedJWT)
            httpServletRequest.setAttribute(RequestAttributeType.CONTEXT.toString(), context)

            chain!!.doFilter(httpServletRequest, httpServletResponse)
        }
        catch (e: Exception) {
            log.warn("Exception in authfiler: ${e.message}")

            setUnauthorizedResponse(httpServletResponse)

            return
        }
    }

    private fun setUnauthorizedResponse(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(
            gson.toJson(mapOf(
                "message" to "Unauthorized"
            ))
        )
    }

    private fun getPublicKey(token: DecodedJWT): RSAPublicKey {
        return keyIdToPublicKeyMap.getOrElse(token.keyId, {
            val key = jwkProvider.get(token.keyId).publicKey as RSAPublicKey

            keyIdToPublicKeyMap[token.keyId] = key

            return key
        })
    }

    private fun decodeJwt(token: String): DecodedJWT {
        return JWT.decode(token) ?: throw InvalidParameterException("Invalid jwt: $token")
    }

    private fun createContext(jwt: DecodedJWT): Context {
        val claims = jwt.claims
        val id = claims.getValue("${claimsNamespace}id").asString()
        val userMetadata = claims.getValue("${claimsNamespace}user_metadata").asMap()
        val userRole: String = userMetadata["role"] as? String
            ?: throw NoSuchElementException("Role is missing from user metadata")

        return Context(UserRole.valueOf(userRole), id)
    }
}