package io.github.tmkontra.scenarios.demo.controllers;

import io.github.tmkontra.scenarios.Scenario;
import io.github.tmkontra.scenarios.ScenariosFor;
import org.springframework.http.ResponseEntity;

@ScenariosFor(
        controller = DemoController.class,
        method = "getMyReport",
        name = "my-report"
)
public class DemoGetMyReportScenarios {
    @Scenario("report-in-progress")
    public ResponseEntity<String> reportInProgress() {
        return ResponseEntity.ok("<pending progress='50%'>");
    }

    @Scenario("report-100")
    public ResponseEntity<String> reportValueEq100() {
        return ResponseEntity.ok("<report value='100'>");
    }
}
