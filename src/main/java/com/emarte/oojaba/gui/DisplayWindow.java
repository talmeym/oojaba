package com.emarte.oojaba.gui;

import com.emarte.oojaba.data.Entity;
import com.emarte.oojaba.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.emarte.oojaba.data.Entity.EntityType.SERVICE;
import static com.emarte.oojaba.gui.PaintMouseMode.COLOR_CHOOSER;

public class DisplayWindow extends JFrame implements CanvasChangeListener {
    private final CanvasPanel canvasPanel;
    JCheckBoxMenuItem showGridMenuItem;
    private final JCheckBoxMenuItem showConnectionsOutwardsItem;

    public DisplayWindow() {
        super("Oojaba");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        canvasPanel = new CanvasPanel(screenSize, this);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem loadItem = new JMenuItem("L - Load");
        loadItem.addActionListener(e -> {
            if (new File(FileUtils.FILENAME_READ).exists()) {
                if (JOptionPane.showConfirmDialog(canvasPanel, "Load from disk ?", "Load entities.json ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                    FileUtils.load(canvasPanel);
                    canvasPanel.repaint();
                }
            } else {
                JOptionPane.showMessageDialog(canvasPanel, "Nothing on disk to load", "I got nothing", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        loadItem.setToolTipText("Load from disk");
        fileMenu.add(loadItem);

        JMenuItem saveItem = new JMenuItem("S - Save");
        saveItem.addActionListener(e -> {
            if(EntitySprite.getEntitySprites().size() > 0) {
                if (JOptionPane.showConfirmDialog(canvasPanel, "Save to disk ?", "Save entities.json ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                    FileUtils.save(canvasPanel);
                }
            } else {
                JOptionPane.showMessageDialog(canvasPanel, "Nothing on screen to save", "I got nothing", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        saveItem.setToolTipText("Save to disk");
        fileMenu.add(saveItem);

        JMenu canvasMenu = new JMenu("Canvas");

        JMenuItem widerMenuItem = new JMenuItem("W - Increase Width");
        widerMenuItem.addActionListener(e -> {
            canvasPanel.makeWider(300);
            repaint();
        });

        widerMenuItem.setToolTipText("Make drawing area wider");
        canvasMenu.add(widerMenuItem);

        JMenuItem tallerMenuItem = new JMenuItem("H - Increase Height");
        tallerMenuItem.addActionListener(e -> {
            canvasPanel.makeTaller(300);
            repaint();
        });

        tallerMenuItem.setToolTipText("Make drawing area taller");
        canvasMenu.add(tallerMenuItem);

        showGridMenuItem = new JCheckBoxMenuItem("G - Show Grid", true);
        showGridMenuItem.addChangeListener(e -> {
            canvasPanel.setShowGrid(showGridMenuItem.isSelected());
            repaint();
        });

        showGridMenuItem.setToolTipText("Make grid lines in drawing area");
        canvasMenu.add(showGridMenuItem);

        JMenuItem setSelectedColorItem = new JMenuItem("Set Selection / Connection Colour");
        setSelectedColorItem.addActionListener(e -> {
            COLOR_CHOOSER.setColor(canvasPanel.getSelectedColor());
            JDialog dialog = JColorChooser.createDialog(canvasPanel, "Choose Selected Colour", true, COLOR_CHOOSER, e1 -> canvasPanel.setSelectedColor(COLOR_CHOOSER.getColor()), null);
            dialog.setVisible(true);
            repaint();
        });

        setSelectedColorItem.setToolTipText("Set colour to be used for showing selections & connections");
        canvasMenu.add(setSelectedColorItem);
        JMenuItem increaseTextSize = new JMenuItem("Increase Text Size");
        increaseTextSize.addActionListener(e -> {
            canvasPanel.increaseFontSize();
            EntitySprite.ENTITY_BORDER_SIZE += 5;
            EntitySprite.ENDPOINT_BORDER_SIZE += 1;
            repaint();
        });

        increaseTextSize.setToolTipText("Make drawing text larger");
        canvasMenu.add(increaseTextSize);

        showConnectionsOutwardsItem = new JCheckBoxMenuItem("O -Show Connections Outwards", true);
        showConnectionsOutwardsItem.addChangeListener(e -> {
            canvasPanel.setShowConnectionsOutwards(showConnectionsOutwardsItem.isSelected());
            repaint();
        });

        showConnectionsOutwardsItem.setToolTipText("Make drawing show connections made from the selected entity / endpoint outwards");
        canvasMenu.add(showConnectionsOutwardsItem);

        JCheckBoxMenuItem highlightUsedEndPointsItem = new JCheckBoxMenuItem("Highlight Used End Points", false);
        highlightUsedEndPointsItem.addChangeListener(e -> {
            canvasPanel.highlightUsedEndPoints(highlightUsedEndPointsItem.isSelected());
            repaint();
        });

        highlightUsedEndPointsItem.setToolTipText("Make drawing highlight end points that have involved in service connections");
        canvasMenu.add(highlightUsedEndPointsItem);

        JMenuItem decreaseTextSizeItem = new JMenuItem("Decrease Text Size");
        decreaseTextSizeItem.addActionListener(e -> {
            canvasPanel.decreaseFontSize();
            EntitySprite.ENTITY_BORDER_SIZE -= 5;
            EntitySprite.ENDPOINT_BORDER_SIZE -= 1;
            repaint();
        });

        decreaseTextSizeItem.setToolTipText("Make drawing text smaller");
        canvasMenu.add(decreaseTextSizeItem);

        JMenuItem moveAllDown = new JMenuItem("Move All Down");
        moveAllDown.addActionListener(e -> {
            canvasPanel.moveAllDown(100);
            repaint();
        });

        moveAllDown.setToolTipText("Move all drawing items down");
        canvasMenu.add(moveAllDown);

        JMenuItem moveAllRightItem = new JMenuItem("Move All Right");
        moveAllRightItem.addActionListener(e -> {
            canvasPanel.moveAllRight(100);
            repaint();
        });

        moveAllRightItem.setToolTipText("Move all drawing items right");
        canvasMenu.add(moveAllRightItem);

        JMenu addMenu = new JMenu("Add");

        JMenuItem addServiceMenuItem = new JMenuItem("Service");
        addServiceMenuItem.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter service name", "New Service", JOptionPane.INFORMATION_MESSAGE);

            if (name != null) {
                new Entity(name, SERVICE);
                canvasPanel.setPointerMouseMode();
                repaint();
            }
        });

        addServiceMenuItem.setToolTipText("Add a service to the drawing");
        addMenu.add(addServiceMenuItem);

        JMenu modeMenu = new JMenu("Mode");
        ButtonGroup buttonGroup = new ButtonGroup();

        for (MouseMode mouseMode : canvasPanel.getMouseModes()) {
            JRadioButtonMenuItem modeItem = new JRadioButtonMenuItem(mouseMode.getName());
            buttonGroup.add(modeItem);
            modeItem.setToolTipText(mouseMode.getTooltipText());
            modeMenu.add(modeItem);
            modeItem.addActionListener(e -> canvasPanel.setMouseMode(mouseMode));
            canvasPanel.addMouseModeChangeListener(m -> {
                if (m == mouseMode && !modeItem.isSelected()) {
                    modeItem.setSelected(true);
                }
            });
        }

        buttonGroup.setSelected(buttonGroup.getElements().nextElement().getModel(), true);

        menuBar.add(fileMenu);
        menuBar.add(canvasMenu);
        menuBar.add(addMenu);
        menuBar.add(modeMenu);
        setJMenuBar(menuBar);

        setContentPane(new JScrollPane(canvasPanel));
        setSize(screenSize);
        setVisible(true);
    }

    public CanvasPanel getCanvasPanel() {
        return canvasPanel;
    }

    @Override
    public void showGrid(boolean showGrid) {
        showGridMenuItem.setSelected(showGrid);
    }

    @Override
    public void showConnectionsOutwardsChanged(boolean showConnectionsOutwards) {
        showConnectionsOutwardsItem.setSelected(showConnectionsOutwards);
    }
}