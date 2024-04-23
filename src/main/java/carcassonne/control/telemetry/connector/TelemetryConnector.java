package carcassonne.control.telemetry.connector;

import carcassonne.model.telemetry.TelemetryData;

public interface TelemetryConnector {

    void sendTelemetryData(TelemetryData data);

    void close();

}
