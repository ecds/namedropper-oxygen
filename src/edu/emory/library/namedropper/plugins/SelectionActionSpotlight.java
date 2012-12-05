/**
 * file src/edu/emory/library/namedropper/plugins/SelectionActionSpotlight.java
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

import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import edu.emory.library.spotlight.SpotlightClient;
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.namedropper.plugins.SelectionAction;
import edu.emory.library.namedropper.ui.DBPediaPanel;

/**
 *  Use the DBpedia Spotlight API to annotate user-selected text
 *  and show the user a list of identified resources.
 */
public class SelectionActionSpotlight extends SelectionAction {

    public static String shortName = "DBpedia Spotlight";
    private static String name = "DBpedia Spotlight annotation";
    private static String description = "Identify resources in selected text";
    private static KeyStroke shortcut = KeyStroke.getKeyStroke(KeyEvent.VK_D,
            InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);

    public SelectionActionSpotlight(StandalonePluginWorkspace ws) {
        super(ws);
        this.putValue(Action.NAME, this.name);
        this.putValue(Action.SHORT_DESCRIPTION, this.description); // does Oxygen use this?
        this.putValue(Action.ACCELERATOR_KEY, this.shortcut);
    }

    public String getShortName() { return this.shortName; }

    public String processSelection(String selection) throws Exception {
        this.showAnnotations(selection);
        return selection;
    }

    /**
     * Preliminary spotlight functionality: just display the identified resources
     * in a pop-up window as a simple way to show that the spotlight request is working
     */
    public void showAnnotations(String text) throws Exception {
        // annotate the text and display identified resources
        SpotlightClient spot = new SpotlightClient();
        List<SpotlightAnnotation> annotations = spot.annotate(text);

        String message = "DBpedia Spotlight identified the following resources in the selected text:\n\n";
        for (SpotlightAnnotation sa : annotations) {
            message += String.format("\t%s :\t%s\n", sa.getSurfaceForm(), sa.getUri());
        }

        JOptionPane.showMessageDialog((java.awt.Frame)this.workspace.getParentFrame(),
            message, "DBpedia Spotlight annotations",
            JOptionPane.INFORMATION_MESSAGE);

        DBPediaPanel panel = NameDropperPlugin.getInstance().getExtension().getDBPediaPanel();

        panel.setResults(annotations);

    }

}
