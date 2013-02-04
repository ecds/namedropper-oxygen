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

    private Double confidence = null;
    private Integer support = null;

    // TODO: allow overriding base url, confidence, support, types
    // in constructor ?

    public SpotlightClient() {}

    public SpotlightClient(double confidence, int support) {
        this.confidence = confidence;
        this.support = support;
    }

    public List<SpotlightAnnotation> annotate(String txt) throws Exception {
        List<SpotlightAnnotation> annotations = new ArrayList<SpotlightAnnotation>();

        try {
            String uri = String.format("%s/annotate", this.baseUrl);

            HashMap headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");
            // content-type required when using POST
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            HashMap params = new HashMap<String, String>();
            params.put("text", txt);
            // restrict to supported types (TODO: don't hard-code here; configurable?)
            params.put("types", "Person,Place,Organisation");
            // include confidence & support parameters if set
            if (this.confidence != null) {
                params.put("confidence", String.format("%s", this.confidence));
            }
            if (this.support != null) {
                params.put("support", String.format("%d", this.support));
            }

            // always use POST to support text larger than that allowed in
            // an HTTP GET request URI
            String response = EULHttpUtils.postUrlContents(uri, headers, params);

            // load the result as json
            JSONObject json = (JSONObject)new JSONParser().parse(response);
            // information about identified names are listed under 'Resources'
            JSONArray jsonArray = (JSONArray)json.get("Resources");
            // if no names are identified, resources will not be set
            if (jsonArray != null) {
                for (int i=0; i < jsonArray.size(); i++) {
                    SpotlightAnnotation sa = new SpotlightAnnotation((JSONObject) jsonArray.get(i));
                    annotations.add(sa);
                }
            }

        } catch (java.io.UnsupportedEncodingException e) {
            // TODO:  log error instead of just printing
            System.out.println("Error encoding text");
        } // TODO: also need to catch json decoding error

        return annotations;
    }

}