package ca.aversa.insessionservice.util

import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [ListPatternValidator::class])
annotation class ListPattern(

    val regex: String = "",
    val message: String = "List elements do not match pattern",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class ListPatternValidator: ConstraintValidator<ListPattern, List<String>> {

    var annotation: ListPattern? = null
    var regex: Regex? = null

    override fun initialize(constraintAnnotation: ListPattern?) {
        constraintAnnotation?.regex?.also {
            regex = it.toRegex()
        }
    }

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext?): Boolean {
        if(value == null || regex == null) {
            return false
        }

        value.forEach{string ->
            if(!string.matches(regex!!)) {
                return false
            }
        }

        return true
    }
}
