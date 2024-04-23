package carcassonne.control.telemetry;

import carcassonne.control.telemetry.connector.TelemetryConnector;
import carcassonne.control.telemetry.connector.impl.GoogleFormsTelemetryConnector;
import carcassonne.model.telemetry.TelemetryData;

import java.time.Duration;
import java.time.Instant;

public class TelemetryManager {

    private static TelemetryManager instance;
    private final TelemetryConnector connector;

    public static TelemetryManager getInstance() {
        if (instance == null) {
            instance = new TelemetryManager();
        }
        return instance;
    }

    private TelemetryManager() {
        connector = new GoogleFormsTelemetryConnector();
    }

    public void sendTelemetryData(TelemetryData data) {
        connector.sendTelemetryData(data);
    }

    public static void main(String[] args) throws Exception {
        TelemetryManager.getInstance().sendTelemetryData(
                new TelemetryData(
                        "test",
                        "test",
                        "test",
                        "test",
                        0,
                        0,
                        "test",
                        Duration.of(25, java.time.temporal.ChronoUnit.SECONDS),
                        Instant.now(),
                        0,
                        0,
                        0
                )
        );
    }

}
