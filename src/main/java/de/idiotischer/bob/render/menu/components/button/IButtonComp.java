package de.idiotischer.bob.render.menu.components.button;

import de.idiotischer.bob.render.menu.Component;

import javax.swing.*;
import java.awt.*;

public interface IButtonComp extends Component {
    boolean isSelected();
    void setSelected(boolean selected);
    String getId();

    Rectangle getBounds();

    void setGroup(ButtonGroup group);

    void setPanel(JPanel panel);
}
