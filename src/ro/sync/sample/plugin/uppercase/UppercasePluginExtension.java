package ro.sync.sample.plugin.uppercase;


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
import java.util.ArrayList;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;


public class UppercasePluginExtension implements SelectionPluginExtension {
    /**
    * Convert the text to uppercase.
    *
    *@param  context  Selection context.
    *@return          Uppercase plugin result.
    */
    public SelectionPluginResult process(SelectionPluginContext context) {
        
        String orig = context.getSelection();
        String result = this.queryVIAF(orig);
        
        return new SelectionPluginResultImpl(result);
    }
    
    
    /**
    * Convert the text to uppercase.
    *
    *@param  context  Selection context.
    *@return          Uppercase plugin result.
    */
    public String queryVIAF(String param){
        String result = param; //if something goes boom it will put back the original text
        
        try{
            StringBuffer url_buf = new StringBuffer();
            url_buf.append("http://viaf.org/viaf/AutoSuggest?");
            url_buf.append("query=");
            url_buf.append(URLEncoder.encode(param, "UTF-8"));
            
            URL url = new URL(url_buf.toString());
            HttpURLConnection connection = null;
            connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            
            JSONObject json = (JSONObject)new JSONParser().parse(sb.toString());
            JSONArray json_array = (JSONArray)json.get("result");
            JSONObject obj = (JSONObject) json_array.get(0);
            String viafid = (String)obj.get("viafid");
//            
            result = "<persname source=\"viaf\" authfilenumber=\"" + viafid + "\">" + param + "</persname>";
        }catch(Exception e){
            result = e.getMessage();
        }               
        
        return result;
    
    
    }
}
