package carcassonne.view.secondary;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import carcassonne.control.ControllerFacade;
import carcassonne.model.Player;
import carcassonne.model.grid.GridDirection;
import carcassonne.model.terrain.TerrainType;
import carcassonne.model.tile.Tile;
import carcassonne.settings.GameSettings;
import carcassonne.util.ImageLoadingUtil;
import carcassonne.view.main.MainView;
import carcassonne.view.util.MouseClickListener;
import carcassonne.view.util.ThreadingUtil;

/**
 * A view for the placement of Meeples on the Tile that was placed previously.
 * @author Timur Saglam
 */
public class MeepleView extends SecondaryView {
    private static final long serialVersionUID = 1449264387665531286L;
    private Map<GridDirection, JButton> meepleButtons;
    private Color defaultButtonColor;
    private Tile tile;

    /**
     * Creates the view.
     * @param controller is the game controller.
     * @param ui is the main view.
     */
    public MeepleView(ControllerFacade controller, MainView ui) {
        super(controller, ui);
        JPanel top = buildButtons();
        JPanel bottom = buildButtonGrid();

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.5;

        constraints.gridx = 0;
        constraints.gridy = 0;

        dialogPanel.add(top, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;

        dialogPanel.add(bottom, constraints);

        dialogPanel.setPreferredSize(new Dimension(256, 256));

        pack();
    }

    /**
     * Sets the tile of the view, updates the view and then makes it visible. Should be called to show the view. The method
     * implements the template method pattern using the method <code>update()</code>.
     * @param tile sets the tile.
     * @param currentPlayer sets the color scheme according to the player.
     */
    public void setTile(Tile tile, Player currentPlayer) {
        if (tile == null) {
            throw new IllegalArgumentException("Tried to set the tile of the " + getClass().getSimpleName() + " to null.");
        }
        this.tile = tile;
        setCurrentPlayer(currentPlayer);
        ThreadingUtil.runAndCallback(this::updatePlacementButtons, this::showUI);
    }

    // build button grid
    private JPanel buildButtonGrid() {
        JPanel bottom = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = 1;
        meepleButtons = new HashMap<>();
        int index = 0;
        for (GridDirection direction : GridDirection.byRow()) {
            JButton button = new PlacementButton(controller, direction);
            button.setToolTipText("Place Meeple on the " + direction.toReadableString() + " of the tile.");
            constraints.gridx = index % 3; // from 0 to 2
            constraints.gridy = index / 3 + 1; // from 1 to 3
            bottom.add(button, constraints);
            meepleButtons.put(direction, button);
            index++;
        }
        return bottom;
    }

    private JPanel buildButtons() {
        JPanel top = new JPanel(new GridBagLayout());

        JButton buttonSkip = new JButton(
                new ImageIcon(ImageLoadingUtil.SKIP.createHighDpiImageIcon().getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH))
        );
        JButton buttonCancel = new JButton(
                new ImageIcon(ImageLoadingUtil.CANCEL.createHighDpiImageIcon().getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH))
        );

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.5;


        buttonSkip.setToolTipText("Don't place meeple and preserve for later use");
        buttonSkip.addMouseListener((MouseClickListener) event -> controller.requestSkip());

        buttonCancel.setToolTipText("Revert tile placement");
        buttonCancel.addMouseListener((MouseClickListener) event -> controller.requestRevert());

        constraints.gridx = 0;
        constraints.gridy = 0;
        top.add(buttonCancel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        top.add(buttonSkip, constraints);

        defaultButtonColor = buttonCancel.getBackground();
        defaultButtonColor = buttonSkip.getBackground();

        return top;
    }

    /**
     * Updates the meepleButtons to reflect the current placement options.
     */
    private void updatePlacementButtons() {
        for (GridDirection direction : GridDirection.values()) {
            TerrainType terrain = tile.getTerrain(direction);
            boolean placeable = tile.hasMeepleSpot(direction) && controller.getSettings().getMeepleRule(terrain);
            JButton button = meepleButtons.get(direction);
            if (placeable) {
                button.setIcon(ImageLoadingUtil.createHighDpiImageIcon(GameSettings.getMeeplePath(terrain, false)));
            } else {
                button.setIcon(ImageLoadingUtil.createHighDpiImageIcon(GameSettings.getMeeplePath(TerrainType.OTHER, false)));
            }
            if (placeable && tile.allowsPlacingMeeple(direction, currentPlayer, controller.getSettings())) {
                button.setEnabled(true);
                button.setBackground(defaultButtonColor);
            } else {
                button.setEnabled(false);
                button.setBackground(currentPlayer.getColor().lightColor());
            }
        }
    }
}