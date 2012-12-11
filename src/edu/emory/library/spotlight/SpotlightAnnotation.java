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

}
