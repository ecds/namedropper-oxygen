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
