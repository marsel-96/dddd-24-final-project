package carcassonne.control.telemetry;

import carcassonne.control.telemetry.connector.TelemetryConnector;
import carcassonne.control.telemetry.connector.impl.GoogleFormsTelemetryConnector;
import carcassonne.model.telemetry.TelemetryData;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TelemetryManager {

    private static final String GOOGLE_MAIN_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSemiSai7-r-kTReoaNpQPptTFVfiNox-WZcSb6KmIHgE79oFw/viewform?usp=pp_url&entry.243585258=%s";

    private static TelemetryManager instance;
    private final TelemetryConnector connector;

    private final String userId;
    private String sessionId;

    private int roundId;
    private boolean advancedHighlightEnabled;

    private int clicks;
    private int misclicks;
    private int rotationClicks;
    private int skipClicks;

    public void addCancelClicks() {
        this.cancelClicks++;
    }
    public void addSkipClicks() {
        this.skipClicks++;
    }
    public void addRotationClicks() {
        this.rotationClicks++;
    }
    public void addMisclicks() {
        this.misclicks++;
    }
    public void addClicks() {
        this.clicks++;
    }
    public void nextRound() {
        this.roundId++;
        this.clicks = 0;
        this.misclicks = 0;
        this.rotationClicks = 0;
        this.skipClicks = 0;
        this.cancelClicks = 0;
    }
    public void newSession() {
        nextRound();

        this.sessionId = UUID.randomUUID().toString();
        this.roundId = 1;
    }
    public void setAdvancedHighlightEnabled(boolean status) {
        this.advancedHighlightEnabled = status;
    }

    private int cancelClicks;

    private Instant startRound;

    public static TelemetryManager getInstance() {
        if (instance == null) {
            instance = new TelemetryManager();
        }
        return instance;
    }

    private TelemetryManager() {
        connector = new GoogleFormsTelemetryConnector();
        userId = UUID.randomUUID().toString();

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                var url = String.format(GOOGLE_MAIN_FORM_URL, userId);
                Desktop.getDesktop()
                        .browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendTelemetryData() {
        var now = Instant.now();
        var roundTime = Duration.between(startRound, now);

        connector.sendTelemetryData(
                new TelemetryData(
                        userId,
                        advancedHighlightEnabled,
                        sessionId,
                        roundId,
                        clicks,
                        misclicks,
                        "[]",
                        roundTime.truncatedTo(ChronoUnit.MILLIS),
                        now.truncatedTo(ChronoUnit.MILLIS),
                        rotationClicks,
                        skipClicks,
                        cancelClicks
                )
        );

        startRound = now;
    }

}
