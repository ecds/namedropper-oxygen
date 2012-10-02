package edu.emory.library.oxygen_plugin.NameDropper;


import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.exml.plugin.selection.SelectionPluginExtension;
import ro.sync.exml.plugin.selection.SelectionPluginResult;
import ro.sync.exml.plugin.selection.SelectionPluginResultImpl;


import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;


public class NameDropperPluginExtension implements SelectionPluginExtension {
    /**
    * Lookup name in name authority.
    *
    *@param  context  Selection context.
    *@return          NameDropper plugin result.
    */
    public SelectionPluginResult process(SelectionPluginContext context) {
        
        //query VIAF for name data
        String orig = context.getSelection();
        String result = this.queryVIAF(orig);
        
        return new SelectionPluginResultImpl(result);
    }
    
    
    /**
    * Query VIAF for name data
    *
    *@param  name  Name to query.
    *@return          String containing persname xml tag data.
    */
    public String queryVIAF(String name){
        String result = name;  //This is retutned if no resulsts are found

        // url query paramters
        HashMap  params = new HashMap();
        params.put("query", name);
        
        
        // get the result of the query
        String query_result = this.query("http://viaf.org/viaf/AutoSuggest", params);
        
        
        // parse the JSON and return resut in the correct format
        try{
            JSONObject json = (JSONObject)new JSONParser().parse(query_result);
            JSONArray json_array = (JSONArray)json.get("result");
            JSONObject obj = (JSONObject) json_array.get(0);
            String viafid = (String)obj.get("viafid");
//            
            result = String.format("<persname source=\"viaf\" authfilenumber=\"%s\">%s</persname>", viafid, name);
        }catch(Exception e){}               
        
        return result;
    
    
    }
    
    /**
    * performs a query against the provided url using the provided query parms.
    *
    *@param  ulr  Base url to query.
    *@param  params   key value pairs of query parameters.
    *@return          Results of query.
    */
    public String query(String url, HashMap params){
        String result = (String)params.get("query");  //Used if there are no results
        StringBuffer url_buf = new StringBuffer();
        url_buf.append(url);
        
       //Build query string
        for (int i=0; i < params.size(); i++) {
            String key = (String)params.keySet().toArray()[i];
            String val = (String)params.values().toArray()[i];
            
            
            //put the ?  if params exits
            if(i==0 && !url.endsWith("?")){
                url_buf.append("?");
            }
            
            
            //Append the key, value pairs
            try{
                url_buf.append(key + "=" + URLEncoder.encode(val, "UTF-8"));    
            }catch(Exception e){}
            
            
            if(i != params.size());{
                url_buf.append("&");
            }

        }
        
        // Do the acual query
        try{
            URL url_obj = new URL(url_buf.toString());
            HttpURLConnection connection = null;
            connection = (HttpURLConnection)url_obj.openConnection();
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            result = sb.toString();
            
        }catch(Exception e){}  //No action yet on Exception
        
        return result;
    }
}