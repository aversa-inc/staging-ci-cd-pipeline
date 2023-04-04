package ca.aversa.insessionservice.context

import ca.aversa.insessionservice.config.AppConfig
import com.google.gson.Gson
import io.mockk.mockk

internal class AuthFilterTest {

    private val appConfig: AppConfig = AppConfig("auth0tenant.com", "", "", "", "")
    private val gson: Gson = mockk()
    private val authFilter = AuthFilter(appConfig, gson)
}