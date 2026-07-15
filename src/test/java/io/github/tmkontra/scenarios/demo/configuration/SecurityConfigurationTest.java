package io.github.tmkontra.scenarios.demo.configuration;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void leavesRequestAnonymousWithoutStaticAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("anonymous")));
    }

    @Test
    void authenticatesAdminUserWithStaticAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/user")
                        .header("Authorization", SecurityConfiguration.ADMIN_AUTHORIZATION_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("superuser")));
    }

    @Test
    void appliesScopedScenarioOverrideForMatchingEndpointAlias() throws Exception {
        mockMvc.perform(get("/api/my-report")
                        .header("Authorization", SecurityConfiguration.ADMIN_AUTHORIZATION_HEADER)
                        .header("X-Scenario-Override", "my-report=report-in-progress"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                        .isEqualTo("<pending progress='50%'>"));
    }

    @Test
    void ignoresScenarioOverrideForOtherEndpointAliases() throws Exception {
        mockMvc.perform(get("/api/my-report")
                        .header("Authorization", SecurityConfiguration.ADMIN_AUTHORIZATION_HEADER)
                        .header("X-Scenario-Override", "user=report-in-progress"))
                .andExpect(status().isOk())
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                        .isEqualTo("<report value='1'>"));
    }

    @Test
    void returnsBadRequestForUnknownScopedScenarioOverride() throws Exception {
        mockMvc.perform(get("/api/my-report")
                        .header("Authorization", SecurityConfiguration.ADMIN_AUTHORIZATION_HEADER)
                        .header("X-Scenario-Override", "my-report=missing-scenario"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("UNKNOWN_SCENARIO_OVERRIDE")))
                .andExpect(jsonPath("$.endpoint", is("my-report")))
                .andExpect(jsonPath("$.requestedScenarioId", is("missing-scenario")));
    }

    @Test
    void returnsBadRequestForMalformedScenarioOverrideHeader() throws Exception {
        mockMvc.perform(get("/api/my-report")
                        .header("X-Scenario-Override", "my-report"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MALFORMED_SCENARIO_OVERRIDE_HEADER")));
    }

    @Test
    void deniesScenarioOverrideForNonAdminUsers() throws Exception {
        mockMvc.perform(get("/api/my-report")
                        .header("Authorization", SecurityConfiguration.CUSTOMER_AUTHORIZATION_HEADER)
                        .header("X-Scenario-Override", "my-report=report-in-progress"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("SCENARIO_OVERRIDE_FORBIDDEN")));
    }
}
