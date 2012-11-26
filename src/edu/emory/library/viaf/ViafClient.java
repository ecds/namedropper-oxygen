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
import java.net.URLEncoder;

// JSON Parsing
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;

import edu.emory.library.utils.EULHttpUtils;
import edu.emory.library.viaf.ViafResource;

/**
 * Client object for accessing VIAF (Virtual International Authority File,
 * http://viaf.org ) webservices.
 *
 * For more information about VIAF APIs, see
 * http://www.oclc.org/developer/documentation/virtual-international-authority-file-viaf/using-api
 *
 */
public class ViafClient {

    public static String baseUrl = "http://viaf.org/viaf";

    /**
     * Query the VIAF AutoSuggest API to get suggestions matches for
     * a user-specified search term.  Returns an empty list if
     * no matches were found or if there was an error either making
     * the request or parsing the response.
     *
     * @param String search term
     * @return list of ViafResource
     */
    public static List<ViafResource> suggest(String term) throws Exception {

        String uri = String.format("%s/AutoSuggest?query=%s", baseUrl,
            URLEncoder.encode(term, "UTF-8"));
        String result = EULHttpUtils.readUrlContents(uri);
        // todo: handle (at least log) http exceptions here

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
                resources.add(vr);
            }

        } catch (Exception e) {
            // json parsing error - should just result in any empty resource list
            // TODO: log the error ?
        }
        return resources;
    }

}