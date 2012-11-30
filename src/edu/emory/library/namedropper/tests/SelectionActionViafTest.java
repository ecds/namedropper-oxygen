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

import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.contentcompletion.xml.CIElement;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.SelectionActionViaf;
import edu.emory.library.viaf.ViafClient;
import edu.emory.library.viaf.ViafResource;


@RunWith(PowerMockRunner.class)
@PrepareForTest(ViafClient.class)
public class SelectionActionViafTest {
    // Mock objects
    SelectionActionViaf mockViaf;

    @Before
    public void setUp() {
        // initialize mock viaf action
        this.mockViaf = Mockito.mock(SelectionActionViaf.class);
    }

    @After
    public void tearDown() {
        this.mockViaf = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

     @Test
     public void testQueryViafNoDocType() throws Exception {

        exception.expect(Exception.class);
        exception.expectMessage("No DocType selected");

        String searchTerm = "Smth";
        // FIXME: use actual class instead of mock here?
        Mockito.when(this.mockViaf.queryVIAF(searchTerm)).thenCallRealMethod();
        this.mockViaf.queryVIAF(searchTerm);
     }

     @Test
     public void testQueryViafNoResults() throws Exception {
        PowerMockito.mockStatic(ViafClient.class);

        exception.expect(Exception.class);
        exception.expectMessage("No Results");

        String term = "Smth";
        this.mockViaf.docType = DocumentType.EAD;
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        Mockito.when(this.mockViaf.queryVIAF(term)).thenCallRealMethod();
        Mockito.when(ViafClient.suggest(term)).thenReturn(suggestions);

        this.mockViaf.queryVIAF(term);
     }

    @Test
    public void testQueryViaf() throws Exception {
        PowerMockito.mockStatic(ViafClient.class);

        String result;
        this.mockViaf.docType = DocumentType.EAD;
        String searchTerm = "Smith";

        // mock suggestions to be returned by mock ViafClient
        ViafResource mockvr = Mockito.mock(ViafResource.class);
        List<ViafResource> suggestions = new ArrayList<ViafResource>();
        suggestions.add(mockvr);
        Mockito.when(ViafClient.suggest(searchTerm)).thenReturn(suggestions);

        // simulate user clicking cancel
        Mockito.when(this.mockViaf.getUserSelection(suggestions)).thenReturn(null);
        result = this.mockViaf.queryVIAF(searchTerm);
        assertEquals(null, result);

        // user selects first (only) option
        Mockito.when(this.mockViaf.queryVIAF(searchTerm)).thenCallRealMethod();
        Mockito.when(this.mockViaf.getUserSelection(suggestions)).thenReturn(mockvr);

        // makeTag functionality is tested separately.
        // if possible, could verify it was called with the correct arguments...
    }

     @Test
     public void testTagAllowed() throws Exception {

        Mockito.when(this.mockViaf.tagAllowed()).thenCallRealMethod();
        this.mockViaf.docType = DocumentType.EAD;

        // simulate editor page unavailable - should be null
        Mockito.when(this.mockViaf.getCurrentPage()).thenReturn(null);
        assertEquals(null, this.mockViaf.tagAllowed());

        // simulate full schema access, no elements allowed; should be false
        WSXMLTextEditorPage mockPage = Mockito.mock(WSXMLTextEditorPage.class);
        WSTextXMLSchemaManager mockSchema = Mockito.mock(WSTextXMLSchemaManager.class);
        Mockito.when(this.mockViaf.getCurrentPage()).thenReturn(mockPage);
        Mockito.when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
        int offset = 1;
        Mockito.when(mockPage.getSelectionStart()).thenReturn(offset);
        // getSelectionStart could throw a javax.swing.text.BadLocationException
        WhatElementsCanGoHereContext mockContext;
        mockContext = Mockito.mock(WhatElementsCanGoHereContext.class);
        Mockito.when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
        java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
        Mockito.when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
        assertEquals(false, this.mockViaf.tagAllowed());

        // schema access and tag matches an allowed element; should be true
        CIElement el = Mockito.mock(CIElement.class);
        Mockito.when(el.getName()).thenReturn("name");
        elements.add(el);
        assertEquals(true, this.mockViaf.tagAllowed());

     }

}
