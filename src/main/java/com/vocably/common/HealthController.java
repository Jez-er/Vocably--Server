package com.vocably.common;

import java.util.Map;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Health", description = "Health check endpoint")
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    public HealthController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @Operation(
            summary = "Health check",
            description = "Reports whether the API and its dependencies (database, Redis) are usable. "
                    + "Answers 503 when any of them is not. Component detail is deliberately omitted — "
                    + "use /actuator/health for that."
    )
    @ApiResponse(responseCode = "200", description = "API and dependencies are healthy")
    @ApiResponse(responseCode = "503", description = "A dependency is unavailable")
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        Status status = statusOf();
        boolean up = Status.UP.equals(status);

        return ResponseEntity
                .status(up ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)

                .body(Map.of("status", up ? "ok" : status.getCode().toLowerCase()));
    }

    private Status statusOf() {
        HealthComponent health = healthEndpoint.health();

        return health != null ? health.getStatus() : Status.UNKNOWN;
    }
}
