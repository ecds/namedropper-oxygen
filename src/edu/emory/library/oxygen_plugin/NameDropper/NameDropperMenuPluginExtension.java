/**
 * file oxygen/src/edu/emory/library/oxygen_plugin/NameDropper/NameDropperMenuPluginExtension.java
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

package edu.emory.library.oxygen_plugin.NameDropper;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

/**
 * Plugin extension - workspace access extension.
 */
public class NameDropperMenuPluginExtension implements WorkspaceAccessPluginExtension {

  /**
   * Plugin workspace access.
   */
  private StandalonePluginWorkspace pluginWorkspaceAccess;
  
  public void applicationStarted(final StandalonePluginWorkspace pluginWorkspaceAccess) {
    this.pluginWorkspaceAccess = pluginWorkspaceAccess;
    
    // Set EAD action
    final Action setEADAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) { 
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", "EAD");
      }
    };

    // Set TEI action
    final Action setTEIAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) {  
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", "TEI");
      }
    };
    
    pluginWorkspaceAccess.addMenuBarCustomizer(new MenuBarCustomizer() { 

      public void customizeMainMenu(JMenuBar mainMenuBar) {
        // nameDropper menu
        JMenu ndMenu = createNDMenu(setEADAction, setTEIAction);
        // Add the ndMenu
        mainMenuBar.add(ndMenu, mainMenuBar.getMenuCount() - 1);
        
      }
    });

 }
  
  /**
   * Create menu that contains the following actions:
   * setEAD, setTEI
   * 
   * @param setEADAction Sets docType prop to EAD.
   * @param setTEIAction Sets docType prop to TEI.
   
   * 
   * @return The ndMenu.
   */
  private JMenu createNDMenu(
      final Action setEAD, 
      final Action setTEI) {
    // ndMenu
    Menu ndMenu = new Menu("NameDropper", true); 
    
    // Add setEAD action on the menu
    final JMenuItem setEADItem = new JMenuItem(setEAD); 
    setEADItem.setText("Set EAD");
    ndMenu.add(setEADItem);

    // Add setTEI action on the menu
    final JMenuItem setTEIItem = new JMenuItem(setTEI); 
    setTEIItem.setText("Set TEI");
    ndMenu.add(setTEIItem);
    
    return ndMenu;
  }

  //aparently required for some reason
  public boolean applicationClosing() {return true;}
}