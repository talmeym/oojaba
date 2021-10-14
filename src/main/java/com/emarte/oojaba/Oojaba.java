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
        DisplayWindow window = new DisplayWindow();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(EntitySprite.getEntitySprites().size() > 0) {
                    if(JOptionPane.showConfirmDialog(window, "Save to disk before exiting ?", "Save before exit ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                        FileUtils.save(window.getCanvasPanel());
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
                    case VK_L:
                        if(new File(FileUtils.FILENAME_READ).exists()) {
                            if (JOptionPane.showConfirmDialog(window, "Load from disk ?", "Load ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                                FileUtils.load(canvasPanel);
                            }
                        } else {
                            JOptionPane.showMessageDialog(canvasPanel, "Nothing on disk to load", "I got nothing", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                    case VK_S:
                        if(EntitySprite.getEntitySprites().size() > 0) {
                            if (JOptionPane.showConfirmDialog(window, "Save to disk ?", "Save ?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION) {
                                FileUtils.save(canvasPanel);
                            }
                        } else {
                            JOptionPane.showMessageDialog(canvasPanel, "Nothing on screen to save", "I got nothing", JOptionPane.INFORMATION_MESSAGE);
                        }
                        break;
                }

                window.repaint();
            }
        });
    }
}
