package de.idiotischer.bob.render.menu.impl.select;

import de.idiotischer.bob.BOB;
import de.idiotischer.bob.country.Country;
import de.idiotischer.bob.render.menu.Component;
import de.idiotischer.bob.render.menu.components.button.ButtonComp;
import de.idiotischer.bob.render.menu.components.button.ButtonGroup;
import de.idiotischer.bob.render.menu.components.button.ButtonRow;
import de.idiotischer.bob.render.menu.components.button.IButtonComp;
import de.idiotischer.bob.scenario.Scenario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

public class CountrySelectMenu extends SelectMenu {

    private Scenario selectedScenario;
    private Country selectedCountry;

    private ButtonRow row;

    private final List<IButtonComp> iButtonComps = new ArrayList<>();
    private final List<Component> other = new ArrayList<>();

    private final ButtonGroup buttonGroup;

    private final ButtonComp startButton = new ButtonComp(
            "Start", Color.WHITE, Color.DARK_GRAY.darker(), true,
            320, -208, 95, 28,
            16, 16, 15,
            Color.DARK_GRAY.brighter(), Color.BLACK,
            true, (b) -> {

        if (selectedScenario == null || selectedCountry == null) return;

        BOB.getInstance().getScenarioSceneLoader().load(selectedScenario, true);
        BOB.getInstance().getPlayer().country(selectedCountry);
    });

    private final ButtonComp backMenuButton = new ButtonComp(
            "Back to Menu", Color.WHITE, Color.DARK_GRAY.darker(), true,
            0, -210, 138, 28,
            16, 16, 15,
            Color.DARK_GRAY.brighter(), Color.BLACK,
            true, (b) -> {
        BOB.getInstance().getMainRenderer().getMenuPanel().setInScenarioSelect(false);
    });

    private final ButtonComp backButton = new ButtonComp(
            "Scenario", Color.WHITE, Color.DARK_GRAY.darker(), true,
            -320, -208, 120, 28,
            16, 16, 15,
            Color.DARK_GRAY.brighter(), Color.BLACK,
            true, (b) -> {
        if (selectedScenario == null) return;

        BOB.getInstance().getMainRenderer().getMenuPanel()
                .setScenarioSelectMenu(new ScenarioSelectMenu(selectedScenario, parent, layoutScaleX, layoutScaleY));
    });

    public CountrySelectMenu(Scenario selected, JPanel panel, int layoutScaleX, int layoutScaleY) {
        super(panel, layoutScaleX, layoutScaleY);

        this.selectedScenario = selected;

        other.add(startButton);
        other.add(backMenuButton);
        other.add(backButton);

        buttonGroup = new ButtonGroup((pair) -> {
            IButtonComp btn = pair.left();
            if (btn == null) return;

            String id = btn.getId();
            if (id == null || id.isEmpty()) return;

            selectedCountry = BOB.getInstance().getCountries().getCountry(id);
        });

        reload();

        row = new ButtonRow(iButtonComps, 8, buttonGroup);
    }

    public void reload() {
        iButtonComps.clear();

        BOB.getInstance().getCountries().getCountrySet()
                .stream()
                .filter(Country::isMajor)
                .forEach(country -> iButtonComps.add(createButton(country)));

        if (row != null) {
            row.setButtons(iButtonComps);
        }
    }

    public IButtonComp createButton(Country country) {
        ButtonComp b = new ButtonComp(
                country.getAbbreviation(),
                country.countryName(),
                Color.WHITE,
                Color.BLACK,
                false,
                0, 0,
                300, 40,
                16, 16,
                3, Color.LIGHT_GRAY,
                Color.DARK_GRAY,
                true,
                buttonGroup,
                (_) -> {}
        );

        b.setPanel(parent);
        return b;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2 = (Graphics2D) g;

        row.getButtons().forEach(b -> {
            if (b instanceof ButtonComp c) {
                c.setPanel(parent);
            }
        });

        row.paint(g2);

        other.forEach(component -> {
            if (component instanceof IButtonComp c) {
                c.setPanel(parent);
                c.paint(g);
            }
        });
    }

    @Override
    public void mouseClick(MouseEvent e, int x, int y) {
        row.mouseClick(e, x, y);
        other.forEach(component -> component.mouseClick(e, x, y));
    }

    @Override
    public void mouseRelease(MouseEvent e, int x, int y) {
        row.mouseRelease(e, x, y);
        other.forEach(component -> component.mouseRelease(e, x, y));
    }

    @Override
    public void mouseMove(MouseEvent e, int x, int y) {
        row.mouseMove(e, x, y);
        other.forEach(component -> component.mouseMove(e, x, y));
    }

    @Override
    public void mouseScroll(MouseWheelEvent e, int x, int y) {
        row.mouseScroll(e, x, y);
        other.forEach(component -> component.mouseScroll(e, x, y));
    }

    @Override
    public void keyPress(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            BOB.getInstance().getMainRenderer().getMenuPanel().setInScenarioSelect(false);
        }
    }
}