package carcassonne.control.telemetry.connector.impl;

import carcassonne.control.telemetry.connector.TelemetryConnector;
import carcassonne.model.telemetry.TelemetryData;
import org.jetbrains.annotations.NotNull;
import okhttp3.*;

import java.io.IOException;

public class GoogleFormsTelemetryConnector implements TelemetryConnector {

    public static final String TELEMETRY_URL = "https://docs.google.com/forms/d/e/1FAIpQLSdhQ9IVaa-Mc6Y6VmTOjEv9uH-_hbsQ9EgpwKH2riUrQemnTw/formResponse";

    public static final String GFORM_USER_ID = "entry.1550165433";
    public static final String GFORM_SESSION_ID = "entry.2146974668";
    public static final String GFORM_GAME_VERSION = "entry.749389799";
    public static final String GFORM_ROUND_ID = "entry.154803844";
    public static final String GFORM_CLICKS = "entry.1714315852";
    public static final String GFORM_MISCLICKS = "entry.88247415";
    public static final String GFORM_TELEMETRY = "entry.47842034";
    public static final String GFORM_ROUND_TIME = "entry.1900182304";
    public static final String GFORM_TIMESTAMP = "entry.2115509525";
    public static final String GFORM_ROTATION_CLICKS = "entry.1321843017";
    public static final String GFORM_SKIP_CLICKS = "entry.1498582672";
    public static final String GFORM_CANCEL_CLICKS = "entry.429528645";

    private final OkHttpClient client;
    private final LogFileTelemetryConnector backupConnector;

    public GoogleFormsTelemetryConnector() {
        client = new OkHttpClient();
        backupConnector = new LogFileTelemetryConnector();
    }

    private Request buildRequest(TelemetryData data) {
        return new Request
                .Builder()
                .url(TELEMETRY_URL)
                .post(
                        new FormBody.Builder()
                                .add(GFORM_USER_ID, data.userId())
                                .add(GFORM_SESSION_ID, data.userId())
                                .add(GFORM_GAME_VERSION, String.valueOf(data.advancedTileHighlight()))
                                .add(GFORM_ROUND_ID, String.valueOf(data.roundId()))
                                .add(GFORM_CLICKS, String.valueOf(data.clicks()))
                                .add(GFORM_MISCLICKS, String.valueOf(data.misclicks()))
                                .add(GFORM_TELEMETRY, data.telemetry())
                                .add(GFORM_ROUND_TIME, data.roundTime().toString())
                                .add(GFORM_TIMESTAMP, data.timestamp().toString())
                                .add(GFORM_ROTATION_CLICKS, String.valueOf(data.rotationClicks()))
                                .add(GFORM_SKIP_CLICKS, String.valueOf(data.skipClicks()))
                                .add(GFORM_CANCEL_CLICKS, String.valueOf(data.cancelClicks()))
                                .build()
                )
                .build();
    }

    @Override
    public void sendTelemetryData(TelemetryData data) {
        var request = buildRequest(data);

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                backupConnector.sendTelemetryData(data);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody ignored = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response: " + response);
                    }
                }
            }

        });
    }

    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }

}
