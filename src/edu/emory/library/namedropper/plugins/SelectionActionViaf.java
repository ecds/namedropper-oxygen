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

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.Action;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.PluginOptions;
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

    public static String shortName = "VIAF";
    private static String name = "VIAF lookup";
    private static KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_V,
            InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);

    public SelectionActionViaf(StandalonePluginWorkspace ws) {
        super(ws);
        this.putValue(Action.NAME, this.name);
        this.putValue(Action.ACCELERATOR_KEY, this.shortcut);
    }

    public String getShortName() { return this.shortName; }

    public String processSelection(String selection) throws Exception {
        String result = selection;

        // otherwise, do previously implemented behavior (viaf lookup)
        if (this.tagAllowedAtSelection() == false) {
            // if the tag is not allowed, throw an exception to be displayed
            // as a warning message to the user
            throw new Exception("Tag is not allowed in the current context");
        }


        result = this.queryVIAF(selection);
        if (result == null) { result = selection; }
        return result;
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

        // shouldn't be called if doctype isn't set, but check and bail out just in case
        if (this.docType == null) { return result; }

        List<ViafResource> suggestions = ViafClient.suggest(name);
        if (suggestions.size() == 0) {
            throw new Exception("No Results");
        }
        ViafResource selection = this.getUserSelection(suggestions);

        // returns null if cancel button is clicked
        if (selection != null) {

            // double-check that tag is allowed against the actual tag
            // to be inserted (not just generic document-type tag)
            DocumentType.NameType nt = DocumentType.NameType.fromString(selection.getType());
            if (this.tagAllowedAtSelection(nt) == false) {
                // warn user that tag is not allowed, and do not insert
                throw new Exception("Tag is not allowed in the current context");
            }
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