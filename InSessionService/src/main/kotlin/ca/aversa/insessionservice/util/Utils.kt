package ca.aversa.insessionservice.util

import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator
import java.security.SecureRandom
import java.util.*

object Utils {

    private val secureRandom = SecureRandom()

    /**
     * This function will create a crypto-generated random string of specified length to generate bytes for
     */
    fun createSecureId(length: Int = 20): String {
        val bytes = ByteArray(length)

        secureRandom.nextBytes(bytes)

        return String(Base64.getUrlEncoder().encode(bytes))
            .replace("+", "_")
            .replace("/", "_")
            .replace("=", "_")
    }

    /**
     * This function will create a contains filter expression along with it's values for DynamoDB.
     */
    fun createContainsFilterExpression(
        attributeName: String, attributePlaceholder: String,
        values: List<String>, conditionalOperator: ConditionalOperator
    ): Pair<String, Map<String, AttributeValue>> {
        val expression = values.mapIndexed { i, _ ->
            "contains($attributeName, $attributePlaceholder$i)"
        }.joinToString(" ${conditionalOperator.name} ")

        val attributeValueMap = values.mapIndexed { i, keyword ->
            "$attributePlaceholder$i" to AttributeValue().withS(keyword)
        }.toMap()

        return Pair(expression, attributeValueMap)
    }
}