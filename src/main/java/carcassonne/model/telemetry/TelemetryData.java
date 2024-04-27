package carcassonne.model.telemetry;

import java.time.Duration;
import java.time.Instant;

public record TelemetryData(
        String userId,
        boolean advancedTileHighlight,
        String sessionId,
        int roundId,
        int clicks,
        int misclicks,
        AdvancedTelemetryData telemetry,
        Duration roundTime,
        Instant timestamp,
        int rotationClicks,
        int skipClicks,
        int cancelClicks,
        int tilePlacementClicks) { }