Developer Notes
***************

Requirements and Setup
======================

* Ant
* Java
* oxygen.jar api.zip, and workspaceaccess.jar (from `Oxygen Plugin SDK`_,
  included in lib directory)
* json-simple (from http://code.google.com/p/json-simple/ )
* xom (from http://www.cafeconleche.org/XOM/xom-1.2.8.jar )
* junit (provided by netbeans)
* mockito (http://code.google.com/p/mockito/)
* log4j

.. _Oxygen Plugin SDK: http://oxygenxml.com/InstData/Editor/Plugins/OxygenPluginsDevelopmentKit.zip

.. Note::
   Originally ``api.zip`` was named ``src.zip``.  In this project it has been renamed to api.zip to reduce confusion.

All setup instructions below assume you have cloned the repository from Github:

  git clone http://github.com/emory-libraries-disc/namedropper-oxygen

Common setup
------------

Dependencies can now be downloaded using maven::

  mvn dependency:copy-dependencies -DoutputDirectory=lib


Setup with NetBeans
-------------------

* Create a new project "From Existing Source"
* Set the source directory to the base directory (where this file is)
* Edit the project properties > Libraries and add lib/oxygen.jar  lib/api.zip, json-simple-1.1.1.jar,
  lib/xom-1.2.8.jar


Setup with Eclipse
------------------

In Eclipse, File > Import > General > Existing Projects into Workspace > Next. Make sure to uncheck ``Copy projects into workspace`` so that the project is created inside the name-dropper repository.

To build any changes just right click on ``build.xml`` and Run As > Ant Build.


Setup without an IDE
--------------------

Run ``ant`` to build the distribution and run the tests.


Oxygen developer documentation
==============================

Developers may find it useful to refer to the `Oxygen XML Editor documentation`_,
particularly the section on `extending Oxygen with plugins`_, as well as the
`Oxygen API documents`_.

.. _Oxygen XML Editor documentation: http://oxygenxml.com/doc/ug-editor/index.html
.. _extending Oxygen with plugins: http://oxygenxml.com/doc/ug-editor/index.html#topics/extend-oxygen-with-plugins.html
.. _Oxygen API documents: http://www.oxygenxml.com/InstData/Editor/Plugins/javadoc/

Manually install the compiled plugin in Oxygen
----------------------------------------------

* Make sure the Oxygen program is not running
* Delete the entire director of any old copies of NameDropper in oxygen/plugins
* Copy the **NameDropper** directory in the dist directory to your installation of Oxygen,
  inside the ``oxygen/plugins`` directory.

If you set the **OXYGEN_HOME** environment variable, you can use the ``ant install`` target as a convenience
to build and copy the latest version of the code into the oxygen plugins directory, and ``ant uninstall`` to
remove the locally installed plugin.

See `HOWTO-RELEASE`_ for instructions on releasing or updating the user-
installable Oxygen add-on.

.. _HOWTO-RELEASE: HOWTO-RELEASE.rst

----

Adding a new selection action to the plugin
===========================================

Currently, the NameDropper plugin consists of several different selection-based actions
that interact with different services for different name look-up or recognition.  The plugin
code is setup to be extensible for adding other selection actions; this is a summary of the
process to add an new action.

1. Add any new libraries or dependencies to `<pom.xml>`_ so they can be automatically
   downloaded by other developers or continuous integration systems.
2. Extend the base **SelectionAction** class:
 * Set a short name, a full name (for display in the NameDropper menu),
   and a keyboard shortcut key
 * Extend the **processSelection** method with the desired functionality
   to be triggered on user-selected text.
 * If the new selection action has user-configurable options, extend the
   **hasUserOptions** method and return True, and implement the **getOptionsAction**
   to return a javax.swing.Action for displaying and processing your options.

3. Update the **ActionType** enum to include your new selection action.  This should
   automatically include your new action and settings, if present, in the NameDropper
   menu and bind the keyboard shortcuts.
4. Add a brief description of the new funtionality to the Oxygen plugin description in
   `OxygenAddons.xml <res/OxygenAddons.xml>`_ and more detailed instructions (if necessary) to the usage
   information in the project `README <README.rst>`_.



