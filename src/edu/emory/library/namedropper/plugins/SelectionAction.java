/**
 * file src/edu/emory/library/namedropper/plugins/SelectionAction.java
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

package edu.emory.library.namedropper.plugins;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

// Pop-Up Message for errors, selection dialog
import javax.swing.JOptionPane;
// Used when getting full stack trace
import java.io.PrintWriter;
import java.io.StringWriter;


import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

/**
 * Base-class for plugin selection-based Actions.
 */
public abstract class SelectionAction extends AbstractAction {
    /**
     * Plugin workspace access.
     */
    protected StandalonePluginWorkspace workspace;

    SelectionAction(StandalonePluginWorkspace ws) {
        this.workspace = ws;
    }

    /**
     * Get the current page via plugin workspace.
     */
    public WSTextEditorPage getCurrentPage() {
        WSTextEditorPage ed = null;
        WSEditor editorAccess = workspace.getCurrentEditorAccess(StandalonePluginWorkspace.MAIN_EDITING_AREA);
        if (editorAccess != null && editorAccess.getCurrentPage() instanceof WSTextEditorPage) {
            ed = (WSTextEditorPage)editorAccess.getCurrentPage();
        }
        return ed;
    }

    /**
     * Get the currently selected text, if any.
     * Returns null if no selection could be retrieved.
     */
    public String getSelection() {
        String selection = null;
        WSTextEditorPage ed = this.getCurrentPage();
        if (ed == null) { return selection; }
        if (ed.hasSelection()) {
            selection = ed.getSelectedText();
        }
        return selection;
    }

    /**
     * Process the selected text.  Argument is the selected text retrieved by
     * getSelection, should return the string that will be used to replace the
     * selected text, or the original text if no changes should be made.
     */
    abstract String processSelection(String selection) throws Exception;

    // short name that will be used in menu to set default name lookup action
    abstract String getShortName();

    /**
     * Update the document based on processed selection-- replace
     * the selected text with the result, if they are different.
     */
    public void updateDocument(String selection, String result) {
        WSTextEditorPage ed = this.getCurrentPage();
        if (ed == null) { return; }

        if (! result.equals(selection)) {
            ed.beginCompoundUndoableEdit();
            int selectionOffset = ed.getSelectionStart();
            ed.deleteSelection();
            javax.swing.text.Document doc = ed.getDocument();
            try {
                doc.insertString(selectionOffset, result,
                    javax.swing.text.SimpleAttributeSet.EMPTY);
            } catch (javax.swing.text.BadLocationException b) {
                // should be ok from selection....
            }
            ed.endCompoundUndoableEdit();
        }
    }

    /**
     *  Run through the selection action when this item is triggered via
     *  the menu.  Gets the selected text and calls processSelection if
     *  there is any text, and then update the document with the result.
     *  Displays a message dialog to the user if any exception occurs
     *  during the selection processing.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            String selection = this.getSelection();
            if (selection == null) { return;}
            String result = this.processSelection(selection);
            this.updateDocument(selection, result);
        } catch (Exception e) {
            // This section is in case you want the whole stack trace in the error message
            // Pass sw.toString() instead of e.getMessage() in the showMessageDialog function
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();

            JOptionPane.showMessageDialog((java.awt.Frame)this.workspace.getParentFrame(),
                e.getMessage(), "Warning", JOptionPane.ERROR_MESSAGE);
        }

    }

}
