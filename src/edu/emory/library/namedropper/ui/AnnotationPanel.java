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
import java.util.ArrayList;

// swing imports
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// awt imports
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// oxygen imports
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.WSEditor;

// local imports
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.namedropper.plugins.PluginOptions;
import edu.emory.library.namedropper.plugins.DocumentType;

public class AnnotationPanel extends JPanel {

    public static String VIEW_ID = "AnnotationViewID";
    public static String TITLE = "NameDropper Annotations";

    private JScrollPane scrollPane;
    private JTable table;


    /**
     * Abstract table model that uses a list of annotations
     * as the basis for each row of data in a table.
     */
    class AnnotationTableModel extends AbstractTableModel {
        private String[] columnNames = {"OK", "Recognized Name"};
        public static final int APPROVED = 0;
        public static final int NAME = 1;

        private List<SpotlightAnnotation> data = new ArrayList<SpotlightAnnotation>();
        private List<Boolean> approved = new ArrayList<Boolean>();

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case APPROVED:
                    return approved.get(row);

                case NAME:
                    // NOTE: this is a bit slow and should probably be done in the background, if possible
                    String name = data.get(row).getLabel();
                    // use recognized surface form, if query doesn't find a label
                    if (name == null || name.equals("")) {
                        return data.get(row).getSurfaceForm();
                    }
                    return name;

            }
            return null;
        }

        public void setValueAt(Object val, int row, int col) {
            if (col == APPROVED) {
                approved.set(row, (Boolean) val);
            }
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case APPROVED:             // selected for insert
                    return true;

                default:
                    return false;
            }
        }

        /**
         * Add a list of annotations and update the table to display them.
         */
        public void addAnnotations(List<SpotlightAnnotation> annotations) {
            int last_row = data.size();
            for (SpotlightAnnotation sa : annotations) {
                data.add(sa);
                // keep approved array consistent with data; init as true
                approved.add(true);
            }
            fireTableRowsInserted(last_row, data.size());
        }

        /**
         * Return the SpotlightAnnotation object for a specific row.
         */
        public SpotlightAnnotation getRowAnnotation(int row) {
            return data.get(row);
        }

        /**
         * Remove a row from the table by row number.
         */
        public void removeRowAnnotation(int row) {
            data.remove(row);
            approved.remove(row);
            fireTableRowsDeleted(row, row);
        }

        /**
         * Remove all associated annotations and update the table
         */
        public void clearAnnotations() {
            int size = data.size();
            data.clear();
            approved.clear();
            fireTableRowsDeleted(0, size);
        }

    } // end table model

    /**
     * Custom table cell renderer for annotations to add information
     * about the recognized resource via tooltip text.
     */
    public class AnnotationRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(
                                JTable table, Object obj,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
            // inherit all the default display logic
            super.getTableCellRendererComponent(table, obj,
                isSelected, hasFocus, row, column);

            // add a custom tool tip
            AnnotationTableModel model = (AnnotationTableModel) table.getModel();
            SpotlightAnnotation an = model.getRowAnnotation(row);

            // display the beginning of the resource description;
            // should be enough in most cases to see if it's the right thing or not
            // (eventually we'll probably want a way to expose more information)
            String description = an.getAbstract();
            // use dbpedia URI as a fallback if we can't get an abstract
            if (description == null || description.equals("")) {
                description = an.getUri();
            } else if (description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            setToolTipText(description);
            return this;
        }
    }

    public AnnotationPanel() {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.GRAY);

        // initialize table and scroll pane
        table = new JTable(new AnnotationTableModel());

        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        // force first column (check box) to be small
        TableColumn column = table.getColumnModel().getColumn(AnnotationTableModel.APPROVED);
        column.setPreferredWidth(5);  // this should work, but as far as I can tell Oxygen ignores it
        column.setMaxWidth(7);      // force the column to be minimal width

        // Customize tool tips for annotation text
        column = table.getColumnModel().getColumn(AnnotationTableModel.NAME);
        // DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        // renderer.setToolTipText("test tool tip");
        column.setCellRenderer(new AnnotationRenderer());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table.getSelectionModel();
        // add a row listener
        rowSM.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // Ignore extra messages.
                if (e.getValueIsAdjusting()) return;

                ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                if (! lsm.isSelectionEmpty()) {
                    int selectedRow = lsm.getMinSelectionIndex();

                    AnnotationTableModel model = (AnnotationTableModel) table.getModel();
                    SpotlightAnnotation an = model.getRowAnnotation(selectedRow);

                    // Based on the selected annotation, highlight the corresponding
                    // recognized text where it occurs in the document.
                    PluginWorkspace ws = PluginOptions.getWorkspace();
                    WSTextEditorPage ed = null;
                    WSEditor editorAccess = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
                    if (editorAccess != null && editorAccess.getCurrentPage() instanceof WSTextEditorPage) {
                        ed = (WSTextEditorPage)editorAccess.getCurrentPage();
                        int start = an.getOffset();
                        ed.setCaretPosition(start);
                        ed.select(start, start + an.getOriginalSurfaceForm().length());
                    }
                }
            }
        });

        this.add(scrollPane, BorderLayout.CENTER);

        // add a toolbar for control buttons (insert, clear)
        JToolBar toolbar = new JToolBar();  // default layout is horizontal

        // add a button to insert all annotations
        JButton insertAll = new JButton("Insert All");
        insertAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertAllAnnotations();
            }
        });
        toolbar.add(insertAll, BorderLayout.SOUTH);

        // insert selected still TODO
        JButton insertSelected = new JButton("Insert Selected");
        insertSelected.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                insertSelectedAnnotations();
            }
        });
        toolbar.add(insertSelected, BorderLayout.SOUTH);

        // add a button to clear all current annotations
        JButton clearAll = new JButton("Clear");
        clearAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AnnotationTableModel model = (AnnotationTableModel) table.getModel();
                model.clearAnnotations();

                // make document editable again
                PluginWorkspace ws = PluginOptions.getWorkspace();
                // NOTE: once annotations are tied to a specific document,
                // update this logic to make the correct document editable
                // TODO: consider shifting repeated logic this to a common location
                WSEditor editorAccess = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
                if (editorAccess != null && editorAccess.getCurrentPage() instanceof WSTextEditorPage) {
                    WSTextEditorPage ed = (WSTextEditorPage)editorAccess.getCurrentPage();
                    ed.setEditable(true);
                }

            }
        });
        toolbar.add(clearAll, BorderLayout.SOUTH);

        this.add(toolbar, BorderLayout.SOUTH);

    }

    /**
     * Add a list of annotations to the table.
     */
    public void addAnnotations(List<SpotlightAnnotation> annotations) {
        AnnotationTableModel model = (AnnotationTableModel) this.table.getModel();
        model.addAnnotations(annotations);
    }


    private void insertAllAnnotations() {
        insertAnnotations(false);
    }

    private void insertSelectedAnnotations() {
        insertAnnotations(true);
        // clear out unselected annotations
        AnnotationTableModel model = (AnnotationTableModel) this.table.getModel();
        model.clearAnnotations();
    }

    private void insertAnnotations(boolean selectedOnly) {
        AnnotationTableModel model = (AnnotationTableModel) table.getModel();

        // get access to the editor so we can update the document
        PluginWorkspace ws = PluginOptions.getWorkspace();
        WSTextEditorPage ed = null;
        WSEditor editorAccess = ws.getCurrentEditorAccess(PluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess != null && editorAccess.getCurrentPage() instanceof WSTextEditorPage) {

            // select the matched term so we can replace it with a tag
            ed = (WSTextEditorPage)editorAccess.getCurrentPage();

            // make document editable so we can insert tags
            ed.setEditable(true);

            // begin a single undoable edit for ALL inserted tags
            ed.beginCompoundUndoableEdit();

            String currentDocType = PluginOptions.getDocumentType();
            DocumentType docType = DocumentType.fromString(currentDocType);

            // iterate through each row in the table and insert tags for the annotation
            int offsetAdjust = 0;
            int i = 0;
            int start = 0;

            // loop through annotations, removing them as they are inserted,
            // until we run out of annotations to insert or get to the end
            while (i < model.getRowCount()) {
                SpotlightAnnotation an = model.getRowAnnotation(i);

                // if only inserting selected annotations, skip unapproved items
                if (selectedOnly && (Boolean) model.getValueAt(i, model.APPROVED) == false) {
                    // if there is an offset adjustment due to previous insertions,
                    // update the unselected annotation
                    if (offsetAdjust != 0) {
                        an.adjustOffset(offsetAdjust);
                    }

                    // increment to process the next annotation, skip to next loop
                    i++;
                    continue;
                }

                // otherwise, process the annotation and insert a tag

                // select recognized text for replacement
                start = an.getOffset() + offsetAdjust;
                ed.setCaretPosition(start);
                ed.select(start, start + an.getOriginalSurfaceForm().length());

                try {
                    String result = docType.makeTag(an);
                    // could error if annotation is an unsupported name type

                    // replace recognized word with tagged name
                    int selectionOffset = ed.getSelectionStart();
                    ed.deleteSelection();
                    javax.swing.text.Document doc = ed.getDocument();
                    try {
                        doc.insertString(selectionOffset, result,
                            javax.swing.text.SimpleAttributeSet.EMPTY);
                    } catch (javax.swing.text.BadLocationException b) {
                        // should be a valid location based on original selection
                    }

                    // keep track of change to upcoming offsets based on text added
                    offsetAdjust += result.length() - an.getOriginalSurfaceForm().length();

                    // remove the processed annotation
                    model.removeRowAnnotation(i);

                } catch (Exception err) {
                    // FIXME: handle exceptions better here...
                    // At *least* we should have logging or something

                    // error: increment so we move on to process the next annotation
                    i++;
                }

            }  // end looping through annotations

            ed.select(start, start);  // ensure no text is highlighted

            // end of the compound edit (inserted all/selected items)
            ed.endCompoundUndoableEdit();

        } // end editor access
    }

}