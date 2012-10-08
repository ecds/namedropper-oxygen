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
  https://raw.github.com/emory-libraries-disc/name-dropper/develop/oxygen/res/OxygenAddons.xml
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

To update the add-on installation version, run ant to generate the plugin jar file, upload that jar file to github as a project download, and then update the URL in ``res/OxygenExtension-NameDropper.xml``.

