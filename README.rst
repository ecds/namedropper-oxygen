NameDropper OxygenXML  plugin
******************************

This is a plugin or add-on for the `Oxygen XML editor`_ to simplify the process of tagging names in XML and
associating those names with authoritative identifiers.

.. _`Oxygen XML editor`: http://oxygenxml.com/

Plugin installation via Oxygen Add-Ons
======================================

To install the *development* version of the plugin as an Oxygen Add-On:

* Open Oxygen Preferences and select **Add-Ons**
* Add and save a new add-on site URL:
  https://github.com/emory-libraries-disc/name-dropper/blob/release/0.1.0/oxygen/res/OxygenAddons.xml
* Under the Oxygen Help menu, select **Manage add-ons...**
* Select **NameDropper** and then click install.  You should be prompted to accept the
  license and then restart Oxygen.


Development Requirements and Setup
==================================

* Ant
* Java
* oxygen.jar and api.zip (from http://oxygenxml.com/InstData/Editor/Plugins/OxygenPluginsDevelopmentKit.zip ,
  included in ``lib`` directory)
* json-simple (from http://code.google.com/p/json-simple/ , included in ``lib``)
* xom (from http://www.cafeconleche.org/XOM/xom-1.2.8.jar , included in ``lib``)

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


Manually install the Compiled Plugin in Oxygen
----------------------------------------------
* Make sure the Oxygen program is not running
* Delete the entire director of any old copies of NameDropper in oxygen/plugins
* Copy the **NameDropper** directory in the dist directory to your installation of Oxygen,
  inside the ``oxygen/plugins`` directory.


Update the user-installable Oxygen add-on
-----------------------------------------

There are a few manual steps required to update the version of this plugin that can be installed using the add-ons interface within Oxygen.

* Run ``ant`` to generate the plugin version of the jar file.
* Rename the plugin jar file (``dist/NameDropper-plugin.jar``) to something reflecting the version you are building, e.g. ``NameDropper-0.1.0-dev.jar``
* Upload the file to the project on GitHub using the **Downloads** file management tools.
* Update the download location in the add-on xml file ``res/OxygenAddons.xml`` with the full URL to the download
file you just uploaded (commit and push to github).
