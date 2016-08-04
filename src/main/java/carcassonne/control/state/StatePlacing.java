package carcassonne.control.state;

import carcassonne.control.MainController;
import carcassonne.model.tile.Tile;
import carcassonne.view.MainGUI;
import carcassonne.view.PlacementGUI;
import carcassonne.view.RotationGUI;

/**
 * The specific state when a Tile can be placed.
 * @author Timur Saglam
 */
public class StatePlacing extends ControllerState {

    /**
     * Constructor of the state.
     * @param controller sets the controller.
     * @param mainGUI sets the main GUI.
     * @param rotationGUI sets the rotation GUI.
     * @param placementGUI sets the placement GUI.
     */
    public StatePlacing(MainController controller, MainGUI mainGUI, RotationGUI rotationGUI, PlacementGUI placementGUI) {
        super(controller, mainGUI, rotationGUI, placementGUI);
    }

    /**
     * @see carcassonne.control.state.ControllerState#entry()
     */
    @Override
    protected void entry() {
        rotationGUI.setTile(round.getCurrentTile());
    }

    /**
     * @see carcassonne.control.state.ControllerState#exit()
     */
    @Override
    protected void exit() {
        rotationGUI.disableFrame();
    }

    @Override
    public boolean placeTile(int x, int y) {
        Tile tile = rotationGUI.useTile();
        if (grid.place(x, y, tile)) {
            round.updateCurrentTile(tile);
            mainGUI.set(tile, x, y);
            rotationGUI.disableFrame();
            changeState(StateManning.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean skip() {
        round.nextTurn();
        entry();
        return true;
    }

    /**
     * @see carcassonne.control.state.ControllerState#abortGame()
     */
    @Override
    public boolean abortGame() {
        changeState(StateGameOver.class);
        return true;
    }

}