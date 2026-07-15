package io.github.tmkontra.scenarios.demo.configuration;

import io.github.tmkontra.scenarios.MalformedScenarioOverrideHeaderException;
import io.github.tmkontra.scenarios.ScenarioOverrideDisabledException;
import io.github.tmkontra.scenarios.ScenarioOverrideForbiddenException;
import io.github.tmkontra.scenarios.UnknownScenarioOverrideException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ScenarioOverrideExceptionHandler {
    @ExceptionHandler(MalformedScenarioOverrideHeaderException.class)
    ProblemDetail malformedScenarioOverrideHeader(MalformedScenarioOverrideHeaderException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setProperty("code", "MALFORMED_SCENARIO_OVERRIDE_HEADER");
        return problem;
    }

    @ExceptionHandler(UnknownScenarioOverrideException.class)
    ProblemDetail unknownScenarioOverride(UnknownScenarioOverrideException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setProperty("code", "UNKNOWN_SCENARIO_OVERRIDE");
        problem.setProperty("endpoint", exception.getEndpoint());
        problem.setProperty("requestedScenarioId", exception.getRequestedScenarioId());
        problem.setProperty("availableScenarioIds", exception.getAvailableScenarioIds());
        return problem;
    }

    @ExceptionHandler(ScenarioOverrideDisabledException.class)
    ProblemDetail disabledScenarioOverride(ScenarioOverrideDisabledException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setProperty("code", "SCENARIO_OVERRIDE_DISABLED");
        return problem;
    }

    @ExceptionHandler(ScenarioOverrideForbiddenException.class)
    ProblemDetail forbiddenScenarioOverride(ScenarioOverrideForbiddenException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setProperty("code", "SCENARIO_OVERRIDE_FORBIDDEN");
        return problem;
    }
}
