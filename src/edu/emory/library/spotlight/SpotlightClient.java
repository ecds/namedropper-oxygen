/**
 * file src/edu/emory/library/namedropper/spotlight/SpotlightClient.java
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

package edu.emory.library.spotlight;


import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

// http requests
import java.net.URLEncoder;

// JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;


import edu.emory.library.utils.EULHttpUtils;
import edu.emory.library.spotlight.SpotlightAnnotation;

/**
 * Client object for accessing DBpedia Spotlight webservice.
 *
 */
public class SpotlightClient {

    /**
     * Default base url for DBpedia Spotlight web service
     */
    public static String baseUrl = "http://spotlight.dbpedia.org/rest";

    private double confidence = 0.0;
    private int support = 0;

    // TODO: allow overriding base url, confidence, support, types
    // in constructor ?

    public List<SpotlightAnnotation> annotate(String txt) throws Exception {
        List<SpotlightAnnotation> annotations = new ArrayList<SpotlightAnnotation>();

        try {
            String uri = String.format("%s/annotate?text=%s&types=%s", this.baseUrl,
                URLEncoder.encode(txt, "UTF-8"),
                // restrict to supported types (TODO: don't hard-code here; configurable?)
                URLEncoder.encode("Person,Place,Organisation", "UTF-8")
                ); // java.io.UnsupportedEncodingException

            // could also add args for confidence, support
            HashMap headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");
            String response = EULHttpUtils.readUrlContents(uri, headers);

            // load the result as json
            JSONObject json = (JSONObject)new JSONParser().parse(response);
            // information about identified names are listed under 'Resources'
            JSONArray jsonArray = (JSONArray)json.get("Resources");
            for (int i=0; i < jsonArray.size(); i++) {
                SpotlightAnnotation sa = new SpotlightAnnotation((JSONObject) jsonArray.get(i));
                annotations.add(sa);
            }

        } catch (java.io.UnsupportedEncodingException e) {
            // TODO:  log error instead of just printing
            System.out.println("Error encoding text");
        } // TODO: also need to catch json decoding error

        return annotations;
    }

}