/**
 * file oxygen/src/edu/emory/library/namedropper/viaf/ViafClient.java
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

package edu.emory.library.viaf;

import java.util.List;
import java.util.ArrayList;

// http requests
import java.net.URL;
import java.net.HttpURLConnection;
import java.lang.StringBuffer;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

//JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import edu.emory.library.viaf.ViafResource;

public class ViafClient {

    public static String baseUrl = "http://viaf.org/viaf";

    public List<ViafResource> suggest(String term) throws Exception {
        String uri = String.format("%s/AutoSuggest?query=%s", this.baseUrl,
            URLEncoder.encode(term, "UTF-8"));

        // Do the acual query
        URL urlObj = new URL(uri);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) urlObj.openConnection();
        connection.setDoOutput(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line+"\n");
        }
        br.close();
        String result = "";
        result = sb.toString();

        List<ViafResource> resources = new ArrayList<ViafResource>();

        try {
            // parse the JSON and initialize a list of ViafResource objects
            // viaf autosuggest returns  in json format, with a list of results
            JSONObject json = (JSONObject)new JSONParser().parse(result);
            JSONArray jsonArray = (JSONArray)json.get("result");

            for (int i=0; i < jsonArray.size(); i++) {
                JSONObject obj = (JSONObject) jsonArray.get(i);

                // initialize a ViafResource for each result, using the viaf id and term
                // results may also include the following identifiers:
                //   lc, dnb, bnf, bne, nkc, nlilat, nla
                ViafResource vr = new ViafResource((String)obj.get("viafid"), (String)obj.get("term"));
                System.out.println(vr.toString());
                resources.add(vr);

            }

        } catch (Exception e) {
            // json parsing error - should just result in any empty resource list
            // TODO: log the error ?
        }
//        System.out.println("generated list with " + resources.size() + " length");

        return resources;
    }


}