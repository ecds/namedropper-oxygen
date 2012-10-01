Requirements
============
Ant
Java
json-simple http://code.google.com/p/json-simple/


Installation(With-NetBeans)
===========================
You can ignore the netbeansproject.dist and build.xml.dist folder and files. Good versions will be created for you.

* Create a new project "From Existing Source"
* Set the source directory to the base directory (where this file is)
* Edit the project properties > Libraries and add lib/oxygen.jar  lib/api.zip, json-simple-1.1.1.jar



Installation(Non-NetBeans)
==========================
* Copy build.xml.dist to build.xml
* Copy nbproject.dist to nbproject


.. Note::

   To build the project run 'ant' in the base directory.



Install The Compiled Plugin in Oxygen
=====================================
* Copy the directory in the dist directory to your installation of Oxygen. Put it in the oxygen/plugins directory.
