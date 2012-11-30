/**
 * file src/edu/emory/library/namedropper/plugins/PluginOptions.java
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

import javax.swing.AbstractAction;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;

import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.PluginWorkspace;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class PluginOptions {

    // option keys
    public static String DOCUMENT_TYPE = "NameDropper:DocumentType";
    public static String DEFAULT_ACTION = "NameDropper:DefaultAction";

    // static variables for option keys
    // methods for getting/setting options

    public static String getOption(String optionName) {
        PluginWorkspace ws = PluginWorkspaceProvider.getPluginWorkspace();
        return ws.getOptionsStorage().getOption(optionName, "");  // FIXME: use null instead?
    }

    public static void setOption(String optionName, String value) {
        PluginWorkspace ws = PluginWorkspaceProvider.getPluginWorkspace();
        ws.getOptionsStorage().setOption(optionName, value);
    }

    public static String getDocumentType() {
        return PluginOptions.getOption(PluginOptions.DOCUMENT_TYPE);
    }

    public static void setDocumentType(String value) {
        PluginOptions.setOption(PluginOptions.DOCUMENT_TYPE, value);
    }

    public static String getDefaultAction() {
        return PluginOptions.getOption(PluginOptions.DEFAULT_ACTION);
    }

    public static void setDefaultAction(String value) {
        PluginOptions.setOption(PluginOptions.DEFAULT_ACTION, value);
    }

}
