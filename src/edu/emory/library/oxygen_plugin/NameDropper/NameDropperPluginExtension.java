/**
 * file oxygen/src/edu/emory/library/oxygen_plugin/NameDropper/NameDropperPluginExtension.java
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

package edu.emory.library.oxygen_plugin.NameDropper;


//Oxygen stuff
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

// Http Requests
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.ArrayList;


// Pop Up Message when errors
import javax.swing.JOptionPane;

//JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

//XML Parsing
import nu.xom.Builder;
import nu.xom.Document;


//Used when getting full stack trace
import java.io.PrintWriter;
import java.io.StringWriter;


import edu.emory.library.oxygen_plugin.NameDropper.ResultChoice;


public class NameDropperPluginExtension implements SelectionPluginExtension {  

    public static String eadLabel;
    public static String teiLabel;
    
    // used to determine ead tag
    public static HashMap eadTag = new HashMap();
    
    // used to determine tei type attribute
    public static HashMap teiType = new HashMap();
    

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
            
            if (this.tagAllowed(docType, context) == false) {
                // if the tag is not allowed, throw an exception to be displayed
                // as a warning message to the user
                throw new Exception("Tag is not allowed in the current context");
            }
            
            result = this.queryVIAF(orig, docType);
            
            if(result == null) {result = orig;}
        } catch(Exception e) {
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
        String result = name;  //This is retutned if no resulsts are found
        String queryResult = "";
        Builder builder = new Builder();
        Document doc = null;
        String viafid = null;

        // url query paramters
        HashMap  params = new HashMap();

        // if document type is not set, don't bother to query VIAF
        // since we won't be able to add a tag
        if (docType == null || docType.equals("")) {
            throw new Exception("No DocType selected");
        }
        
        try {
            
            params.put("query", name);

            // get the result of the query
            queryResult = this.query("http://viaf.org/viaf/AutoSuggest", params);

            
            // get choices from JSON
            Object[] choices = this.makeChoices(queryResult);
            
            // display pop-up box 
            ResultChoice selectedChoice = (ResultChoice) JOptionPane.showInputDialog(null, 
                     "Names", "Search Results", 
                     JOptionPane.PLAIN_MESSAGE, null, 
                     choices, choices[0]);
             
            // return null if cancel button is clicked 
            if(selectedChoice == null) {
                 return null;
             }
             else {
                 viafid = selectedChoice.getViafid();
                 
                 // Query and parse xml based on viafid 
                 String viafInfo = query(String.format("http://viaf.org/viaf/%s/viaf.xml", viafid), new HashMap());
                 doc = builder.build(viafInfo, null); // Build doc from retrun string
                 String nameType = doc.getRootElement().getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").getValue();
                 
                 result = this.makeTag(viafid, name, nameType, docType);
             }
        } catch(Exception e) {
            throw e; //Throw up
        }
        return result;
    }

    /**
    * performs a query against the provided url using the provided query parms.
    *
    * @param  url  Base url to query.
    * @param  params   key value pairs of query parameters.
    * @return          Results of query.
    */
    public String query(String url, HashMap params) throws Exception {
        String result = "";
        StringBuffer urlBuf = new StringBuffer();

        try {
            result = (String)params.get("query");  //Used if there are no results
            urlBuf = new StringBuffer();
            urlBuf.append(url);

            //Build query string
            for (int i=0; i < params.size(); i++) {
                String key = (String)params.keySet().toArray()[i];
                String val = (String)params.values().toArray()[i];

                //put the ?  if params exits
                if(i==0 && !url.endsWith("?")){
                    urlBuf.append("?");
                }

                //Append the key, value pairs
                urlBuf.append(key + "=" + URLEncoder.encode(val, "UTF-8"));

                if(i != params.size());{
                    urlBuf.append("&");
                }
            }

            // Do the acual query
            URL urlObj = new URL(urlBuf.toString());
            HttpURLConnection connection = null;
            connection = (HttpURLConnection)urlObj.openConnection();
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            result = sb.toString();


        } catch(Exception e){
            throw e; //Throw up
        }
         return result;
    }
    
    public Object[] makeChoices(String jsonStr) throws Exception {
        
        try{
            // parse the JSON and return resut in the correct format
                JSONObject json = (JSONObject)new JSONParser().parse(jsonStr);
                JSONArray jsonArray = (JSONArray)json.get("result");
            
                //No results from query
                if (jsonArray == null){
                    throw new Exception("No Results");
                }
            
                // get First 15 choices
                ArrayList choicesList = new ArrayList();
            
                for(int i=0; i < jsonArray.size() && i < 15; i++){
                    JSONObject obj = (JSONObject) jsonArray.get(i);
                    ResultChoice choice = new ResultChoice((String)obj.get("viafid"), (String)obj.get("term"));
                    choicesList.add(choice);
                }
            
                return choicesList.toArray();
        } catch (Exception e) {
           throw e; 
        }
        
    }
    
    
    public String makeTag(String viafid, String name, String nameType, String docType) throws Exception {
        String result = null;;
        
        try{
            String tag = null;
            String type = null;

            
            // docType must be set   
            if(docType.equals(this.eadLabel)){
                tag = this.getTagName(docType);
                if (tag == null) {throw new Exception("Unsupported nameType: " + nameType);}
                
                result = String.format("<%s source=\"viaf\" authfilenumber=\"%s\">%s</%s>", tag, viafid, name, tag);
            }

            else if (docType.equals(this.teiLabel)){
                tag = this.getTagName(docType);
                type = (String) this.teiType.get(nameType);
                
                if (type == null) {throw new Exception("Unsupported nameType: " + nameType);}
                
                //create tag with viafid if result is one of the suppoeted types
                  result = String.format("<%s ref=\"http://viaf.org/viaf/%s\" type=\"%s\">%s</%s>", tag, viafid, type, name, tag);

            }    
        } catch (Exception e) {
            throw e;
    }
   
        return result;
  }
    
}