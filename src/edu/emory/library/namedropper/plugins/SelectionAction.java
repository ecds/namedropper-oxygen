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
import javax.swing.Action;
// Used when getting full stack trace
import java.io.PrintWriter;
import java.io.StringWriter;


import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.CIElement;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.PluginOptions;


/**
 * Base-class for plugin selection-based Actions.
 */
public abstract class SelectionAction extends AbstractAction {
    /**
     * Plugin workspace access.
     */
    protected StandalonePluginWorkspace workspace;

    public DocumentType docType;
    // should this be made private? initialized via pluginoptions

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
     *  the menu.  Gets the selected text, currently configured document type,
     *  then calls processSelection if there is any text, and then updates
     *  the document with the result.
     *  Displays a message dialog to the user if any exception occurs
     *  during the selection processing.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            String selection = this.getSelection();
            if (selection == null) { return; }
            this.preprocess();
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

    /**
     * Common pre-processing when handling a selection,
     * before calling processSelection.
     * Currently, checks that document type is set.
     */
    public void preprocess() throws Exception {
        String currentDocType = PluginOptions.getDocumentType();
        // FIXME: must be a better way to do this; can we *store* as doctype?
        this.docType = DocumentType.fromString(currentDocType);
        // if document type is not set, we can't check for allowed tags or query VIAF
        if (this.docType == null) {
            throw new Exception("No Document Type has been selected");
        }
    }

    /**
     * Check if a tag is allowed at the location of the current selection.
     * Gets selection offset and then calls tagAllowed.
     */
    public Boolean tagAllowedAtSelection() {
        WSTextEditorPage ed = this.getCurrentPage();
        int selectionOffset = ed.getSelectionStart();
        return tagAllowed(selectionOffset);
    }

    /**
     * Variant of tagAllowed method with no nametype (uses generic
     * name tag for current document type).
     */
    public Boolean tagAllowed(int offset) {
        return this.tagAllowed(offset, null);
    }

    /**
     * Attempt to determine if the tag that will be added for this document type
     * is allowed in the current context based on the XML Schema, if available.
     * Returns true or false when a schema is available to determine definitively
     * if the tag is allowed or not.  Otherwise returns null.
     *
     * @return Boolean
     */
    public Boolean tagAllowed(int offset, DocumentType.NameType nt) {
        Boolean tagAllowed = null;

        // shouldn't be called if doctype isn't set, but check and bail out just in case
        if (this.docType == null) { return tagAllowed; }

        WSTextEditorPage page = this.getCurrentPage();
        if (page == null) { return tagAllowed; }

        String tag = null;
        if (nt == null) {
            // get generic tag name for this docuent
            tag = this.docType.getTagName();
        } else {
            // get a tag specific to the type of name
            tag = this.docType.getTagName(nt);
        }

        // use workspace context to get schema
        // cast as an xml text editor page if possible, for access to schema
        if (page != null && page instanceof WSXMLTextEditorPage) {
            WSTextEditorPage textpage = (WSXMLTextEditorPage) page;
            WSTextXMLSchemaManager schema = textpage.getXMLSchemaManager();
            try {
                // use the schema to get a context-based list of allowable elements
                WhatElementsCanGoHereContext elContext = schema.createWhatElementsCanGoHereContext(offset);
                java.util.List<CIElement> elements;
                elements = schema.whatElementsCanGoHere(elContext);
                tagAllowed = false;
                // loop through the list to see if the tag we want to add
                // matches a name on any of the allowed elements
                for (int i=0; elements != null && i < elements.size(); i++) {
                    ro.sync.contentcompletion.xml.CIElement el = elements.get(i);
                    if (el.getName().equals(tag)) {
                        tagAllowed = true;
                        break;
                    }
                }
            } catch (javax.swing.text.BadLocationException e) {
                tagAllowed = null;
            }
        }

       return tagAllowed;
    }


    /** place-holder methods for action subclasses that have user-configurable options */

    /**
     * If true, indicates this selection action has user-configurable options
     * which should be made accessible from the top-level NameDropper menu.
     */
    public boolean hasUserOptions() {
        return false;
    }

    /**
     * If hasUserOptions returns true, this method should return an Action
     * to display a dialog box with whatever user options are appropriate
     * for this selection action.
     */
    public Action getOptionsAction() {
        return null;
    }


}
