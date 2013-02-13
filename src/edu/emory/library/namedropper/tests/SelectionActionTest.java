/**
 * file src/edu/emory/library/namedropper/tests/SelectionActionTest.java
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
import java.awt.event.ActionEvent;

import ro.sync.contentcompletion.xml.WhatElementsCanGoHereContext;
import ro.sync.exml.workspace.api.editor.page.text.WSTextXMLSchemaManager;
import ro.sync.exml.workspace.api.editor.page.text.xml.WSXMLTextEditorPage;
import ro.sync.contentcompletion.xml.CIElement;

import edu.emory.library.namedropper.plugins.DocumentType;
import edu.emory.library.namedropper.plugins.SelectionAction;
import edu.emory.library.namedropper.plugins.PluginOptions;


@RunWith(PowerMockRunner.class)
public class SelectionActionTest {
    // Mock objects
    SelectionAction mockAction;

    @Before
    public void setUp() {
        // initialize mock selection action
        this.mockAction = Mockito.mock(SelectionAction.class);
    }

    @After
    public void tearDown() {
        this.mockAction = null;
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();


     @Test
     @PrepareForTest(PluginOptions.class)
     public void testPreprocessNoDocType() throws Exception {
        PowerMockito.mockStatic(PluginOptions.class);

        Mockito.when(PluginOptions.getDocumentType()).thenReturn("");
        // NOTE: have to use doThrow because Mockito.when doesn't work with void methods
        Mockito.doThrow(new Exception("No Document Type has been selected")).when(this.mockAction).preprocess();
     }

     @Test
     public void testTagAllowed() throws Exception {

        Mockito.when(this.mockAction.tagAllowed(1)).thenCallRealMethod();
        Mockito.when(this.mockAction.tagAllowed(1, null)).thenCallRealMethod();
        this.mockAction.docType = DocumentType.EAD;

        // simulate editor page unavailable - should be null
        Mockito.when(this.mockAction.getCurrentPage()).thenReturn(null);
        assertEquals(null, this.mockAction.tagAllowed(1));

        // simulate full schema access, no elements allowed; should be false
        WSXMLTextEditorPage mockPage = Mockito.mock(WSXMLTextEditorPage.class);
        WSTextXMLSchemaManager mockSchema = Mockito.mock(WSTextXMLSchemaManager.class);
        Mockito.when(this.mockAction.getCurrentPage()).thenReturn(mockPage);
        Mockito.when(mockPage.getXMLSchemaManager()).thenReturn(mockSchema);
        int offset = 1;
        WhatElementsCanGoHereContext mockContext;
        mockContext = Mockito.mock(WhatElementsCanGoHereContext.class);
        Mockito.when(mockSchema.createWhatElementsCanGoHereContext(offset)).thenReturn(mockContext);
        java.util.List<CIElement> elements = new java.util.ArrayList<CIElement>();
        Mockito.when(mockSchema.whatElementsCanGoHere(mockContext)).thenReturn(elements);
        assertEquals(false, this.mockAction.tagAllowed(offset));

        // schema access and tag matches an allowed element; should be true
        CIElement el = Mockito.mock(CIElement.class);
        Mockito.when(el.getName()).thenReturn("name");
        elements.add(el);
        assertEquals(true, this.mockAction.tagAllowed(offset));

    }

     @Test
     public void testTagAllowedAtSelection() throws Exception {
        // should just get selection offset and call tagAllowed
        int offset = 55;
        WSXMLTextEditorPage mockPage = Mockito.mock(WSXMLTextEditorPage.class);
        Mockito.when(mockPage.getSelectionStart()).thenReturn(offset);
        Mockito.when(this.mockAction.tagAllowedAtSelection()).thenCallRealMethod();
        Mockito.when(this.mockAction.getCurrentPage()).thenReturn(mockPage);

        this.mockAction.tagAllowedAtSelection();
        // verify tag allowed was called with selection offset
        Mockito.verify(this.mockAction).tagAllowed(offset);

     }

}
