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

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.viaf.ViafResource;

public class DocumentTypeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

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

        DocumentType tei = DocumentType.TEI;
        DocumentType ead = DocumentType.EAD;

        // person result
        when(mockvr.getType()).thenReturn("Personal");
        assertEquals("<persname source=\"viaf\" authfilenumber=\"12345\">someName</persname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"person\">someName</name>",
            tei.makeTag("someName", mockvr));

        // corporate
        when(mockvr.getType()).thenReturn("Corporate");
        assertEquals("<corpname source=\"viaf\" authfilenumber=\"12345\">someName</corpname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"org\">someName</name>",
            tei.makeTag("someName", mockvr));

        // place
        when(mockvr.getType()).thenReturn("Geographic");
        assertEquals("<geogname source=\"viaf\" authfilenumber=\"12345\">someName</geogname>",
            ead.makeTag("someName", mockvr));
        assertEquals("<name ref=\"http://viaf.org/viaf/12345\" type=\"place\">someName</name>",
            tei.makeTag("someName", mockvr));

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

}
