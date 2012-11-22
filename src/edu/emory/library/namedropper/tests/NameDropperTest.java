/**
 * file src/edu/emory/library/namedropper/tests/NameDropperTest.java
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

import java.util.List;
import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.io.File;
import javax.swing.JOptionPane;

import ro.sync.exml.plugin.selection.SelectionPluginContext;
import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.contentcompletion.xml.CIElement;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.NameDropperPluginExtension;
import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;

public class NameDropperTest {
    // Mock objects
    NameDropperPluginExtension mockND;
    SelectionPluginContext mockContext;

    @Before
    public void setUp() {
        // initialize mocks
        this.mockND = mock(NameDropperPluginExtension.class);
        this.mockContext = mock(SelectionPluginContext.class);
        this.mockND.viaf = mock(ViafClient.class);
    }

    @After
    public void tearDown() {
        this.mockND = null;
        this.mockContext = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

     @Test
     public void testQueryViafNoDocType() throws Exception {

        exception.expect(Exception.class);
        exception.expectMessage("No DocType selected");

        String searchTerm = "Smth";
        // FIXME: use actual class instead of mock here?
        when(this.mockND.queryVIAF(searchTerm)).thenCallRealMethod();
        this.mockND.queryVIAF(searchTerm);
     }

     @Test
     public void testQueryViafNoResults() throws Exception {

        exception.expect(Exception.class);
        exception.expectMessage("No Results");

        String term = "Smth";
        String docType = "EAD";
        this.mockND.docType = DocumentType.EAD;
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        when(this.mockND.queryVIAF(term)).thenCallRealMethod();
        when(this.mockND.viaf.suggest(term)).thenReturn(suggestions);

        this.mockND.queryVIAF(term);
     }

    @Test
    public void testQueryViaf() throws Exception {

        String result;
        String docType = "EAD";
        this.mockND.docType = DocumentType.EAD;
        String searchTerm = "Smith";

        // mock suggestions to be returned by mock ViafClient
        ViafResource mockvr = mock(ViafResource.class);
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        suggestions.add(mockvr);
        when(this.mockND.viaf.suggest(searchTerm)).thenReturn(suggestions);

        // simulate user clicking cancel
        when(this.mockND.getUserSelection(suggestions)).thenReturn(null);
        result = this.mockND.queryVIAF(searchTerm);
        assertEquals(null, result);

        // user selects first (only) option
        when(this.mockND.queryVIAF(searchTerm)).thenCallRealMethod();
        when(this.mockND.getUserSelection(suggestions)).thenReturn(mockvr);

        // makeTag functionality is tested separately.
        // if possible, could verify it was called with the correct arguments...
    }

     @Test
     public void testTagAllowed() throws Exception {

        int wsId = StandalonePluginWorkspace.MAIN_EDITING_AREA;
        StandalonePluginWorkspace mockWS = mock(StandalonePluginWorkspace.class);

        this.mockND.docType = DocumentType.EAD;

        // simulate editor unavailable - should be null
        when(this.mockContext.getPluginWorkspace()).thenReturn(mockWS);
        when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(null);
        when(this.mockND.tagAllowed(this.mockContext)).thenCallRealMethod();
        assertEquals(null, this.mockND.tagAllowed(this.mockContext));

        // simulate editor but no page available - should be null
        WSEditor mockEd = mock(WSEditor.class);
        when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(mockEd);
        when(mockEd.getCurrentPage()).thenReturn(null);
        assertEquals(null, this.mockND.tagAllowed(this.mockContext));

        // simulate full schema access, no elements allowed; should be false
        WSXMLTextEditorPage mockPage = mock(WSXMLTextEditorPage.class);
        WSTextXMLSchemaManager mockSchema = mock(WSTextXMLSchemaManager.class);
        when(mockEd.getCurrentPage()).thenReturn(mockPage);
        when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
        int offset = 1;
        when(mockPage.getSelectionStart()).thenReturn(offset);
        // getSelectionStart could throw a javax.swing.text.BadLocationException
        WhatElementsCanGoHereContext mockContext;
        mockContext = mock(WhatElementsCanGoHereContext.class);
        when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
        java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
        when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
        assertEquals(false, this.mockND.tagAllowed(this.mockContext));

        // schema access and tag matches an allowed element; should be true
        CIElement el = mock(CIElement.class);
        when(el.getName()).thenReturn("name");
        elements.add(el);
        assertEquals(true, this.mockND.tagAllowed(this.mockContext));

     }


}
