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

import org.json.simple.JSONObject;

import java.util.List;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.model.Value;

public class SpotlightAnnotation {

    private String uri;
    private String surfaceForm;
    private String types;  // TODO: convert to a list internally?
    private Integer support;
    private Integer offset;
    private double similarityScore;
    private double percentageOfSecondRank;

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
     * Determine what type of name this resource is.
     */
    public String getType() {
        String type = null;
        if (this.types.contains("DBpedia:Person") || this.types.contains("Freebase:/people/person")) {
            type = "Personal";
        } else if (this.types.contains("DBpedia:Organisation")) {
            type = "Corporate";
        } else if (this.types.contains("DBpedia:Place")) {
            type = "Geographic";
        }
        return type;
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
                String queryString =
                    "SELECT ?val " +
                    "\n WHERE { <%s> %s ?val" +
                    "\n FILTER langMatches( lang(?val), \"%s\" )}";
                queryString = String.format(queryString, this.uri, property, language);
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
           // TODO... handle exception
            System.out.println("exception " + e);
        }
        return val;
    }


}
