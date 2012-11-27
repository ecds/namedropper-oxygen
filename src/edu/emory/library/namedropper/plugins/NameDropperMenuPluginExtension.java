/**
 * file src/edu/emory/library/namedropper/plugins/NameDropperMenuPluginExtension.java
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

import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.Menu;


import edu.emory.library.namedropper.plugins.DocumentType;

/**
 * Plugin extension - workspace access extension.
 */
public class NameDropperMenuPluginExtension implements WorkspaceAccessPluginExtension {

    // Store document type when one of the document type menu items is clicked
    final Action setDocumentTypeAction = new AbstractAction() {
      public void actionPerformed(ActionEvent selection) {
        // store the selected document type;
        // action command is the text of the menu item, i.e. TEI or EAD
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", selection.getActionCommand());
      }
    };

    // get the current value for selected docType, if any
    public String getCurrentDoctype(){
      return pluginWorkspaceAccess.getOptionsStorage().getOption("docType", "");
    }

  /**
   * Plugin workspace access.
   */
  private StandalonePluginWorkspace pluginWorkspaceAccess;

  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    this.pluginWorkspaceAccess = pluginWorkspaceAccess;

    pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() {

      public void customizeMainMenu(JMenuBar mainMenuBar) {
        // nameDropper menu
        JMenu ndMenu = createNDMenu();
        // Add the ndMenu just before Help menu
        mainMenuBar.add(ndMenu, mainMenuBar.getMenuCount() - 1);
      }
    });

 }

  /**
   * Create a custom NameDropper menu to allow the users to configure plugin behavior.
   * Currently consists of a document type submenu with a radio-style list
   * of supported document types
   *
   * @return Menu
   */
  private JMenu createNDMenu() {
    // ndMenu
    Menu ndMenu = new Menu("NameDropper", true);
    Menu docTypeMenu = new Menu("Document Type", true);

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

    ndMenu.add(docTypeMenu);
    return ndMenu;
  }

  // required because we are implementing Interface WorkspaceAccessPluginExtension
  public boolean applicationClosing() {
    return true;  // it's ok with this plugin for the application to close
  }
}