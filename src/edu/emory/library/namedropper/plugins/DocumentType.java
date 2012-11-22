/**
 * file src/edu/emory/library/namedropper/plugins/NameDropperMenuPlugin.java
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

package edu.emory.library.namedropper.plugins;

import edu.emory.library.viaf.ViafResource;

/**
 * Information about supported document types.
 */
public enum DocumentType {

    EAD ("persname", "corpname", "geogname"),
    TEI ("person", "org", "place");


    private final String person;
    private final String org;
    private final String place;
    DocumentType(String person, String org, String place) {
        this.person = person;
        this.org = org;
        this.place = place;
    }

    public enum NameType {
        PERSONAL, CORPORATE, GEOGRAPHIC;
    }

    public enum EadTag {
        PERSNAME, CORPNAME, GEOGNAME;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
       }
    };

    public enum TeiType {
        PERSON, ORG, PLACE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    };

    /*
     * Determine what XML tag name to use for the current document type.
     *
     * @return String
     */
    public String getTagName() {
        return this.getTagName(null);
    }

    /*
     * Determine what XML tag name to use for the current document type.
     *
     * @param NameType
     * @return String
     */
    public String getTagName(NameType name) {
        String tag = null;
        switch (this) {

            case TEI:
                tag = "name";
                break;

            case EAD:

                if (name != null) {
                    switch (name) {
                        case PERSONAL:
                            tag = EadTag.PERSNAME.toString();
                            break;
                        case GEOGRAPHIC:
                            tag = EadTag.GEOGNAME.toString();
                            break;
                        case CORPORATE:
                            tag = EadTag.CORPNAME.toString();
                            break;
                    }
                } else {
                    // use generic name tag for ead when entity type is unknown
                    tag = "name";
                }
        }

        return tag;
    }

    public String getTagType(NameType name) {
        String type = null;
        switch (this) {
            case TEI:
                switch (name) {
                    case PERSONAL:
                        type = TeiType.PERSON.toString();
                        break;
                    case GEOGRAPHIC:
                        type = TeiType.PLACE.toString();
                        break;
                    case CORPORATE:
                        type = TeiType.ORG.toString();
                        break;
                }

            // no type needed for EAD
        }
        return type;
    }

    /**
     * Generate an xml tag for the current document type, based on a name, a resource. Uses
     * ViafResource type and viafid or URI to generate the appropriate tag and attributes.
     *
     * @param String name text of the name, which will be used as the content of the tag
     * @param ViafResource resource
     *
     * @return String of the generated tag or null
     * @raises Exception if a resource has an unsupported name type
     */
    public String makeTag(String name, ViafResource resource) throws Exception {

        String result = null;
        String tag = null;
        String type = null;

        String nameType = resource.getType(); // TODO: could getType use NameType enum instead of string?
        DocumentType.NameType nt;
        // FIXME: must be a better way to do this
        if (nameType.equals("Personal")) {
            nt = DocumentType.NameType.PERSONAL;
        } else if (nameType.equals("Corporate")) {
            nt = DocumentType.NameType.CORPORATE;
        } else if (nameType.equals("Geographic")) {
            nt = DocumentType.NameType.GEOGRAPHIC;
        } else {
            throw new Exception("Unsupported nameType: " + nameType);
        }


        switch (this) {
            case TEI:
                tag = this.getTagName(nt);
                type = this.getTagType(nt);
                // create tag with viafid if result is one of the supported types
                result = String.format("<%s ref=\"%s\" type=\"%s\">%s</%s>", tag,
                    resource.getUri(), type, name, tag);

                break;

            case EAD:
                tag = this.getTagName(nt);
                result = String.format("<%s source=\"viaf\" authfilenumber=\"%s\">%s</%s>", tag,
                    resource.getViafId(), name, tag);
                break;
        }

        return result;
  }

}