package de.idiotischer.bob.render.menu.components.button;

import de.idiotischer.bob.render.menu.Component;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class ButtonRow implements Component {

    private final int maxButtons;
    private  List<IButtonComp> buttons;
    private final boolean vertical;
    private final ButtonGroup group;

    private final int padding = 10;
    private final int spacing = 10;

    public ButtonRow(List<IButtonComp> buttons, int maxButtons) {
        this(buttons, maxButtons, null,false);
    }

    public ButtonRow(List<IButtonComp> buttons, int maxButtons, ButtonGroup group) {
        this(buttons,maxButtons,group,false);
    }

    public ButtonRow(List<IButtonComp> buttons, int maxButtons, ButtonGroup group, boolean vertical) {
        this.buttons = buttons;
        this.group = group;
        this.vertical = vertical;
        this.maxButtons = maxButtons;

        fixup();

        if(group != null) {
            buttons.forEach(b -> b.setGroup(group));
        }

        layoutChildren();
    }

    //IMPORTANT BC THIS MODIFIES THE RECTANGLE
    //private void layoutChildren() {
    //    int yOffset = padding;
    //    int xOffset = padding;

    //    for (IButtonComp btn : buttons) {
    //        Rectangle b = btn.getBounds();

    //        if(vertical) {
    //            b.y += yOffset;
    //            yOffset += b.height + spacing;
    //        }
    //        else {
    //            b.x  += xOffset;
    //            xOffset += b.width + spacing;
    //        }
    //    }
    //}

    private void layoutChildren() {
        int yOffset = padding;

        for (IButtonComp child : buttons) {
            Rectangle b = child.getBounds();
            b.y += yOffset;
            yOffset += b.height + spacing;
        }
    }

    @Override
    public void paint(Graphics g) {
        buttons.forEach(b -> b.paint(g));
    }
    @Override
    public void mouseScroll(MouseWheelEvent e, int x, int y) {
        buttons.forEach(b -> {b.mouseScroll(e, x, y);});
    }

    @Override
    public void mouseClick(MouseEvent e, int x, int y) {
        buttons.forEach(component -> component.mouseClick(e, x, y));
    }

    @Override
    public void mouseRelease(MouseEvent e, int x, int y) {
        buttons.forEach(component -> component.mouseRelease(e, x, y));
    }

    @Override
    public void mouseMove(MouseEvent e, int x, int y) {
        buttons.forEach(component -> component.mouseMove(e, x, y));
    }


    //private void fixup() {
    //    if (buttons == null) return;
    //
    //    while (buttons.size() > maxButtons) {
    //        buttons.remove(buttons.size() - 1);
    //    }
    //}

    private void fixup() {
        if (buttons == null) return;

        if (buttons.size() > maxButtons) {
            buttons = buttons.subList(0, maxButtons);
        }
    }

    public boolean isVertical() {
        return vertical;
    }

    public ButtonGroup getGroup() {
        return group;
    }

    public void setButtons(List<IButtonComp> buttons) {
        this.buttons = buttons;
        fixup();
        layoutChildren();
    }

    public List<IButtonComp> getButtons() {
        return buttons;
    }
}
