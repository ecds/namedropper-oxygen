NameDropper OxygenXML plugin
****************************

This is a plugin or add-on for the `Oxygen XML editor`_  (version 14.0) to
simplify the process of tagging names in XML and associating those names with
authoritative identifiers (currently using `VIAF`_).  See the top-level
`namedropper`_ repository for more information.

.. image:: https://travis-ci.org/emory-libraries-disc/namedropper-oxygen.png?branch=develop
  :alt: current build status for namedropper-py
  :target: https://travis-ci.org/emory-libraries-disc/namedropper-oxygen

.. _Oxygen XML editor: http://oxygenxml.com/
.. _VIAF: http://viaf.org/
.. _namedropper: https://github.com/emory-libraries-disc/name-dropper

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

License
=======
NameDropper Oxygen plugin is distributed under the
`Apache 2.0 License <http://www.apache.org/licenses/LICENSE-2.0>`_.

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

All setup instructions below assume you have cloned the repository from Github:

  git clone http://github.com/emory-libraries-disc/namedropper-oxygen

Setup with NetBeans
-------------------

* Create a new project "From Existing Source"
* Set the source directory to the base directory (where this file is)
* Edit the project properties > Libraries and add lib/oxygen.jar  lib/api.zip, json-simple-1.1.1.jar, lib/xom-1.2.8.jar


Setup with Eclipse
------------------

In Eclipse, File > Import > General > Existing Projects into Workspace > Next. Make sure to uncheck ``Copy projects into workspace`` so that the project is created inside the name-dropper repository.

To build any changes just right click on ``build.xml`` and Run As > Ant Build.


Setup without an IDE
--------------------

Run ``ant`` from name-dropper/oxygen to build the distribution and run the tests.


Oxygen developer documentation
------------------------------

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
to build and copy the latest version of the code into the oxygen plugins directory.

-----

See **HOWTO-RELEASE** for instructions on releasing or updating the user-
installable Oxygen add-on.
