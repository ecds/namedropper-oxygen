/**
 * file src/edu/emory/library/namedropper/plugins/DocumentType.java
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
import edu.emory.library.spotlight.SpotlightAnnotation;

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

    /**
     * Types of names that are supported.
     */
    public enum NameType {
        PERSONAL, CORPORATE, GEOGRAPHIC;

        public static NameType fromString(String s) {
            if (s == null) { return null; }

            for (NameType n : NameType.values()) {
                if (s.equalsIgnoreCase(n.toString())) {
                    return n;
                }
            }
            return null;  // return null if no match was found
        }
    };

    /**
     * Details for tags in EAD document type.
     */
    public enum EadTag {
        PERSNAME, CORPNAME, GEOGNAME;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

       /**
        * Static method to initialize an EadTag instance based on
        * a NameType.
        */
        public static EadTag fromNameType(NameType name) {
            if (name == null) { return null; }

            switch (name) {
                case PERSONAL:
                    return EadTag.PERSNAME;
                case GEOGRAPHIC:
                    return EadTag.GEOGNAME;
                case CORPORATE:
                    return EadTag.CORPNAME;
                default:
                    return null;
            }
        }
    };

    /**
     * Details for type in TEI document type.
     */
    public enum TeiType {
        PERSON, ORG, PLACE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        /**
         * Static method to initialize a TeiType instance based on
         * a NameType.
         */
        public static TeiType fromNameType(NameType name) {
            if (name == null) { return null; }

            switch (name) {
                case PERSONAL:
                    return TeiType.PERSON;
                case GEOGRAPHIC:
                    return TeiType.PLACE;
                case CORPORATE:
                    return TeiType.ORG;
                default:
                    return null;
            }
        }
    };

    /**
     * Static method to initialize a DocumentType instance from a string.
     * Case insensitive.
     */
    public static DocumentType fromString(String value) {
        if (value == null) { return null; }

        for (DocumentType d : DocumentType.values()) {
            if (value.equalsIgnoreCase(d.toString())) {
              return d;
            }
        }
        return null;  // no match found
    }

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

                EadTag eadtag = EadTag.fromNameType(name);
                if (eadtag != null) {
                    tag = eadtag.toString();
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
                type = TeiType.fromNameType(name).toString();
                break;

            // no type needed for EAD
        }
        return type;
    }

    /**
     * Generate an xml tag for the current document type, based on a name, a resource. Uses
     * ViafResource type and viafid or URI to generate the appropriate tag and attributes.
     * Raises an exception when resource has an unsupported name type.
     *
     * @param name       text of the name to used as the content of the tag
     * @param resource  ViafResource, for tag attributes
     *
     * @return String of the generated tag or null
     */
    public String makeTag(String name, ViafResource resource) throws Exception {

        String result = null;
        String tag = null;
        String type = null;

        // TODO: should NameType be somewhere common so
        // ViafResource.getType could use NameType enum instead of string?
        String nameType = resource.getType();
        DocumentType.NameType nt = NameType.fromString(nameType);
        if (nt == null) {
            throw new Exception("Unsupported nameType: " + nameType);
        }
        return this.makeTag(name, nt, resource.getUri(), "viaf", resource.getViafId());
    }

    /**
     * Generate an xml tag for the current document type, based on a resource identified
     * by DBpedia Spotlight annotation.
     *
     * @param annotation DBpedia Spotlight annotation result
     *
     * @return String of the generated tag or null
     */
    public String makeTag(SpotlightAnnotation annotation) throws Exception {

        String result = null;
        String tag = null;
        String type = null;

        String nameType = annotation.getType();
        DocumentType.NameType nt = NameType.fromString(nameType);
        if (nt == null) {
            throw new Exception("Unsupported nameType: " + nameType);
        }

        // use dbpedia URI as default identifier
        String uri = annotation.getUri();
        String id = annotation.getUri();
        String source = "dbpedia";
        // get VIAF id if possible (currently only supported for personal names)
        if (nt == DocumentType.NameType.PERSONAL) {
            String viafid = annotation.getViafId();
            if (viafid != null) {
                id = viafid;
                source = "viaf";
                // When VIAF id is available, use VIAF URI also
                uri = String.format("http://viaf.org/viaf/%d", viafid);
            }

        }
        return this.makeTag(annotation.getSurfaceForm(), nt, annotation.getUri(),
            source, id);
    }

    /**
     * Generate an xml tag for the current document type, based on a name, a resource. Uses
     * ViafResource type and viafid or URI to generate the appropriate tag and attributes.
     *
     * @param text  text to be used as the content of the tag
     * @param nt  type of name to generate a tag for
     * @param uri   Resource URI (used to generate TEI tags)
     * @param idSource Source name for identifier (used for EAD source attribute)
     * @param id    Identifier (used for EAD authfilenumber attribute with idSource)
     *
     * @return String of the generated tag or null
     */
    public String makeTag(String text, NameType nt, String uri, String idSource, String id)  {

        String result = null;
        String tag = null;
        String type = null;

        switch (this) {
            case TEI:
                tag = this.getTagName(nt);
                type = this.getTagType(nt);
                // create tag with viafid if result is one of the supported types
                result = String.format("<%s ref=\"%s\" type=\"%s\">%s</%s>", tag,
                    uri, type, text, tag);

                break;

            case EAD:
                tag = this.getTagName(nt);
                result = String.format("<%s source=\"%s\" authfilenumber=\"%s\">%s</%s>", tag,
                    idSource, id, text, tag);
                break;
        }

        return result;
  }



}