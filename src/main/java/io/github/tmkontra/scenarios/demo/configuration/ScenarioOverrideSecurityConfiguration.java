package io.github.tmkontra.scenarios.demo.configuration;

import io.github.tmkontra.scenarios.ScenarioOverride;
import io.github.tmkontra.scenarios.ScenarioOverrideAuthorizer;
import io.github.tmkontra.scenarios.ScenarioOverrideForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.util.Optional;

@Configuration
public class ScenarioOverrideSecurityConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ScenarioOverrideSecurityConfiguration.class);

    @Bean
    @Primary
    public ScenarioOverrideAuthorizer novaviewScenarioOverrideAuthorizer() {
        return new StaffAdminScenarioOverrideAuthorizer();
    }

    static class StaffAdminScenarioOverrideAuthorizer implements ScenarioOverrideAuthorizer {
        @Override
        public void authorize(HttpServletRequest request, HandlerMethod endpoint, ScenarioOverride override) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthorized = Optional.ofNullable(authentication).filter(Authentication::isAuthenticated).filter(a -> a.getAuthorities().stream().anyMatch(a1 -> a1.getAuthority().equals("ROLE_ADMIN"))).isPresent();
            if (!isAuthorized) {
                logger.warn(
                        "Scenario override authorization denied endpoint={} scenarioId={} principal={}",
                        endpoint.getShortLogMessage(),
                        override.scenarioId(),
                        authentication == null ? null : authentication.getName()
                );
                throw new ScenarioOverrideForbiddenException();
            }
            logger.info(
                    "Scenario override authorization granted endpoint={} scenarioId={} principal={}",
                    endpoint.getShortLogMessage(),
                    override.scenarioId(),
                    authentication.getName()
            );
        }
    }
}
