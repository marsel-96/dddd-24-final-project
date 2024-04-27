package carcassonne.model.telemetry;


import java.awt.Point;
import java.util.List;

public record AdvancedTelemetryData(
        int mouseSampleRate,
        List<int[]> mousePositions
) {
}
