/**
 * file src/edu/emory/library/namedropper/plugins/NameDropperPluginExtension.java
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


// Oxygen components
import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.exml.plugin.selection.SelectionPluginExtension;
import ro.sync.exml.plugin.selection.SelectionPluginResult;
import ro.sync.exml.plugin.selection.SelectionPluginResultImpl;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.WSEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.contentcompletion.xml.CIElement;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

// Pop-Up Message for errors, selection dialog
import javax.swing.JOptionPane;

// JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

// XML Parsing
import nu.xom.Builder;
import nu.xom.Document;

// Used when getting full stack trace
import java.io.PrintWriter;
import java.io.StringWriter;

// local dependencies
import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;
import edu.emory.library.spotlight.SpotlightClient;
import edu.emory.library.spotlight.SpotlightAnnotation;

public class NameDropperPluginExtension implements SelectionPluginExtension {

    public static String eadLabel;
    public static String teiLabel;

    // used to determine ead tag
    public static HashMap eadTag = new HashMap();

    // used to determine tei type attribute
    public static HashMap teiType = new HashMap();

    public ViafClient viaf = new ViafClient();

    /*
     * Constructor
     */

    public NameDropperPluginExtension() {
        this.eadLabel = "EAD";
        this.teiLabel = "TEI";


        this.eadTag.put("Personal", "persname");
        this.eadTag.put("Corporate", "corpname");
        this.eadTag.put("Geographic", "geogname");


        this.teiType.put("Personal", "person");
        this.teiType.put("Corporate", "org");
        this.teiType.put("Geographic", "place");
    }

    /**
    * Lookup name in name authority.
    *
    * @param  context  Selection context.
    * @return          NameDropper plugin result.
    */
    public SelectionPluginResult process(SelectionPluginContext context) {

        //query VIAF for name data
        String orig = "";
        String result = "";
        String docType = context.getPluginWorkspace().getOptionsStorage().getOption("docType", "");

        try {
            orig = context.getSelection();
            result = orig; // put back original text if anything goes wrong

            // TEMPORARY: for now, trigger dbpedia annotation if selection is over an arbitrary word count
            Integer wordCount = orig.trim().split("\\s+").length;
            if (wordCount > 5) {
                this.showAnnotations(orig, context);
                // skip viaf lookup

            } else {
                // otherwise, do previously implemented behavior (viaf lookup)

                if (this.tagAllowed(docType, context) == false) {
                    // if the tag is not allowed, throw an exception to be displayed
                    // as a warning message to the user
                    throw new Exception("Tag is not allowed in the current context");
                }

                result = this.queryVIAF(orig, docType);

                if(result == null) {result = orig;}
            }
        } catch (Exception e) {
            // This section is in case you want the whole stack trace in the error message
            // Pass sw.toString() instead of e.getMessage() in the showMessageDialog function
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            e.printStackTrace();

            JOptionPane.showMessageDialog(context.getFrame(), e.getMessage(), "Warning",
                    JOptionPane.ERROR_MESSAGE);
        }

        return new SelectionPluginResultImpl(result);
    }

    // preliminary spotlight functionality: just display the identified resources
    // in a pop-up window as a simple way to show that the spotlight request is working
    public void showAnnotations(String text, SelectionPluginContext context) throws Exception {
        // annotate the text and display identified resources
        SpotlightClient spot = new SpotlightClient();
        List<SpotlightAnnotation> annotations = spot.annotate(text);

        String message = "DBpedia Spotlight identified the following resources in the selected text:\n\n";
        for (SpotlightAnnotation sa : annotations) {
            message += String.format("\t%s :\t%s\n", sa.getSurfaceForm(), sa.getUri());
        }

        JOptionPane.showMessageDialog(context.getFrame(),
            message, "DBpedia Spotlight annotations",
            JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * Determine what XML tag name to use for the specified document type.
     *
     * @param docType
     * @return String or null
     */
    public String getTagName(String docType) {
        return this.getTagName(docType, null);
    }

    /**
     * Determine what XML tag name to use for the specified document type
     * and type of name.
     *
     * @param docType (e.g., EAD or TEI)
     * @param nameType (Personal, Corporate, or Geographic)
     * @return String or null
     */
    public String getTagName(String docType, String nameType) {
        String tag = null;
        if (docType != null) {
            if (docType.equals(this.teiLabel)) {
                tag = "name";
            } else if (docType.equals(this.eadLabel) && nameType != null) {
                tag = (String) this.eadTag.get(nameType);
            }
        }
        return tag;
    }

    /**
     * Attempt to determine if the tag that will be added for this document type
     * is allowed in the current context based on the XML Schema, if available.
     * Returns true or false when a schema is available to determine definitively
     * if the tag is allowed or not.  Otherwise returns null.
     *
     * @param docType
     * @param context
     * @return Boolean
     */
    public Boolean tagAllowed(String docType, SelectionPluginContext context) {
        String tag = this.getTagName(docType);
        Boolean tagAllowed = null;
        // determine what tag (roughly) we will be adding
        if (tag == null && docType.equals(this.eadLabel)) {
            // use as generic stand-in for EAD, since all name tags
            // follow basically the same rules
            tag = "persname";
        }
        // use workspace context to get schema
        int workspaceId = StandalonePluginWorkspace.MAIN_EDITING_AREA;
        WSEditor ed = context.getPluginWorkspace().getCurrentEditorAccess(workspaceId);

        if (ed != null) {  // editor could be null if no workspace is initialized
            WSEditorPage page = ed.getCurrentPage();
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
        }

       return tagAllowed;
    }

    /**
    * Query VIAF for name data
    *
    * @param  name  Name to query.
    * @param  docType  String - EAD or TEI.
    * @return          String containing persname xml tag data.
    */
    public String queryVIAF(String name, String docType) throws Exception {
        // FIXME: may want to rename this method to be a little more descriptive

        String result = null;  // returned if no results are found

        // if document type is not set, don't bother to query VIAF
        // since we won't be able to add a tag
        if (docType == null || docType.equals("")) {
            throw new Exception("No DocType selected");
        }

        List<ViafResource> suggestions = this.viaf.suggest(name);
        if (suggestions.size() == 0) {
            throw new Exception("No Results");
        }
        ViafResource selection = this.getUserSelection(suggestions);

        // returns null if cancel button is clicked
        if (selection != null) {
            result = this.makeTag(name, selection, docType);
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

    /**
     * Generate an xml tag based on a name, a resource, and the type of document.  Uses
     * ViafResource type and viafid or URI to generate the appropriate tag and attributes.
     *
     * @param String name text of the name, which will be used as the content of the tag
     * @param ViafResource resource
     * @param String docType - type of document (EAD or TEI), to determine type of tag
     *     to insert
     *
     * @return String of the generated tag or null
     * @raises Exception if a resource has an unsupported name type
     */
    public String makeTag(String name, ViafResource resource, String docType) throws Exception {

        String result = null;
        String tag = null;
        String type = null;

        String nameType = resource.getType();

        // docType must be set
        if (docType.equals(this.eadLabel)){
            tag = this.getTagName(docType, nameType);
            if (tag == null) {
                throw new Exception("Unsupported nameType: " + nameType);
            }

            result = String.format("<%s source=\"viaf\" authfilenumber=\"%s\">%s</%s>", tag,
                resource.getViafId(), name, tag);

        } else if (docType.equals(this.teiLabel)) {
            tag = this.getTagName(docType);
            type = (String) this.teiType.get(nameType);

            if (type == null) {
                throw new Exception("Unsupported nameType: " + nameType);
            }

            // create tag with viafid if result is one of the supported types
            result = String.format("<%s ref=\"%s\" type=\"%s\">%s</%s>", tag,
                resource.getUri(), type, name, tag);
        }

        return result;
  }

}