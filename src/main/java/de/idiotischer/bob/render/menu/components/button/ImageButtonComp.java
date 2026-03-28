package de.idiotischer.bob.render.menu.components.button;

import de.idiotischer.bob.BOB;
import de.idiotischer.bob.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class ImageButtonComp implements IButtonComp {

    private String text;
    private String id;

    private final Color selectedColor;
    private ButtonGroup group;

    private Rectangle bounds;
    private BufferedImage image;
    private final Color textColor;
    private final Consumer<ImageButtonComp> onClick;
    private final int arcWidth;
    private final int arcHeight;
    private final boolean centered;
    private final int borderWidth;
    private final Color borderColor;
    private final Color borderColorWhenHover;
    private final boolean unselectWhenUnhover;

    private boolean hovered = false;
    private boolean pressed = false;
    private boolean selected = false;

    private long lastClickTime = 0L;
    private static final long CLICK_THRESHOLD = 200L;
    private JPanel panel;
    private boolean debug = false;
    private boolean useImgHeight = true;

    private int imgWidth = 1;
    private int imgHeight = 1;
    private int imgOffsetX = 0;
    private int imgOffsetY = 0;

    public ImageButtonComp(String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, BufferedImage image, boolean centered, Consumer<ImageButtonComp> onClick) {
        this("", text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, image, centered, null, onClick);
    }

    public ImageButtonComp(String id, String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, BufferedImage image, boolean centered, ButtonGroup group, Consumer<ImageButtonComp> onClick) {
        this(id, text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, image, centered, group, onClick);
    }

    public ImageButtonComp(String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, BufferedImage image, boolean centered, ButtonGroup group, Consumer<ImageButtonComp> onClick) {
        this("", text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, image, centered, group, onClick);
    }

    public ImageButtonComp(String id, String text, Color textColor, Color selectedColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, BufferedImage image, boolean centered, ButtonGroup group, Consumer<ImageButtonComp> onClick) {
        this.id = id;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.image = image;
        this.textColor = textColor;
        this.selectedColor = selectedColor;
        this.onClick = onClick;
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.centered = centered;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.borderColorWhenHover = borderColorWhenHover;
        this.unselectWhenUnhover = unselectWhenUnhover;
        this.group = group;

        if (this.group != null) {
            this.group.add(this);
        }
    }

    private AffineTransform getGlobalTransform() {
        return BOB.getInstance().getMainRenderer().getViewportTransform();
    }

    private Point2D getTransformedPoint(Point screenPoint) {
        try {
            return getGlobalTransform().inverseTransform(screenPoint, null);
        } catch (NoninvertibleTransformException e) {
            return screenPoint;
        }
    }

    public Rectangle getActualBounds() {
        JPanel pl = panel != null ? panel : BOB.getInstance().getMainRenderer().getGamePanel();
        int x = centered ? (pl.getWidth() - bounds.width) / 2 : 0;
        int y = centered ? (pl.getHeight() - bounds.height) / 2 : 0;

        x += bounds.x;
        y -= bounds.y;

        return new Rectangle(x, y, bounds.width, bounds.height);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.transform(getGlobalTransform());

        Rectangle r = getActualBounds();
        int x = r.x;
        int y = r.y;

        g2.setColor((hovered && borderWidth > 0) ? borderColorWhenHover : borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(x, y, bounds.width, bounds.height, arcWidth, arcHeight);

        if (selected) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, bounds.width, bounds.height, arcWidth, arcHeight);
        }

        if (image != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int widthImg = useImgHeight ? bounds.width : imgWidth;
            int heightImg = useImgHeight ? bounds.height : imgHeight;

            Image scaled = image.getScaledInstance(widthImg, heightImg, Image.SCALE_SMOOTH);

            g2.drawImage(ImageUtil.makeRoundedCorner(scaled, widthImg, heightImg, arcHeight),
                    x + imgOffsetX, y + imgOffsetY, widthImg, heightImg, null
            );
        }

        g2.setColor(textColor);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (bounds.width - fm.stringWidth(text)) / 2;
        int textY = y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();

        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    @Override
    public void mouseClick(MouseEvent e, int x, int y) {
        Point2D p = getTransformedPoint(e.getPoint());
        if (getActualBounds().contains(p) && onClick != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime < CLICK_THRESHOLD) return;
            lastClickTime = currentTime;

            if (group != null) group.select(this);
            onClick.accept(this);
        }
        pressed = false;
    }

    @Override
    public void mouseRelease(MouseEvent e, int x, int y) {
        Point2D p = getTransformedPoint(e.getPoint());
        pressed = getActualBounds().contains(p);
    }

    @Override
    public void mouseMove(MouseEvent e, int x, int y) {
        Point2D p = getTransformedPoint(e.getPoint());
        hovered = getActualBounds().contains(p);
    }

    public void setBounds(Rectangle bounds) { this.bounds = bounds; }

    public void setPanel(JPanel pl) { this.panel = pl; }

    public Rectangle getBounds() { return bounds; }

    @Override public boolean isSelected() { return selected; }

    @Override public void setSelected(boolean selected) { this.selected = selected; }

    public String getText() { return text; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public void setText(String text) { this.text = text; }

    public void setDebug(boolean debug) { this.debug = debug; }

    @Override public void setGroup(ButtonGroup group) { this.group = group; }

    public void setImage(BufferedImage image) { this.image = image; }

    public void setImgWidth(int imgWidth) { this.imgWidth = imgWidth; }

    public void setImgHeight(int imgHeight) { this.imgHeight = imgHeight; }

    public void setUseImgHeight(boolean useImgHeight) { this.useImgHeight = useImgHeight; }

    public void setImgOffsetY(int i) { this.imgOffsetY = i; }

    public void setImgOffsetX(int i) { this.imgOffsetX = i; }
}