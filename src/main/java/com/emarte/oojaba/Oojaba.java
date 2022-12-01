package com.emarte.oojaba;

import com.emarte.oojaba.gui.CanvasPanel;
import com.emarte.oojaba.gui.DisplayWindow;
import com.emarte.oojaba.gui.EntitySprite;
import com.emarte.oojaba.util.FileUtils;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import static java.awt.event.KeyEvent.*;

public class Oojaba {
    public static void main(String[] args) {
        File file = new File(FileUtils.FILENAME);

        DisplayWindow window = new DisplayWindow(file);

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(EntitySprite.getEntitySprites().size() > 0) {
                    if(JOptionPane.showConfirmDialog(window, "Save to disk before exiting ?", "Save entities.json ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                        FileUtils.save(window.getCanvasPanel(), file);
                    }
                }

                System.exit(0);
            }
        });

        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                CanvasPanel canvasPanel = window.getCanvasPanel();

                switch(e.getKeyCode()) {
                    case VK_P: canvasPanel.setPointerMouseMode(); break;
                    case VK_E: canvasPanel.setEraserMouseMode(); break;
                    case VK_C: canvasPanel.setConnectorMouseMode(); break;
                    case VK_V: canvasPanel.setViewerMouseMode(); break;
                    case VK_D: canvasPanel.setEditorMouseMode(); break;
                    case VK_A: canvasPanel.setPainterMouseMode(); break;
                    case VK_W: canvasPanel.makeWider(300); break;
                    case VK_H: canvasPanel.makeTaller(300); break;
                    case VK_G: canvasPanel.setShowGrid(!canvasPanel.getShowGrid()); break;
                    case VK_O: canvasPanel.setShowConnectionsOutwards(!canvasPanel.getShowConnectionsOutwards()); break;
                    case VK_U: canvasPanel.setShowUsedEndPoints(!canvasPanel.getShowUsedEndPoints()); break;
                }

                window.repaint();
            }
        });
    }
}
