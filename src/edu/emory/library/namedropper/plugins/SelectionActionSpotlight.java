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
import java.util.TreeMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

// oxygen dependencies
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;

// local
import edu.emory.library.spotlight.SpotlightClient;
import edu.emory.library.spotlight.SpotlightAnnotation;
import edu.emory.library.namedropper.plugins.SelectionAction;
import edu.emory.library.namedropper.ui.AnnotationPanel;
import edu.emory.library.utils.IntegerField;
import edu.emory.library.utils.DoubleField;

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
        this.findAnnotations(selection);
        return selection;
    }

    /**
     * Preliminary spotlight functionality: just display the identified resources
     * in a pop-up window as a simple way to show that the spotlight request is working
     */
    public void findAnnotations(String text) throws Exception {

        // regular expression to remove xml tags from the selected text
        Pattern xmlTag = Pattern.compile("(</?\\w+[ =:.\\w\\\"]*>)");
        Matcher m = xmlTag.matcher(text);
        // treemap to store offsets and adjustments (using treemap to retain sorting)
        TreeMap<Integer,Integer> offsetAdjustments = new TreeMap();
        // store offset and length of text removed so spotlight annotation
        // offsets can be matched back to original text with the xml tags
        Integer currentOffset = 0;
        Integer adjustedStart = 0;
        // iterate over matched xml tags in the selected text
        while (m.find()) {
            // start of the match relative to the text *without* xml tags
            adjustedStart = m.start() - currentOffset;
            // current total offset adjustment at this point (length of all removed tags)
            currentOffset += (m.end() - m.start());
            // store the offset where a tag was removed and the length of text removed
            offsetAdjustments.put(adjustedStart, currentOffset);
        }

        // replace all xml tags in the selected text with the empty string
        String[] textParts = xmlTag.split(text);  // split on regex and join segments
        text = "";
        for (int i = 0; i < textParts.length; i++) {
            text += textParts[i];
        }

        // query spotlight to annotate the text and display identified resources
        double confidence = Double.parseDouble(SelectionActionSpotlight.getSpotlightConfidence());
        int support = Integer.parseInt(SelectionActionSpotlight.getSpotlightSupport());
        SpotlightClient spot = new SpotlightClient(confidence, support);
        // store document offset for current selected text
        WSTextEditorPage ed = this.getCurrentPage();
        if (ed == null) { return; }
        int selectionOffset = ed.getSelectionStart();
        // clear user-selected text by setting an empty selection
        ed.select(selectionOffset, selectionOffset);

        // TODO: this should run in the background
        List<SpotlightAnnotation> annotations = spot.annotate(text);
        if (annotations.size() == 0) {
            throw new Exception("No resources were identified in the selected text");
        }

        // make the annotation panel visible if it isn't already
        this.workspace.showView(AnnotationPanel.VIEW_ID, false); // false = don't request focus
        AnnotationPanel panel = NameDropperPlugin.getInstance().getExtension().getAnnotationPanel();

        // Annotation offsets are relative to the selected text that was sent to Spotlight.
        // Adjust offsets to account for any xml tags removed from the selection
        Integer offset = null;
        for (SpotlightAnnotation sa : annotations) {
            // find the greatest offset less than or equal to the current annotation offset
            offset = offsetAdjustments.floorKey(sa.getOffset());
            // if there is an adjustment for this offset, use it
            if (offset != null) {
                sa.adjustOffset(offsetAdjustments.get(offset));
            }
            // all anntaotions must be adjusted so they are relative to the entire document
            sa.adjustOffset(selectionOffset);
        }

        // loop through the list and remove annotations where
        // names cannot be tagged in the context (e.g., previously tagged names)
        int i = 0;
        while (i < annotations.size()) {
            SpotlightAnnotation sa = annotations.get(i);
            if (this.tagAllowed(sa.getOffset()) == false) {
                annotations.remove(sa);
            } else {
                i++;
            }
        }

        // add them to the UI annotation panel for display and user interaction
        panel.addAnnotations(annotations);
    }


    // option keys
    public static String SPOTLIGHT_CONFIDENCE = "NameDropper:SpotlightConfidence";
    public static String SPOTLIGHT_SUPPORT = "NameDropper:SpotlightSupport";

    public boolean hasUserOptions() {
        return true;
    }

    // TODO: find sensible defaults... where to store them?

    // methods to get and store user-configured spotlight values in plugin options
    public static String getSpotlightConfidence() {
        return PluginOptions.getOption(SelectionActionSpotlight.SPOTLIGHT_CONFIDENCE, "0.4");
    }
    public static void setSpotlightConfidence(String value) {
        PluginOptions.setOption(SelectionActionSpotlight.SPOTLIGHT_CONFIDENCE, value);
    }
    public static String getSpotlightSupport() {
        return PluginOptions.getOption(SelectionActionSpotlight.SPOTLIGHT_SUPPORT, "200");
    }
    public static void setSpotlightSupport(String value) {
        PluginOptions.setOption(SelectionActionSpotlight.SPOTLIGHT_SUPPORT, value);
    }

    // action for a dialog to show user-configurable parameters
    public Action getOptionsAction() {
        final Action showOptions = new AbstractAction() {
            public void actionPerformed(ActionEvent selection) {
                String dialogLabel = "DBpedia Spotlight settings";

                IntegerField support = new IntegerField(4);
                support.setValue(SelectionActionSpotlight.getSpotlightSupport());

                DoubleField confidence = new DoubleField(4);
                confidence.setValue(SelectionActionSpotlight.getSpotlightConfidence());

                JPanel optionPanel = new JPanel();
                // for simplicity, using simple grid layout: label, input
                java.awt.GridLayout layout = new java.awt.GridLayout(2,3);  // rows, columns
                optionPanel.setLayout(layout);
                optionPanel.add(new JLabel("Confidence: "));
                optionPanel.add(confidence);
                optionPanel.add(new JLabel("Support: "));
                optionPanel.add(support);
                // TODO: would be nice to add help or tips about these values

                int result = JOptionPane.showConfirmDialog((java.awt.Frame)workspace.getParentFrame(),
                    optionPanel, dialogLabel, JOptionPane.OK_CANCEL_OPTION);

                // if dialog was closed by clicking OK, store updated values
                if (result == JOptionPane.OK_OPTION) {
                    SelectionActionSpotlight.setSpotlightConfidence(confidence.getText());
                    SelectionActionSpotlight.setSpotlightSupport(support.getText());
                } // on cancel, do nothing (don't save changes)
            }
        };

        return showOptions;
    }


}
