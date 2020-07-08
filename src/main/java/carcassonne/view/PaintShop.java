package carcassonne.view;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import carcassonne.model.Player;
import carcassonne.model.terrain.TerrainType;
import carcassonne.settings.GameSettings;
import carcassonne.util.ImageLoadingUtil;

/**
 * This is the Carcassonne paint shop! It paints meeple images and tile highlights! It is implemented as a utility class
 * with static methods to increase performance through avoiding loading images more often that needed.
 * @author Timur Saglam
 */
public final class PaintShop {
    private static final int HIGH_DPI_FACTOR = 2;
    private static final BufferedImage emblemImage = ImageLoadingUtil.EMBLEM.createBufferedImage();
    private static final BufferedImage highlightBaseImage = ImageLoadingUtil.NULL_TILE.createBufferedImage();
    private static final BufferedImage highlightImage = ImageLoadingUtil.HIGHLIGHT.createBufferedImage();
    private static final Map<String, ImageIcon> chachedMeepleImages = new HashMap<>();
    private static final Map<TerrainType, BufferedImage> templateMap = buildImageMap(true);
    private static final Map<TerrainType, BufferedImage> imageMap = buildImageMap(false);
    private static final String KEY_SEPARATOR = "|";
    private static final int MAXIMAL_ALPHA = 255;
    private static final int SCALING_STRATEGY = Image.SCALE_SMOOTH;

    private PaintShop() {
        // private constructor ensures non-instantiability!
    }

    /**
     * Adds the emblem image to the top right of any tile image.
     * @param originalTile is the original tile image without the emblem.
     * @return a copy of the image with an emblem.
     */
    public static Image addEmblem(BufferedImage originalTile) {
        BufferedImage copy = deepCopy(originalTile);
        for (int x = 0; x < emblemImage.getWidth(); x++) {
            for (int y = 0; y < emblemImage.getHeight(); y++) {
                Color emblemPixel = new Color(emblemImage.getRGB(x, y), true);
                Color imagePixel = new Color(copy.getRGB(x, y), true);
                Color blendedColor = blend(imagePixel, emblemPixel, false);
                copy.setRGB(x, y, blendedColor.getRGB());
            }
        }
        return copy;
    }

    /**
     * Clears the meeple image cache. Should be cleared when player colors change.
     */
    public static void clearCachedImages() {
        chachedMeepleImages.clear();
    }

    /**
     * Returns a custom colored highlight image.
     * @param player determines the color of the highlight.
     * @param size is the edge length in pixels of the image.
     * @return the highlighted tile.
     */
    public static ImageIcon getColoredHighlight(Player player, int size) {
        ImageIcon coloredImage = colorMaskBased(highlightBaseImage, highlightImage, player.getColor());

        Image small = coloredImage.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        int largeSize = Math.min(size * HIGH_DPI_FACTOR, GameSettings.TILE_RESOLUTION);
        Image large = coloredImage.getImage().getScaledInstance(largeSize, largeSize, Image.SCALE_SMOOTH);

        return new ImageIcon(new BaseMultiResolutionImage(small, large));
    }

    /**
     * Returns a custom colored meeple.
     * @param meepleType is the type of the meeple.
     * @param color is the custom color.
     * @param size is the edge length in pixels of the image.
     * @return the colored meeple.
     */
    public static ImageIcon getColoredMeeple(TerrainType meepleType, Color color, int size) {
        String key = createKey(color, meepleType, size);
        if (chachedMeepleImages.containsKey(key)) {
            return chachedMeepleImages.get(key);
        }
        Image paintedMeeple = paintMeeple(meepleType, color.getRGB(), size * HIGH_DPI_FACTOR);
        ImageIcon icon = new ImageIcon(ImageLoadingUtil.createHighDpiImage(paintedMeeple));
        chachedMeepleImages.put(key, icon);
        return icon;
    }

    /**
     * Returns a custom colored meeple.
     * @param meepleType is the type of the meeple.
     * @param player is the {@link Player} whose color is used.
     * @param size is the edge length in pixels of the image.
     * @return the colored meeple.
     */
    public static ImageIcon getColoredMeeple(TerrainType meepleType, Player player, int size) {
        return getColoredMeeple(meepleType, player.getColor(), size);
    }

    /**
     * Creates a non-colored meeple image icon.
     * @param meepleType is the type of the meeple.
     * @param size is the edge length in pixels of the image.
     * @return non-colored meeple image.
     */
    public static ImageIcon getPreviewMeeple(TerrainType meepleType, int size) {
        String key = createKey(meepleType, size);
        if (chachedMeepleImages.containsKey(key)) {
            return chachedMeepleImages.get(key);
        }
        Image preview = imageMap.get(meepleType).getScaledInstance(size * HIGH_DPI_FACTOR, size * HIGH_DPI_FACTOR, SCALING_STRATEGY);
        ImageIcon icon = new ImageIcon(ImageLoadingUtil.createHighDpiImage(preview));
        chachedMeepleImages.put(key, icon);
        return icon;
    }

    /**
     * Blends to colors correctly based on alpha composition. Either blends both colors or applies the second on the first
     * one.
     * @param first is the first color to be applied.
     * @param second is the second color to be applied.
     * @param blendEqually applies the second on the first one of true, blends on alpha values if false.
     * @return the blended color.
     */
    private static Color blend(Color first, Color second, boolean blendEqually) {
        double totalAlpha = blendEqually ? first.getAlpha() + second.getAlpha() : MAXIMAL_ALPHA;
        double firstWeight = blendEqually ? first.getAlpha() : MAXIMAL_ALPHA - second.getAlpha();
        firstWeight /= totalAlpha;
        double secondWeight = second.getAlpha() / totalAlpha;
        double red = firstWeight * first.getRed() + secondWeight * second.getRed();
        double green = firstWeight * first.getGreen() + secondWeight * second.getGreen();
        double blue = firstWeight * first.getBlue() + secondWeight * second.getBlue();
        int alpha = Math.max(first.getAlpha(), second.getAlpha());
        return new Color((int) red, (int) green, (int) blue, alpha);
    }

    // prepares the base images and templates
    private static Map<TerrainType, BufferedImage> buildImageMap(boolean isTemplate) {
        Map<TerrainType, BufferedImage> map = new HashMap<>();
        for (TerrainType terrainType : TerrainType.values()) {
            BufferedImage meepleImage = ImageLoadingUtil.createBufferedImage(GameSettings.getMeeplePath(terrainType, isTemplate));
            map.put(terrainType, meepleImage);
        }
        return map;
    }

    private static ImageIcon colorMaskBased(BufferedImage imageToColor, BufferedImage maskImage, Color targetColor) {
        BufferedImage image = deepCopy(imageToColor);
        for (int x = 0; x < maskImage.getWidth(); x++) {
            for (int y = 0; y < maskImage.getHeight(); y++) {
                Color maskPixel = new Color(maskImage.getRGB(x, y), true);
                Color targetPixel = new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), maskPixel.getAlpha());
                Color imagePixel = new Color(image.getRGB(x, y), true);
                Color blendedColor = blend(imagePixel, targetPixel, true);
                image.setRGB(x, y, blendedColor.getRGB());
            }
        }
        return new ImageIcon(image);
    }

    private static String createKey(Color color, TerrainType meepleType, int size) {
        return createKey(meepleType, size) + color.getRGB();
    }

    private static String createKey(TerrainType meepleType, int size) {
        return meepleType + KEY_SEPARATOR + size + KEY_SEPARATOR; // TODO (MEDIUM) choose more efficient composite key
    }

    // copies a image to avoid side effects.
    private static BufferedImage deepCopy(BufferedImage image) {
        ColorModel model = image.getColorModel();
        boolean isAlphaPremultiplied = model.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(model, raster, isAlphaPremultiplied, null);
    }

    // Colors a meeple with RGB color.
    private static Image paintMeeple(TerrainType meepleType, int color, int size) {
        BufferedImage image = deepCopy(imageMap.get(meepleType));
        BufferedImage template = templateMap.get(meepleType);
        for (int x = 0; x < template.getWidth(); x++) {
            for (int y = 0; y < template.getHeight(); y++) {
                if (template.getRGB(x, y) == Color.BLACK.getRGB()) {
                    image.setRGB(x, y, color);
                }
            }
        }
        return image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
    }
}