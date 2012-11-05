NameDropper OxygenXML plugin
****************************

This is a plugin or add-on for the `Oxygen XML editor`_  (version 14.0) to
simplify the process of tagging names in XML and associating those names with
authoritative identifiers (currently using `VIAF`_).

.. _`Oxygen XML editor`: http://oxygenxml.com/
.. _`VIAF`: http://viaf.org/

Plugin installation via Oxygen Add-Ons
======================================

To install the current released version of the plugin as an Oxygen Add-On:

* Open Oxygen Preferences and select **Add-Ons**
* Add and save a new add-on site URL:
  https://raw.github.com/emory-libraries-disc/name-dropper/master/oxygen/res/OxygenAddons.xml
* Under the Oxygen Help menu, select **Manage add-ons...**
* Select **NameDropper** and then click install.  You should be prompted to accept the
  license and then restart Oxygen.
* As new versions are released, you should be able to update the NameDropper plugin by
  using the **Check for add-ons updates..** menu entry.

Using the plugin in Oxygen
--------------------------

To use the plugin once you have it installed:

* The first time you use the plugin, or if you need to switch between documents, use the
  NameDropper menu located in the main Oxygen menu bar to choose the type of document
  you will be editing (TEI or EAD).
* Highlight a name in your document and invoke the plugin either by using the contextual
  menu and selecting **Plugins -> NameDropper** or by using the keyboard
  shortcut **Control + Shift + N**.

Current behavior is to add an appropriate name tag (based on the document type and the
type of record found) with a VIAF id for for the first VIAF match found.  You should
see a warning message if no match is found for the highlighted text, or if a name
tag is not valid in the current context.

Development Requirements and Setup
==================================

* Ant
* Java
* oxygen.jar api.zip, jide.jar and workspaceaccess.jar (from http://oxygenxml.com/InstData/Editor/Plugins/OxygenPluginsDevelopmentKit.zip ,
  included in ``lib`` directory)
* json-simple (from http://code.google.com/p/json-simple/ , included in ``lib``)
* xom (from http://www.cafeconleche.org/XOM/xom-1.2.8.jar , included in ``lib``)
* junit (provided by netbeans)
* mockito (http://code.google.com/p/mockito/ , included in ``lib``)
* log4j required for unit tests (included in ``lib``)

.. Note::
   Originally ``api.zip`` was named ``src.zip``.  In this project it has been renamed to api.zip to reduce confusion.


Setup with NetBeans
-------------------
You can ignore the ``netbeansproject.dist`` and ``build.xml.dist`` folder and files. Good versions will be created for you.

* Create a new project "From Existing Source"
* Set the source directory to the base directory (where this file is)
* Edit the project properties > Libraries and add lib/oxygen.jar  lib/api.zip, json-simple-1.1.1.jar, lib/xom-1.2.8.jar


Setup without NetBeans
----------------------
* Copy build.xml.dist to build.xml
* Copy nbproject.dist to nbproject

.. Note::
   To build the project run 'ant' in the base directory.

Oxygen developer documentation
------------------------------

Developers may find it useful to refer to the `Oxygen XML Editor documentation`_,
particularly the section on `extending Oxygen with plugins`_, as well as the
`Oxygen API documents`_.

.. _`Oxygen XML Editor documentation`: http://oxygenxml.com/doc/ug-editor/index.html
.. _`extending Oxygen with plugins`: http://oxygenxml.com/doc/ug-editor/index.html#topics/extend-oxygen-with-plugins.html
.. _`Oxygen API documents`: http://www.oxygenxml.com/InstData/Editor/Plugins/javadoc/

Manually install the Compiled Plugin in Oxygen
----------------------------------------------
* Make sure the Oxygen program is not running
* Delete the entire director of any old copies of NameDropper in oxygen/plugins
* Copy the **NameDropper** directory in the dist directory to your installation of Oxygen,
  inside the ``oxygen/plugins`` directory.


See `HOWTO-RELEASE`_ for instructions on releasing or updating the user-
installable Oxygen add-on.

.. _HOWTO-RELEASE: HOWTO-RELEASE.rst
