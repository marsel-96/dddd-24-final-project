package carcassonne.model.telemetry;

import java.time.Duration;
import java.time.Instant;

public record TelemetryData(
        String userId,
        String gameVersion,
        String sessionId,
        String roundId,
        int clicks,
        int misclicks,
        String telemetry,
        Duration roundTime,
        Instant timestamp,
        int rotationClicks,
        int skipClicks,
        int cancelClicks
) { }