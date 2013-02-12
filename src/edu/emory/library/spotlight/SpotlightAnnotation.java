/**
 * file src/edu/emory/library/namedropper/spotlight/SpotlightAnnotation.java
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

import java.util.logging.Logger;
import java.util.List;

import org.json.simple.JSONObject;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.model.Value;

import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;

public class SpotlightAnnotation {

    private String uri;
    private String surfaceForm;
    private String originalSurfaceForm;
    private String types;  // TODO: convert to a list internally?
    private Integer support;
    private Integer offset;
    private double similarityScore;
    private double percentageOfSecondRank;

    private final static Logger LOGGER = Logger.getLogger(SpotlightAnnotation.class.getName());

    public SpotlightAnnotation(JSONObject annotation) {
        this.uri = (String) annotation.get("@URI");
        this.surfaceForm = (String) annotation.get("@surfaceForm");
        this.types = (String) annotation.get("@types");
        this.support = Integer.parseInt((String) annotation.get("@support"));
        this.offset = Integer.parseInt((String) annotation.get("@offset"));
        this.similarityScore = Double.parseDouble((String) annotation.get("@similarityScore"));
        this.percentageOfSecondRank = Double.parseDouble((String) annotation.get("@percentageOfSecondRank"));
    }

    public String getUri() {
        return this.uri;
    }

    private String dbpedia_base_uri = "http://dbpedia.org/resource/";

    public String getId() {
        // treat the unique resource string after dbpedia url as a dbpedia/wikipedia id
        String uri = this.getUri();
        return uri.substring(dbpedia_base_uri.length(), uri.length());
    }


    public String getSurfaceForm() {
        return this.surfaceForm;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public String toString() {
        return this.surfaceForm;
    }

    public void adjustOffset(int relative) {
        this.offset += relative;
    }

    /**
     * Because dbpedia spotlight normalizes whitespace, the
     * surface form returned by spotlight may differ from that
     * in the original document.  This property allows
     * other methods to store and retrieve a calculated original
     * surface form.
     */
    public String getOriginalSurfaceForm() {
        return this.originalSurfaceForm;
    }

    public void setOriginalSurfaceForm(String orig) {
        this.originalSurfaceForm = orig;
    }


    /**
     * Determine what type of name this resource is.
     */
    String _type = null;
    public String getType() {
        if (_type != null) { return _type; }
        if (this.types.contains("DBpedia:Person") || this.types.contains("Freebase:/people/person")) {
            _type = "Personal";
        } else if (this.types.contains("DBpedia:Organisation")) {
            _type = "Corporate";
        } else if (this.types.contains("DBpedia:Place")) {
            _type = "Geographic";
        }
        return _type;
    }

    private String _label = null;
    // query dbpedia for the label; store it on first query
    public String getLabel() {
        if (_label != null) { return _label; }
        _label = getDBpediaProperty("rdfs:label");
        return _label;
    }

    private String _abstract = null;
    // query dbpedia for the abstract; store it on first query
    public String getAbstract() {
        if (_abstract != null) { return _abstract; }
        _abstract = getDBpediaProperty("<http://dbpedia.org/ontology/abstract>");
        return _abstract;
    }

    private String _viafid = null;
    public String getViafId() {
        // viaf id look-up currently only supported for personal names
        if (this.getType() != "Personal") { return _viafid; }
        if (_viafid != null) { return _viafid; }
        // some dbpedia records have a viaf property in DBpedia; check for that first
        _viafid = getDBpediaProperty("<http://dbpedia.org/property/viaf>",
            null);  // null = disable language filter (numeric property, no language)

        // if viafid is not available as a dbpedia property, search viaf for a match
        if (_viafid == null || _viafid.length() == 0) {
            // in some cases, dbpedia label is empty; use surface form as fallback
            String label = this.getLabel();
            if (label.isEmpty()) {
                label = this.getSurfaceForm();
            }

            try {
                List<ViafResource> suggestions = ViafClient.suggest(label);
                String id = this.getId();
                // iterate through suggestions looking for a VIAF record with a sameAs rel
                // to the current dbpedia resource
                for (int i = 0; i < suggestions.size(); i++) {
                    ViafResource vres = suggestions.get(i);
                    if (vres.isSameAs(this.getUri())) {
                        _viafid = vres.getViafId();
                        // stop checking results as soon as we find a match
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.warning(String.format("Error looking up VIAF id for %s : %s",
                    label, e.getMessage()));
            }

        }
        return _viafid;
    }

    private String getDBpediaProperty(String property) {
        return getDBpediaProperty(property, "EN");
    }

    // get a dbpedia property for this resource
    // property should either be a <uri> or a ns:property in a namespace dbpedia
    // has pre-defined
    private String getDBpediaProperty(String property, String language) {
        String val = "";
        try {

            String endpointURL = "http://dbpedia.org/sparql";
            HTTPRepository dbpediaEndpoint = new HTTPRepository(endpointURL, "");
            dbpediaEndpoint.initialize();
            RepositoryConnection conn =  dbpediaEndpoint.getConnection();

            try {
                // generate the sparql query for the requested property
                String queryString =
                    "SELECT ?val " +
                    "\n WHERE { <%s> %s ?val";
                // only use language filter if language is not null
                // (some properties, such as viaf, are numeric and do not have a language)
                if (language != null) {
                    queryString += "\n FILTER langMatches( lang(?val), \"%s\" ) }";
                    queryString = String.format(queryString, this.uri, property, language);
                } else {
                    queryString += "}";
                    queryString = String.format(queryString, this.uri, property);
                }

                TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                TupleQueryResult result = tupleQuery.evaluate();
                try {
                    if (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        Value l = bindingSet.getValue("val");
                        val = l.stringValue();
                    }

                }  finally {
                    result.close();
                }

           } finally {
              conn.close();
           }
        } catch (OpenRDFException e) {
            LOGGER.warning(String.format("Error querying DBpedia for %s : %s",
                property, e.getMessage()));
        }
        return val;
    }


}
