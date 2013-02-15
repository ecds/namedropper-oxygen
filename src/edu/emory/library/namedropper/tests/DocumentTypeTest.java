/**
 * file src/edu/emory/library/namedropper/plugins/tests/DocumentTypeTest.java
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


package edu.emory.library.namedropper.tests;


import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import org.geonames.Toponym;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.viaf.ViafResource;
import edu.emory.library.spotlight.SpotlightAnnotation;

public class DocumentTypeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testFromString() {
        assertEquals(DocumentType.TEI, DocumentType.fromString("TEI"));
        assertEquals(DocumentType.TEI, DocumentType.fromString("tei"));
        assertEquals(DocumentType.EAD, DocumentType.fromString("EAD"));
        assertEquals(DocumentType.EAD, DocumentType.fromString("ead"));
        assertNull(DocumentType.fromString("unknown"));
    }

    @Test
    public void testNameType() {
        assertEquals(DocumentType.NameType.PERSONAL, DocumentType.NameType.fromString("Personal"));
        assertEquals(DocumentType.NameType.CORPORATE, DocumentType.NameType.fromString("Corporate"));
        assertEquals(DocumentType.NameType.GEOGRAPHIC, DocumentType.NameType.fromString("Geographic"));
        assertNull(DocumentType.NameType.fromString("unknown type"));
    }


    @Test
    public void testEadTagFromNameType() {
        assertEquals(DocumentType.EadTag.PERSNAME, DocumentType.EadTag.fromNameType(DocumentType.NameType.PERSONAL));
        assertEquals(DocumentType.EadTag.CORPNAME, DocumentType.EadTag.fromNameType(DocumentType.NameType.CORPORATE));
        assertEquals(DocumentType.EadTag.GEOGNAME, DocumentType.EadTag.fromNameType(DocumentType.NameType.GEOGRAPHIC));
        assertNull(DocumentType.EadTag.fromNameType(null));
    }

    @Test
    public void testTeiTypeFromNameType() {
        assertEquals(DocumentType.TeiType.PERSON, DocumentType.TeiType.fromNameType(DocumentType.NameType.PERSONAL));
        assertEquals(DocumentType.TeiType.ORG, DocumentType.TeiType.fromNameType(DocumentType.NameType.CORPORATE));
        assertEquals(DocumentType.TeiType.PLACE, DocumentType.TeiType.fromNameType(DocumentType.NameType.GEOGRAPHIC));
        assertNull(DocumentType.TeiType.fromNameType(null));
    }


    @Test
    public void testTagName() {
        DocumentType tei = DocumentType.TEI;

        assertEquals("name", tei.getTagName());

        DocumentType ead = DocumentType.EAD;
        assertEquals("name", ead.getTagName());
        assertEquals("persname", ead.getTagName(DocumentType.NameType.PERSONAL));
        assertEquals("corpname", ead.getTagName(DocumentType.NameType.CORPORATE));
        assertEquals("geogname", ead.getTagName(DocumentType.NameType.GEOGRAPHIC));

    }

    @Test
    public void testTagType() {
        DocumentType tei = DocumentType.TEI;
        assertEquals("person", tei.getTagType(DocumentType.NameType.PERSONAL));
        assertEquals("org", tei.getTagType(DocumentType.NameType.CORPORATE));
        assertEquals("place", tei.getTagType(DocumentType.NameType.GEOGRAPHIC));

        DocumentType ead = DocumentType.EAD;
        assertNull(ead.getTagType(DocumentType.NameType.PERSONAL));;

    }

    @Test
    public void testMakeTag() throws Exception {

        // build a mock ViafResource to test with
        ViafResource mockvr = mock(ViafResource.class);
        when(mockvr.getViafId()).thenReturn("12345");
        when(mockvr.getUri()).thenReturn("http://viaf.org/viaf/12345");

        // mock spotlight annotation to test with
        SpotlightAnnotation mockAnnotation = mock(SpotlightAnnotation.class);
        when(mockAnnotation.getUri()).thenReturn("http://dbpedia.org/Some_Person");
        when(mockAnnotation.getId()).thenReturn("Some_Person");
        when(mockAnnotation.getSurfaceForm()).thenReturn("someName");

        DocumentType tei = DocumentType.TEI;
        DocumentType ead = DocumentType.EAD;

        // person result
        // - viaf
        when(mockvr.getType()).thenReturn("Personal");
        assertEquals("<persname source=\"viaf\" authfilenumber=\"12345\">someName</persname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"person\">someName</name>",
            tei.makeTag("someName", mockvr));
        // - spotlight annotation with no viaf id
        when(mockAnnotation.getType()).thenReturn("Personal");
        when(mockAnnotation.getViafId()).thenReturn("");
        assertEquals("<persname source=\"dbpedia\" authfilenumber=\"Some_Person\">someName</persname>",
            ead.makeTag(mockAnnotation));
        assertEquals("<name ref=\"http://dbpedia.org/Some_Person\" type=\"person\">someName</name>",
            tei.makeTag(mockAnnotation));
        // - spotlight annotation with viaf id
        when(mockAnnotation.getViafId()).thenReturn("12345");
        assertEquals("<persname source=\"viaf\" authfilenumber=\"12345\">someName</persname>",
            ead.makeTag(mockAnnotation));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"person\">someName</name>",
            tei.makeTag(mockAnnotation));

        // corporate
        // - viaf
        when(mockvr.getType()).thenReturn("Corporate");
        assertEquals("<corpname source=\"viaf\" authfilenumber=\"12345\">someName</corpname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"org\">someName</name>",
            tei.makeTag("someName", mockvr));
        // - spotlight annotation
        when(mockAnnotation.getType()).thenReturn("Corporate");
        assertEquals("<corpname source=\"dbpedia\" authfilenumber=\"Some_Person\">someName</corpname>",
            ead.makeTag(mockAnnotation));
        assertEquals("<name ref=\"http://dbpedia.org/Some_Person\" type=\"org\">someName</name>",
            tei.makeTag(mockAnnotation));

        // place
        when(mockvr.getType()).thenReturn("Geographic");
        assertEquals("<geogname source=\"viaf\" authfilenumber=\"12345\">someName</geogname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"place\">someName</name>",
            tei.makeTag("someName", mockvr));
        // - spotlight annotation
        when(mockAnnotation.getType()).thenReturn("Geographic");
        assertEquals("<geogname source=\"dbpedia\" authfilenumber=\"Some_Person\">someName</geogname>",
            ead.makeTag(mockAnnotation));
        assertEquals("<name ref=\"http://dbpedia.org/Some_Person\" type=\"place\">someName</name>",
            tei.makeTag(mockAnnotation));
     }

     @Test
     public void testMakeTagNoResults() throws Exception {
        exception.expect(Exception.class);
        exception.expectMessage("Unsupported nameType: BadNameType");
        ViafResource mockvr = mock(ViafResource.class);
        when(mockvr.getType()).thenReturn("BadNameType");

        DocumentType tei = DocumentType.TEI;
        tei.makeTag("someName", mockvr);
     }

     @Test
     public void testMakeTagToponym() throws Exception {
        DocumentType tei = DocumentType.TEI;

        String name = "Belfast";
        Toponym place = new Toponym();
        place.setGeoNameId(3333223);
        String result = tei.makeTag(name, place);
        assertEquals("<name ref=\"http://sws.geonames.org/3333223/\" type=\"place\">Belfast</name>",
            result);
        // really we just care that makeTag is called with the proper args,
        // but adding mocking/spying to verify args doesn't seem worth the trouble

     }

}
