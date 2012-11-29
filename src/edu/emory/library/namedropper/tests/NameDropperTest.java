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
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.mockito.Mockito;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest(ViafClient.class)
public class NameDropperTest {
    // Mock objects
    NameDropperPluginExtension mockND;
    SelectionPluginContext mockContext;

    @Before
    public void setUp() {
        // initialize mocks
        this.mockND = Mockito.mock(NameDropperPluginExtension.class);
        this.mockContext = Mockito.mock(SelectionPluginContext.class);
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
        Mockito.when(this.mockND.queryVIAF(searchTerm)).thenCallRealMethod();
        this.mockND.queryVIAF(searchTerm);
     }

     @Test
     public void testQueryViafNoResults() throws Exception {
        PowerMockito.mockStatic(ViafClient.class);

        exception.expect(Exception.class);
        exception.expectMessage("No Results");

        String term = "Smth";
        String docType = "EAD";
        this.mockND.docType = DocumentType.EAD;
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        Mockito.when(this.mockND.queryVIAF(term)).thenCallRealMethod();
        Mockito.when(ViafClient.suggest(term)).thenReturn(suggestions);

        this.mockND.queryVIAF(term);
     }

    @Test
    public void testQueryViaf() throws Exception {
        PowerMockito.mockStatic(ViafClient.class);

        String result;
        String docType = "EAD";
        this.mockND.docType = DocumentType.EAD;
        String searchTerm = "Smith";

        // mock suggestions to be returned by mock ViafClient
        ViafResource mockvr = Mockito.mock(ViafResource.class);
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        suggestions.add(mockvr);
        Mockito.when(ViafClient.suggest(searchTerm)).thenReturn(suggestions);

        // simulate user clicking cancel
        Mockito.when(this.mockND.getUserSelection(suggestions)).thenReturn(null);
        result = this.mockND.queryVIAF(searchTerm);
        assertEquals(null, result);

        // user selects first (only) option
        Mockito.when(this.mockND.queryVIAF(searchTerm)).thenCallRealMethod();
        Mockito.when(this.mockND.getUserSelection(suggestions)).thenReturn(mockvr);

        // makeTag functionality is tested separately.
        // if possible, could verify it was called with the correct arguments...
    }

     @Test
     public void testTagAllowed() throws Exception {

        int wsId = StandalonePluginWorkspace.MAIN_EDITING_AREA;
        StandalonePluginWorkspace mockWS = Mockito.mock(StandalonePluginWorkspace.class);

        this.mockND.docType = DocumentType.EAD;

        // simulate editor unavailable - should be null
        Mockito.when(this.mockContext.getPluginWorkspace()).thenReturn(mockWS);
        Mockito.when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(null);
        Mockito.when(this.mockND.tagAllowed(this.mockContext)).thenCallRealMethod();
        assertEquals(null, this.mockND.tagAllowed(this.mockContext));

        // simulate editor but no page available - should be null
        WSEditor mockEd = Mockito.mock(WSEditor.class);
        Mockito.when(mockWS.getCurrentEditorAccess(wsId)).thenReturn(mockEd);
        Mockito.when(mockEd.getCurrentPage()).thenReturn(null);
        assertEquals(null, this.mockND.tagAllowed(this.mockContext));

        // simulate full schema access, no elements allowed; should be false
        WSXMLTextEditorPage mockPage = Mockito.mock(WSXMLTextEditorPage.class);
        WSTextXMLSchemaManager mockSchema = Mockito.mock(WSTextXMLSchemaManager.class);
        Mockito.when(mockEd.getCurrentPage()).thenReturn(mockPage);
        Mockito.when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
        int offset = 1;
        Mockito.when(mockPage.getSelectionStart()).thenReturn(offset);
        // getSelectionStart could throw a javax.swing.text.BadLocationException
        WhatElementsCanGoHereContext mockContext;
        mockContext = Mockito.mock(WhatElementsCanGoHereContext.class);
        Mockito.when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
        java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
        Mockito.when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
        assertEquals(false, this.mockND.tagAllowed(this.mockContext));

        // schema access and tag matches an allowed element; should be true
        CIElement el = Mockito.mock(CIElement.class);
        Mockito.when(el.getName()).thenReturn("name");
        elements.add(el);
        assertEquals(true, this.mockND.tagAllowed(this.mockContext));

     }


}
