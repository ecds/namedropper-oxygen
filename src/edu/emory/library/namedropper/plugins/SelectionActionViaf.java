/**
 * file src/edu/emory/library/namedropper/plugins/SelectionActionViaf.java
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

import java.util.List;

// Pop-Up Message for errors, selection dialog
import javax.swing.JOptionPane;

import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.CIElement;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.Action;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;

import edu.emory.library.namedropper.plugins.SelectionAction;

/**
 *  Use the VIAF API to look up user-selected text
 *  and prompt the user with suggested matches, and insert
 *  a name tag based on the current document type and
 *  the selected resource.
 */
public class SelectionActionViaf extends SelectionAction {

    public DocumentType docType;
    // TODO: should probably make this private and use a setter

    private static String name = "VIAF lookup";
    private static KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_V,
            InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);

    public SelectionActionViaf(StandalonePluginWorkspace ws) {
        super(ws);
        this.putValue(Action.NAME, this.name);
        this.putValue(Action.ACCELERATOR_KEY, this.shortcut);
    }

    public String processSelection(String selection) throws Exception {
        String result = selection;
        String currentDocType = this.workspace.getOptionsStorage().getOption("docType", "");
        // FIXME: must be a better way to do this; can we *store* as doctype?
        this.docType = DocumentType.fromString(currentDocType);

        // otherwise, do previously implemented behavior (viaf lookup)
        // TODO: shift to common select action ?
        if (this.tagAllowed() == false) {
            // if the tag is not allowed, throw an exception to be displayed
            // as a warning message to the user
            throw new Exception("Tag is not allowed in the current context");
        }


        result = this.queryVIAF(selection);
        if (result == null) { result = selection; }
        return result;
    }

    /**
     * Attempt to determine if the tag that will be added for this document type
     * is allowed in the current context based on the XML Schema, if available.
     * Returns true or false when a schema is available to determine definitively
     * if the tag is allowed or not.  Otherwise returns null.
     *
     * @return Boolean
     */
    public Boolean tagAllowed() {
        Boolean tagAllowed = null;

        WSTextEditorPage page = this.getCurrentPage();
        if (page == null) { return tagAllowed; }

        String tag = this.docType.getTagName();

        // use workspace context to get schema
        // cast as an xml text editor page if possible, for access to schema
        if (page != null && page instanceof WSXMLTextEditorPage) {
            WSTextEditorPage textpage = (WSXMLTextEditorPage) page;
            WSTextXMLSchemaManager schema = textpage.getXMLSchemaManager();
            int selectionOffset = textpage.getSelectionStart();
            try {
                // use the schema to get a context-based list of allowable elements
                WhatElementsCanGoHereContext elContext = schema.createWhatElementsCanGoHereContext(selectionOffset);
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

    /**
    * Query VIAF for name data
    *
    * @param  name  Name to query.
    * @return          String containing persname xml tag data.
    */
    public String queryVIAF(String name) throws Exception {
        // FIXME: may want to rename this method to be a little more descriptive

        String result = null;  // returned if no results are found

        // if document type is not set, don't bother to query VIAF
        // since we won't be able to add a tag
        if (this.docType == null) {
            throw new Exception("No DocType selected");
        }

        List<ViafResource> suggestions = ViafClient.suggest(name);
        if (suggestions.size() == 0) {
            throw new Exception("No Results");
        }
        ViafResource selection = this.getUserSelection(suggestions);

        // returns null if cancel button is clicked
        if (selection != null) {
            result = this.docType.makeTag(name, selection);
         }

        return result;
    }

    /**
     * Given a list of ViafResource objects, prompt the user to choose one
     * and return the selected resource, or null for no selection.
     */
    public ViafResource getUserSelection(List<ViafResource> suggestions) {
        Object[] choices = suggestions.toArray();
        // display pop-up box and return whatever value the user selects
        return (ViafResource) JOptionPane.showInputDialog(null, // parent component (?)
                 "Names", "Search Results",    // message/label
                 JOptionPane.PLAIN_MESSAGE, null,   // type of message / ?
                 choices, choices[0]);
        // FIXME: seems to be a unicode issue here; accented characters come through
        // fine in json response and as ViafResource label, but don't display correctly in the dialog
    }


}