package ca.aversa.insessionservice.controller

import ca.aversa.insessionservice.model.request.PasswordlessRequest
import ca.aversa.insessionservice.model.response.PasswordlessResponse
import ca.aversa.insessionservice.service.EmailService
import ca.aversa.insessionservice.service.SmsNotificationService
import ca.aversa.insessionservice.util.Constants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthenticationController(

   private val smsNotificationService: SmsNotificationService,
   private val emailService: EmailService
): BaseController() {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/sms-gateway")
    fun smsGateway(@Valid @RequestBody request: PasswordlessRequest): ResponseEntity<PasswordlessResponse> {
        log.info("Passwordless body from Auth0: {}", request)

        when(request.phoneNumber) {
            Constants.TEST_APPSTORE_CLINICIAN_PHONE -> {
                log.info("Sending code to test Appstore clinician email")

                emailService.sendEmail(Constants.TEST_APPSTORE_CLINICIAN_EMAIL, request.body, request.body)
            }
            Constants.TEST_APPSTORE_CLIENT_PHONE -> {
                log.info("Sending code to test Appstore client email")

                emailService.sendEmail(Constants.TEST_APPSTORE_CLIENT_EMAIL, request.body, request.body)
            }
            else -> {
                smsNotificationService.sendSms(request.phoneNumber, request.body)
            }
        }

        return ResponseEntity.ok().body(
            PasswordlessResponse("SMS Sent")
        )
    }
}