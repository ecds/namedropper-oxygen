/**
 * file oxygen/src/edu/emory/library/oxygen_plugin/NameDropper/NameDropperPlugin.java
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

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

public class NameDropperPlugin extends Plugin {
    /**
    * Plugin instance.
    */
    private static NameDropperPlugin instance = null;  
    
    /**
    * NameDropperPlugin constructor.
    * 
    * @param descriptor Plugin descriptor object.
    */
    public NameDropperPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    
        if (instance != null) {
            throw new IllegalStateException("Already instantiated !");
        }    
        instance = this;
    }
    
    /**
    * Get the plugin instance.
    * 
    * @return the shared plugin instance.
    */
    public static NameDropperPlugin getInstance() {
        return instance;
    }
}
