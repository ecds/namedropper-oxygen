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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;

import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

import edu.emory.library.namedropper.plugins.DocumentType;

/**
 * Custom NameDropper menu to allow users to configure plugin behavior.
 * Currently consists of a document type submenu with a radio-style list
 * of supported document types.
 */
public class NameDropperMenu extends Menu {

   /**
   * Plugin workspace access.
   */
  private StandalonePluginWorkspace workspace;

  private static String documentTypeOption = "docType";  // FIXME: should this be namedropper-specific?
  // FIXME: where should this be set? needs to be shared with plugin extension

  private static String label = "NameDropper";

  public NameDropperMenu(StandalonePluginWorkspace ws) {
    super(label,  // menu title
      true);      // true = this menu is intended for oxygen menubar
    workspace = ws;

    // configure the menu
    Menu docTypeMenu = new Menu("Document Type");
    String currentType = getCurrentDoctype();

    // create radio-button style menu for defined document types
    ButtonGroup docTypeGroup = new ButtonGroup();
    JRadioButtonMenuItem docTypeItem;

    for (DocumentType type : DocumentType.values()) {
      String label = type.toString();
      docTypeItem = new JRadioButtonMenuItem(label);
      // if current type is set, initialize matching menu item as selected
      if (currentType.equals(label)) {
        docTypeItem.setSelected(true);
      }
      docTypeItem.setAction(setDocumentTypeAction);
      docTypeItem.setText(label);
      docTypeGroup.add(docTypeItem);
      docTypeMenu.add(docTypeItem);
      // NOTE: should be possible to set keyboard shortcuts for these if we want to
      // see methods setAccelerator and setMnemonic
    }

    this.add(docTypeMenu);
  }

  // Store document type when one of the document type menu items is clicked
  final Action setDocumentTypeAction = new AbstractAction() {
    public void actionPerformed(ActionEvent selection) {
      // store the selected document type;
      // action command is the text of the menu item, i.e. TEI or EAD
      workspace.getOptionsStorage().setOption(documentTypeOption,
          selection.getActionCommand());
    }
  };

    // get the current value for selected docType, if any
    public String getCurrentDoctype(){
      return workspace.getOptionsStorage().getOption(documentTypeOption, "");
    }

  }
