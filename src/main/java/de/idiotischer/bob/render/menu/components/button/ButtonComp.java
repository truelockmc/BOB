package de.idiotischer.bob.render.menu.components.button;

import de.idiotischer.bob.BOB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

public class ButtonComp implements IButtonComp {
    private String text;
    private String id;
    private final Color selectedColor;
    private ButtonGroup group;

    private Rectangle bounds;
    private final Color bgColor;
    private final Color textColor;
    private final Consumer<ButtonComp> onClick;
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

    private long lastClickTime = 0;
    private final long CLICK_THRESHOLD = 200;
    private JPanel panel;
    private boolean debug = false;


    public ButtonComp(String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, Color color, boolean centered, Consumer<ButtonComp> onClick) {
        this("", text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, color, centered, null, onClick);
    }

    public ButtonComp(String id, String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, Color color, boolean centered, ButtonGroup group, Consumer<ButtonComp> onClick) {
        this(id, text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, color, centered, group, onClick);
    }

    public ButtonComp(String text, Color textColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, Color color, boolean centered, ButtonGroup group, Consumer<ButtonComp> onClick) {
        this("", text, textColor, Color.WHITE, borderColor, unselectWhenUnhover, x, y, width, height, arcWidth, arcHeight, borderWidth, borderColorWhenHover, color, centered, group, onClick);
    }

    public ButtonComp(String id, String text, Color textColor, Color selectedColor, Color borderColor, boolean unselectWhenUnhover, int x, int y, int width, int height, int arcWidth, int arcHeight, int borderWidth, Color borderColorWhenHover, Color color, boolean centered, ButtonGroup group, Consumer<ButtonComp> onClick) {
        this.id = id;
        this.text = text;
        this.bounds = new Rectangle(x, y, width, height);
        this.bgColor = color;
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
        //später dann ne globaltransform ig
        return BOB.getInstance().getMainRenderer().getViewportTransform();
    }

    public Rectangle getLocalBounds() {
        JPanel pl = panel != null ? panel : BOB.getInstance().getMainRenderer().getGamePanel();
        int x = 0;
        int y = 0;

        if (centered && pl != null) {
            x = (pl.getWidth() - bounds.width) / 2;
            y = (pl.getHeight() - bounds.height) / 2;
        }

        x += bounds.x;
        y -= bounds.y;

        return new Rectangle(x, y, bounds.width, bounds.height);
    }

    private Point2D getTransformedPoint(Point screenPoint) {
        try {
            return getGlobalTransform().inverseTransform(screenPoint, null);
        } catch (NoninvertibleTransformException e) {
            return screenPoint;
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.transform(getGlobalTransform());

        Rectangle r = getLocalBounds();

        Color displayColor = bgColor;
        if (pressed && unselectWhenUnhover) {
            displayColor = bgColor.darker();
        } else if (hovered) {
            displayColor = bgColor.brighter();
        }

        g2.setColor(hovered && borderWidth > 0 ? borderColorWhenHover : borderColor);
        g2.setStroke(new BasicStroke(borderWidth));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, arcWidth, arcHeight);

        g2.setColor(displayColor);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, arcWidth, arcHeight);

        if (selected) {
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(r.x, r.y, r.width, r.height, arcWidth, arcHeight);
        }

        g2.setColor(textColor);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g2.getFontMetrics();
        int textX = r.x + (r.width - fm.stringWidth(text)) / 2;
        int textY = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(text, textX, textY);

        g2.dispose();
    }

    @Override
    public void mouseClick(MouseEvent e, int x, int y) {
        Point2D p = getTransformedPoint(e.getPoint());
        if (getLocalBounds().contains(p) && onClick != null) {
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
        pressed = getLocalBounds().contains(p);
    }

    @Override
    public void mouseMove(MouseEvent e, int x, int y) {
        Point2D p = getTransformedPoint(e.getPoint());
        hovered = getLocalBounds().contains(p);
    }

    public void setBounds(Rectangle bounds) { this.bounds = bounds; }
    public void setPanel(JPanel pl) { this.panel = pl; }
    public Rectangle getBounds() { return bounds; }
    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @Override public void setGroup(ButtonGroup group) { this.group = group; }
    public void setDebug(boolean debug) { this.debug = debug; }

}