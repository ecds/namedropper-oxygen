package ro.sync.sample.plugin.uppercase;

import ro.sync.exml.plugin.Plugin;
import ro.sync.exml.plugin.PluginDescriptor;

public class UppercasePlugin extends Plugin {
    /**
    * Plugin instance.
    */
    private static UppercasePlugin instance = null;  
    
    /**
    * UppercasePlugin constructor.
    * 
    * @param descriptor Plugin descriptor object.
    */
    public UppercasePlugin(PluginDescriptor descriptor) {
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
    public static UppercasePlugin getInstance() {
        return instance;
    }
}
