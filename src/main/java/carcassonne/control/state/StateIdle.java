package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.ai.ArtificialIntelligence;
import carcassonne.model.grid.GridDirection;
import carcassonne.view.ViewContainer;
import carcassonne.view.main.MainGUI;
import carcassonne.view.util.GameMessage;

/**
 * The specific state if no game is running.
 * @author Timur Saglam
 */
public class StateIdle extends AbstractGameState {

    /**
     * Constructor of the state.
     * @param controller sets the ControllerFacade
     * @param mainGUI sets the MainGUI
     * @param previewGUI sets the PreviewGUI
     * @param placementGUI sets the PlacementGUI
     */
    public StateIdle(MainController controller, ViewContainer views, ArtificialIntelligence playerAI) {
        super(controller, views, playerAI);
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#abortGame()
     */
    @Override
    public void abortGame() {
        GameMessage.showMessage("There is currently no game running.");
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#newRound()
     */
    @Override
    public void newRound(int playerCount) {
        startNewRound(playerCount);
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#placeMeeple()
     */
    @Override
    public void placeMeeple(GridDirection position) {
        throw new IllegalStateException("Placing meeples in StateIdle is not allowed.");
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#placeTile()
     */
    @Override
    public void placeTile(int x, int y) {
        // do nothing.
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#skip()
     */
    @Override
    public void skip() {
        throw new IllegalStateException("There is nothing to skip in StateIdle.");
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#entry()
     */
    @Override
    protected void entry() {
        views.onMainView(MainGUI::resetGrid);
    }

    /**
     * @see carcassonne.control.state.AbstractGameState#exit()
     */
    @Override
    protected void exit() {
        // No exit functions.
    }

}
