package ca.aversa.insessionservice.util

import java.time.ZonedDateTime
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [FutureZonedDateTimeValidator::class])
annotation class FutureZonedDateTime(

    val message: String = "Time needs to be in the future",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class FutureZonedDateTimeValidator: ConstraintValidator<FutureZonedDateTime, ZonedDateTime> {

    override fun isValid(value: ZonedDateTime?, context: ConstraintValidatorContext?): Boolean {
        return value?.isAfter(ZonedDateTime.now()) ?: false
    }
}