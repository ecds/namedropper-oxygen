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


// Http Requests
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;


// Pop Up Message when errors
import javax.swing.JOptionPane;

//JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

//XML Parsing
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;


//Used when getting full stack trace
import java.io.PrintWriter;
import java.io.StringWriter;



public class NameDropperPluginExtension implements SelectionPluginExtension {    
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
            result = orig; // put back original if something goes BOOM!
            
            result = this.queryVIAF(orig, docType);
        } catch(Exception e) {
            // This section is in case you want the whole stack trace in the error message
            // Pass sw.toString() instead of e.getMessage() in the showMessageDialog function
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);


            
            JOptionPane.showMessageDialog(context.getFrame(), e.getMessage(), "Warning",
                    JOptionPane.ERROR_MESSAGE);
        }

        return new SelectionPluginResultImpl(result);
    }

    /**
    * Query VIAF for name data
    *
    * @param  name  Name to query.
    * @param  docType  String - EAD or TEI.
    * @return          String containing persname xml tag data.
    */
    public String queryVIAF(String name, String docType) throws Exception{
        String result = name;  //This is retutned if no resulsts are found
        String queryResult = "";

        // url query paramters
        HashMap  params = new HashMap();
        Document doc = null;
        Element root = null;

        try {

            params.put("query", name);

            // get the result of the query
            queryResult = this.query("http://viaf.org/viaf/AutoSuggest", params);

            // parse the JSON and return resut in the correct format
            JSONObject json = (JSONObject)new JSONParser().parse(queryResult);
            JSONArray jsonArray = (JSONArray)json.get("result");
            
            //No results from query
            if (jsonArray == null){
                throw new Exception("No Results");
            }
            
            JSONObject obj = (JSONObject) jsonArray.get(0);
            String viafid = (String)obj.get("viafid");

            //Query by viafid and get name type
            Builder builder = new Builder();
            
            //This does the query and reads the XML for you
            String viafInfo = query(String.format("http://viaf.org/viaf/%s/viaf.xml", viafid), new HashMap());
            doc = builder.build(viafInfo, null);
            root = doc.getRootElement();
            String nameType = root.getFirstChildElement("nameType", "http://viaf.org/viaf/terms#").getValue();
            String tag = null;

            if(docType != null && docType.equals("EAD")){
                if (nameType.equals("Personal")) {tag = "persname";}
                else if (nameType.equals("Corporate")) {tag = "corpname";}
                else if (nameType.equals("Geographic")) {tag = "geogname";}
                else throw new Exception("Unsupported nameType: " + nameType);

                //create tag with viafid if result is one of the suppoeted types
                if (tag != null){
                  result = String.format("<%s source=\"viaf\" authfilenumber=\"%s\">%s</%s>", tag, viafid, name, tag);
                }
                else{
                    result = name;  //no resulsts or no supported nameTypes
                }
            }
            else if (docType != null && docType.equals("TEI")){
                tag="name";
                String type = null;
                
                if (nameType.equals("Personal")) {type = "person";}
                else if (nameType.equals("Corporate")) {type = "org";}
                else if (nameType.equals("Geographic")) {type = "place";}
                else throw new Exception("Unsupported nameType: " + nameType);
                
                //create tag with viafid if result is one of the suppoeted types
                if (type != null){
                  result = String.format("<%s ref=\"http://viaf.org/viaf/%s\" type=\"%s\">%s</%s>", tag, viafid, type, name, tag);
                }
                else{
                    result = name;  //no resulsts or no supported nameTypes
                }
            }
            else{
                throw new Exception("No DocType selected");
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
    
}
