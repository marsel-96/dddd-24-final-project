package carcassonne.control.state;

import java.util.Collection;
import java.util.Optional;

import carcassonne.control.telemetry.TelemetryManager;
import carcassonne.model.Player;
import carcassonne.model.ai.AbstractCarcassonneMove;
import carcassonne.model.ai.ArtificialIntelligence;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.grid.GridSpot;
import carcassonne.model.tile.Tile;
import carcassonne.settings.GameSettings;
import carcassonne.view.ViewFacade;
import carcassonne.view.main.MainView;
import carcassonne.view.util.GameMessage;

/**
 * The specific state when a Tile can be placed.
 * @author Timur Saglam
 */
public class StatePlacing extends AbstractGameState {

    private static final int SLEEP_DURATION = 10;

    /**
     * Constructor of the state.
     * @param stateMachine is the state machine managing this state.
     * @param settings are the game settings.
     * @param views contains the user interfaces.
     * @param playerAI is the current AI strategy.
     */
    public StatePlacing(StateMachine stateMachine, GameSettings settings, ViewFacade views, ArtificialIntelligence playerAI) {
        super(stateMachine, settings, views, playerAI);
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#abortGame()
     */
    @Override
    public void abortGame() {
        changeState(StateGameOver.class);
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#newRound()
     */
    @Override
    public void newRound(int playerCount) {
        GameMessage.showWarning("Abort the current game before starting a new one.");

    }

    /**
     * @see carcassonne.control.state.AbstractGameState#placeMeeple()
     */
    @Override
    public void placeMeeple(GridDirection position) {
        throw new IllegalStateException("Placing meeples in StatePlacing is not allowed.");
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#placeTile()
     */
    @Override
    public void placeTile(int x, int y) {
        if (!round.getActivePlayer().isComputerControlled()) {
            Tile tile = views.getSelectedTile();
            placeTile(tile, x, y, false);
        }
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#skip()
     */
    @Override
    public void skip() {
        if (!round.getActivePlayer().isComputerControlled()) {
            TelemetryManager.getInstance().addSkipClick();
            skipPlacingTile();
        }
    }

    private void skipPlacingTile() {
        getTileToDrop().ifPresent(it -> {
            tileStack.putBack(it);
            if (!round.getActivePlayer().dropTile(it)) {
                throw new IllegalStateException("Cannot drop tile " + it + "from player " + round.getActivePlayer());
            }
        });
        if (!round.getActivePlayer().isComputerControlled()) {
            views.onMainView(MainView::resetPlacementHighlights);
        }
        TelemetryManager.getInstance().finishRound();
        round.nextTurn();
        views.onMainView(it -> it.setCurrentPlayer(round.getActivePlayer()));
        entry();
    }

    private void placeTile(Tile tile, int x, int y, boolean highlightPlacement) {
        var isValidPlacement = grid.place(x, y, tile);
        if (isValidPlacement) {
            round.getActivePlayer().dropTile(tile);
            views.onMainView(it -> it.setTile(tile, x, y));
            if (highlightPlacement) {
                views.onMainView(view -> view.setPlacementHighlight(x, y));
            }
            changeState(StateManning.class);
        } else {
            TelemetryManager.getInstance().addMisClick();
        }
    }

    private void placeTileWithAI(Player player) {
        Optional<AbstractCarcassonneMove> bestMove = playerAI.calculateBestMoveFor(player.getHandOfTiles(), player, grid, tileStack);
        if (bestMove.isEmpty()) {
            skipPlacingTile();
        }
        bestMove.ifPresent(it -> {
            Tile tile = it.getOriginalTile();
            tile.rotateTo(it.getRequiredTileRotation());
            placeTile(tile, it.getX(), it.getY(), round.hasHumanPlayers());
        });
    }

    private Optional<Tile> getTileToDrop() {
        if (round.getActivePlayer().isComputerControlled()) {
            Collection<Tile> handOfTiles = round.getActivePlayer().getHandOfTiles();
            if (handOfTiles.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(playerAI.chooseTileToDrop(handOfTiles));
        }
        return Optional.of(views.getSelectedTile());
    }

    private void waitForUI() {
        while (views.isBusy()) {
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
                GameMessage.showError(exception.getCause().getMessage());
            }
        }
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#entry()
     */
    @Override
    protected void entry() {
        Player player = round.getActivePlayer();

        if (player.getRevert()) {

            Tile tile = getSelectedTile();
            GridSpot spot = tile.getGridSpot();
            spot.removeTile();

            if(!grid.remove(spot.getX(), spot.getY())) {
                throw new IllegalStateException("Tile could not be removed from grid.");
            }

            player.setRevert(false);
            player.addTile(tile);
            highlightSurroundings(tile);

            views.onTileView(it -> it.setTiles(player));

            return;
        }

        if (!player.hasFullHand() && !tileStack.isEmpty()) {
            Tile tile = tileStack.drawTile();
            player.addTile(tile);
            highlightSurroundings(tile);
        }
        updateStackSize();
        if (round.isOver()) {
            changeState(StateGameOver.class);
        } else if (player.isComputerControlled()) {
            waitForUI();
            placeTileWithAI(player);
        } else {
            views.onTileView(it -> it.setTiles(player));
        }
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#exit()
     */
    @Override
    protected void exit() {
        views.onTileView(it -> it.setVisible(false));
    }

    @Override
    public void revert() {
        throw new IllegalStateException("Reverting in StatePlacing is not allowed.");
    }

}
