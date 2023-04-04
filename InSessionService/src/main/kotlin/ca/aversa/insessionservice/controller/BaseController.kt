package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.context.Context
import ca.aversa.insessionservice.context.RequestAttributeType
import ca.aversa.insessionservice.exception.DuplicateResourceException
import ca.aversa.insessionservice.exception.ResourceNotFoundException
import ca.aversa.insessionservice.exception.SessionTimeConflictException
import ca.aversa.insessionservice.model.response.ExceptionResponse
import ca.aversa.insessionservice.util.ResponseConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import javax.servlet.http.HttpServletRequest

abstract class BaseController {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun extractContextFromRequest(request: HttpServletRequest): Context {
        return request.getAttribute(RequestAttributeType.CONTEXT.toString()) as Context
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [(BindException::class)])
    fun handleValidationExceptionForPostRequests(exception: BindException): Map<String, String> {
        val fieldErrors = HashMap<String, String>()

        exception.bindingResult.allErrors.forEach { error ->
            val fieldError = error as? FieldError

            fieldError?.also { field ->
                val message = error.defaultMessage

                fieldErrors[field.field] = message ?: ""
            }
        }

        return fieldErrors
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [(HttpMessageNotReadableException::class)])
    fun handleMissingFieldsExceptionForPostRequests(exception: HttpMessageNotReadableException): ExceptionResponse {
        return ExceptionResponse(
            "There are missing required properties in this request"
        )
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = [(ResourceNotFoundException::class)])
    fun handleResourceNotFoundException(exception: ResourceNotFoundException): ExceptionResponse {
        exception.printStackTrace();

        return ExceptionResponse(ResponseConstants.RESOURCE_NOT_FOUND_MESSAGE)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = [(DuplicateResourceException::class)])
    fun handleDuplicateResourceException(exception: DuplicateResourceException): ExceptionResponse {
        exception.printStackTrace();

        return ExceptionResponse(ResponseConstants.DUPLICATE_RESOURCE_EXCEPTION_MESSAGE)
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = [(SessionTimeConflictException::class)])
    fun handleSessionTimeConflictException(exception: SessionTimeConflictException): ExceptionResponse {
        exception.printStackTrace();

        return ExceptionResponse(ResponseConstants.SESSION_TIME_CONFLICT_EXCEPTION)
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [(Exception::class)])
    fun handleException(exception: Exception): ExceptionResponse {
        log.error("General exception occurred while processing request: ", exception)

        return ExceptionResponse(ResponseConstants.GENERAL_EXCEPTION_MESSAGE)
    }
}