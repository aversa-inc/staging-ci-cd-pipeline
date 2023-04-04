package ca.aversa.insessionservice.bo

import io.mockk.mockk

internal class AuthenticationBoTest {

    private val clientBo: ClientBo = mockk()
    private val clinicianBo: ClinicianBo = mockk()
    private val authenticationBo: AuthenticationBo = AuthenticationBo()

}