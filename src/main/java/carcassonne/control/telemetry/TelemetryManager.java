package carcassonne.control.telemetry;

import carcassonne.control.telemetry.connector.TelemetryConnector;
import carcassonne.control.telemetry.connector.impl.GoogleFormsTelemetryConnector;
import carcassonne.model.telemetry.AdvancedTelemetryData;
import carcassonne.model.telemetry.TelemetryData;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class TelemetryManager {

    private static final String GOOGLE_MAIN_FORM_URL = "https://docs.google.com/forms/d/e/1FAIpQLSemiSai7-r-kTReoaNpQPptTFVfiNox-WZcSb6KmIHgE79oFw/viewform?usp=pp_url&entry.243585258=%s&hl=en";

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
    private int cancelClicks;
    private int tilePlacementClicks;
    private Instant startRound;
    private final Timer mousePositionCapture;
    private final java.util.List<int[]> mousePositions;

    private final boolean enabled = true;

    public void addCancelClick() {
        this.cancelClicks++;
    }
    public void addSkipClick() {
        this.skipClicks++;
    }
    public void addRotationClick() {
        this.rotationClicks++;
    }
    public void addTilePlacementClick() {
        this.tilePlacementClicks++;
    }
    public void addMisClick() {
        this.misclicks++;
    }
    public void newSession() {
        var now = Instant.now();

        clearRoundData(now);
        startMousePositionCapture();

        this.sessionId = UUID.randomUUID().toString();
        this.roundId = 1;
    }
    public void abortGame() {
        finishRound();
        this.sessionId = "NULL";
        this.roundId = 0;
    }

    public void setAdvancedHighlightEnabled(boolean status) {
        this.advancedHighlightEnabled = status;
    }

    public String getUserId() {
        return userId;
    }

    public void startMousePositionCapture() {
        mousePositions.clear();
        mousePositionCapture.start();
    }

    public static TelemetryManager getInstance() {
        if (instance == null) {
            instance = new TelemetryManager();
        }

        return instance;
    }

    private void clearRoundData(Instant startRound) {
        this.clicks = 0;
        this.misclicks = 0;
        this.rotationClicks = 0;
        this.skipClicks = 0;
        this.cancelClicks = 0;
        this.tilePlacementClicks = 0;
        this.startRound = startRound;
    }

    public void openQuestionnaire() {
        try {
            Runtime.getRuntime().exec(
                    String.format("cmd.exe /c start chrome --incognito \"%s\"", String.format(GOOGLE_MAIN_FORM_URL, userId))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private TelemetryManager() {
        connector = new GoogleFormsTelemetryConnector();
        userId = UUID.randomUUID().toString();
        mousePositions = new java.util.ArrayList<>();
        mousePositionCapture = new Timer(500, e -> {
            Point p = MouseInfo.getPointerInfo().getLocation();
            mousePositions.add(new int [] {p.x, p.y});
        });

        if (enabled) {
            Timer timer = new Timer(3000, ignore -> openQuestionnaire());
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void finishRound() {
        var now = Instant.now();
        var roundTime = Duration.between(startRound, now);

        if (enabled) {
            connector.sendTelemetryData(
                    new TelemetryData(
                            userId,
                            advancedHighlightEnabled,
                            sessionId,
                            roundId,
                            clicks,
                            misclicks,
                            new AdvancedTelemetryData(500, mousePositions),
                            roundTime.truncatedTo(ChronoUnit.MILLIS),
                            now.truncatedTo(ChronoUnit.MILLIS),
                            rotationClicks,
                            skipClicks,
                            cancelClicks,
                            tilePlacementClicks
                    )
            );
        }

        clearRoundData(now);

        this.roundId++;
        startMousePositionCapture();
    }

    public boolean getAdvancedHighlightEnabledDefault() {
        return new File("highlight").isFile();
    }
}
