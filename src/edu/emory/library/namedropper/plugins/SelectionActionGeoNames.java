/**
 * file src/edu/emory/library/namedropper/plugins/SelectionActionGeoNames.java
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
import java.util.Arrays;

// Pop-Up Message for errors, selection dialog
import javax.swing.JOptionPane;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

// geonames.org api client
import org.geonames.WebService;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.PluginOptions;

import edu.emory.library.namedropper.plugins.SelectionAction;

/**
 *  Use the GeoNames.org API to look up user-selected text
 *  and prompt the user with suggested matches, and insert
 *  a place name tag based on the current document type and
 *  the selected resource.
 */
public class SelectionActionGeoNames extends SelectionAction {

    public static String shortName = "GeoNames";
    private static String name = "GeoNames.org lookup";
    private static KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_G,
            InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);

    public SelectionActionGeoNames(StandalonePluginWorkspace ws) {
        super(ws);
        this.putValue(Action.NAME, this.name);
        this.putValue(Action.ACCELERATOR_KEY, this.shortcut);
    }

    public String getShortName() { return this.shortName; }

    public String processSelection(String selection) throws Exception {
        String result = selection;

        // check if geographic name tag is allowed
        if (this.tagAllowedAtSelection(DocumentType.NameType.GEOGRAPHIC) == false) {
            // if the tag is not allowed, throw an exception to be displayed
            // as a warning message to the user
            throw new Exception("Tag is not allowed in the current context");
        }

        result = this.queryGeoNames(selection);
        if (result == null) { result = selection; }
        return result;
    }

    /**
    * Query GeoNames.org based on the selectionn
    *
    * @param  text  text to query
    * @return       String containing geographic xml tag data.
    */
    public String queryGeoNames(String text) throws Exception {
        String result = null;  // returned if no results are found

        // shouldn't be called if doctype isn't set, but check and bail out just in case
        if (this.docType == null) { return result; }
        String username = SelectionActionGeoNames.getGeoNamesUsername();
        if (username.isEmpty()) {
            throw new Exception("Please set a GeoNames.org username");
        }

        WebService.setUserName(username);
        ToponymSearchCriteria search = new ToponymSearchCriteria();
        search.setQ(text);
        ToponymSearchResult searchResult = WebService.search(search);
        if (searchResult.getTotalResultsCount() == 0) {
            throw new Exception("No Results");
        }

        Toponym selection = this.getUserSelection(searchResult.getToponyms());
        // returns null if cancel button is clicked
        if (selection != null) {
            result = this.docType.makeTag(text, selection);
         }

        return result;
    }

    /**
     * Given a list of Toponyms, prompt the user to choose one
     * and return the selected resource, or null for no selection.
     */
    public Toponym getUserSelection(List<Toponym> suggestions) {
        Object[] choices = suggestions.toArray();
        String[] labels = new String[suggestions.size()];
        // the toString method for a Toponym isn't suited to user display,
        // so build a list of labels from the Toponyms
        for (int i = 0; i < suggestions.size(); i++) {
            Toponym t = suggestions.get(i);
            labels[i] =  t.getName();
            // not all places have a country; list it if available
            if (! t.getCountryName().isEmpty()) {
                // NOTE: for U.S. cities, it would be nice to have state as well,
                // but the additional hierarchy API call for each item is too slow
                labels[i] += String.format(" (%s)", t.getCountryName());
            }
        }
        // display pop-up box and return a toponym based on the value the user selects
        Object choice = JOptionPane.showInputDialog(null, // parent component (?)
                 "Names", "Search Results",    // message/label
                 JOptionPane.PLAIN_MESSAGE, null,   // type of message / ?
                 labels, labels[0]);  // default to first option selected
        // no selection
        if (choice == null) {
            return null;
        } else {
            // find corresponding toponym based on selected label
            return suggestions.get(Arrays.asList(labels).indexOf(choice));
        }

    }

    // option keys
    public static String GEONAMES_USERNAME = "NameDropper:GeoNames_username";

    public boolean hasUserOptions() {
        return true;
    }

    // methods to get and store user-configured geonames values in plugin options
    public static String getGeoNamesUsername() {
        return PluginOptions.getOption(SelectionActionGeoNames.GEONAMES_USERNAME);
    }
    public static void setGeoNamesUsername(String value) {
        PluginOptions.setOption(SelectionActionGeoNames.GEONAMES_USERNAME, value);
    }

    // action for a dialog to show user-configurable parameters
    public Action getOptionsAction() {
        final Action showOptions = new AbstractAction() {
            public void actionPerformed(ActionEvent selection) {
                String dialogLabel = "GeoNames.org settings";

                JTextField username = new JTextField(SelectionActionGeoNames.getGeoNamesUsername(), 15);

                JPanel optionPanel = new JPanel();
                // for simplicity, using simple grid layout: label, input
                java.awt.GridLayout layout = new java.awt.GridLayout(2,2);  // rows, columns
                optionPanel.setLayout(layout);
                optionPanel.add(new JLabel("GeoNames API Username: "));
                optionPanel.add(username);

                int result = JOptionPane.showConfirmDialog((java.awt.Frame)workspace.getParentFrame(),
                    optionPanel, dialogLabel, JOptionPane.OK_CANCEL_OPTION);

                // if dialog was closed by clicking OK, store updated values
                if (result == JOptionPane.OK_OPTION) {
                    SelectionActionGeoNames.setGeoNamesUsername(username.getText());
                } // on cancel, do nothing (don't save changes)
            }
        };

        return showOptions;
    }



}