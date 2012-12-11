/**
 * file src/edu/emory/library/namedropper/ui/AnnotationPanel.java
 *
 * Copyright 2012 Emory University Library
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.emory.library.namedropper.ui;

import java.util.List;

// swing imports
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JList;

// awt imports
import java.awt.Color;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

// oxygen imports
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.WSEditor;


// local imports
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.namedropper.plugins.PluginOptions;


public class AnnotationPanel extends JPanel {

    public static String VIEW_ID = "AnnotationViewID";
    public static String TITLE = "NameDropper Annotations";

    private JScrollPane scrollPane;
    private JPanel scrollContent;
    private JPanel fillerPanel;
    private JButton btnAcceptAll;
    private int currentRow = 0;

    public AnnotationPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.BLACK);

        this.add(getScrollPane(), BorderLayout.CENTER);

        btnAcceptAll = new JButton("Add Row");
        btnAcceptAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addStringToList("row " + currentRow);
                System.out.println("test");
            }
        });
        this.add(btnAcceptAll, BorderLayout.SOUTH);
    }

    private JScrollPane getScrollPane() {
        scrollContent = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        //gbc.gridx = gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        fillerPanel = new JPanel();
        scrollContent.add(fillerPanel, gbc);

        scrollPane = new JScrollPane(scrollContent);
        return scrollPane;
    }

    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.anchor = (x == 0) ? GridBagConstraints.FIRST_LINE_START : GridBagConstraints.FIRST_LINE_START;
        //gbc.fill = (x == 0) ? GridBagConstraints.BOTH : GridBagConstraints.HORIZONTAL;

        //Insets(top, left, bottom, right)
        gbc.insets = (y == 0) ? new Insets(3, 5, 3, 5) : new Insets(0, 5, 3, 5);
        gbc.weightx = (x == 0) ? 0.1 : 1.0;
        gbc.weighty = 0.0;

        return gbc;
    }

    public void setResults(List<SpotlightAnnotation> annotations, int offset) {
    	scrollContent.removeAll();
    	currentRow = 0;

    	// iterate through annotations to add each to the display
    	for (SpotlightAnnotation sa : annotations) {
    		//this.addStringToList(sa.getSurfaceForm());
            this.addAnnotationToList(sa, offset);
    	}

    	// create gbc for the filler div to ensure layout starts at the top
    	GridBagConstraints gbc = createGbc(0, currentRow+1);
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        scrollContent.add(fillerPanel, gbc);

        // revalidate and repaint the scrollContent to ensure it gets added to the view
        scrollContent.revalidate();
        scrollContent.repaint();
    }

    private void addStringToList(String str) {
        GridBagConstraints gbc = createGbc(0, currentRow);
        scrollContent.add(new JCheckBox(), gbc);

        gbc = createGbc(1, currentRow);
        gbc.ipady = 3;
        JLabel label = new JLabel(str);
        scrollContent.add(label, gbc);

        currentRow++;
    }


    private void addAnnotationToList(final SpotlightAnnotation sa, final int offset) {
        GridBagConstraints gbc = createGbc(0, currentRow);
        scrollContent.add(new JCheckBox(), gbc);

        gbc = createGbc(1, currentRow);
        gbc.ipady = 3;
        JLabel label = new JLabel(sa.getSurfaceForm());

        label.addMouseListener(new java.awt.event.MouseAdapter() {  // java.awt.event.MouseListener
            public void mouseClicked(MouseEvent e) {
                PluginWorkspace ws = PluginOptions.getWorkspace();
                WSTextEditorPage ed = null;
                WSEditor editorAccess = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess != null && editorAccess.getCurrentPage() instanceof WSTextEditorPage) {
                    ed = (WSTextEditorPage)editorAccess.getCurrentPage();
                    // test setting caret position
                    int start = sa.getOffset() + offset;
                    ed.setCaretPosition(start);
                    ed.select(start, start + sa.getSurfaceForm().length());
                }
            }
        });


        scrollContent.add(label, gbc);

        currentRow++;

    }

}