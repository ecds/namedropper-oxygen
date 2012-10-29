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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.options.WSOptionChangedEvent;
import ro.sync.exml.workspace.api.options.WSOptionListener;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ui.Menu;

/**
 * Plugin extension - workspace access extension.
 */
public class NameDropperMenuPluginExtension implements WorkspaceAccessPluginExtension {
    
    // labels and stuff for menu actions
    final String eadLabel = "EAD";
    final String teiLabel = "TEI";
    final String checkmark = " \u2713";
    
    // Set EAD action
    final Action setEADAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) { 
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", eadLabel);
      }
    };

    // Set TEI action
    final Action setTEIAction = new AbstractAction() {
      public void actionPerformed(ActionEvent arg0) {  
        pluginWorkspaceAccess.getOptionsStorage().setOption("docType", teiLabel);
      }
    };
    
    public final JMenuItem setEADItem = new JMenuItem();
    public final JMenuItem setTEIItem = new JMenuItem();
    
    /*
     * Sets the check mark for in the menu for the currently selected mode EAD or TEI 
     */
    public void setMenu(){

            
      if (pluginWorkspaceAccess.getOptionsStorage().getOption("docType", "").equals(eadLabel)){
         setEADItem.setText(eadLabel + checkmark);
         setTEIItem.setText(teiLabel);
      }
      
      if (pluginWorkspaceAccess.getOptionsStorage().getOption("docType", "").equals(teiLabel)){
         setTEIItem.setText(teiLabel + checkmark);
          setEADItem.setText(eadLabel);
         
      }
  }
    
    
    
    //Option Listener - triggers when docType option changes
    WSOptionListener OL = new WSOptionListener("docType"){
        @Override
        public void  optionValueChanged(WSOptionChangedEvent e) {
            setMenu();    
        }
    };    

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
        // Add the ndMenu
        mainMenuBar.add(ndMenu, mainMenuBar.getMenuCount() - 1);        
      }
    });
    
    // Attach option Listner to the options
    pluginWorkspaceAccess.getOptionsStorage().addOptionListener(OL);
 }
  
  /**
   * Create menu that contains the following actions:
   * setEAD, setTEI 
   * @return The ndMenu.
   */
  private JMenu createNDMenu() {      
    // ndMenu
    Menu ndMenu = new Menu("NameDropper", true);
    Menu docTypeMenu = new Menu("Document Type", true);
    
    // Add setEAD action on the menu
    setEADItem.setAction(setEADAction);
    setEADItem.setText(eadLabel);
    docTypeMenu.add(setEADItem);

    // Add setTEI action on the menu
    setTEIItem.setAction(setTEIAction);
    setTEIItem.setText(teiLabel);
    docTypeMenu.add(setTEIItem);
    
    ndMenu.add(docTypeMenu);
    
    
    setMenu();
    
    return ndMenu;
  }
  
  
  
  

  //aparently required for some reason
  public boolean applicationClosing() {return true;} 
}