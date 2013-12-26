SikuliX-Docs
============

The official documentation for SikuliX-1.1+

<hr />

**To get the documentation package without a setup <br />
just doubleclick SikuliX-Docs.zip and use "View Raw" to download, <br />then unzip and double-click the contained `index.html`** 

<hr />

**Beginning with version 1.1.0 the documentation in HTML format will be made available on your local system during setup.** 

In the IDE you can open the documentation with the respectivce entry in the Help menu. The command scripts will have an option to open the dcumentation. In all cases it will be opened in your standard browser using the index.html on the first level of the docs folder, so you might as well open it directly and/or add a bookmark to your browser. 

**General Information and details about the scripting API**

Currently the only supported scripting language is Jython.

The sources of the docs are contained here in the Python folder and are text files in restructured text (.rst) format. 

To build the docs from the sources, you need the Python based Sphinx converter <br />
(needing Python 2.7+ and using `easy_install -U Sphinx`). 

To run the build on Mac/Linux use `make html` (on Windows `make.bat html`) in the folder containing the source folder (for other target formats use without html to see the options). The HTML version will be built in the folder `build/html`.

**Information for the Java API**

The docs contain information about the Java API at places, where there are major differences to the scripting API.

The contained HTML formatted JavaDocs are restricted to the officially supported public API. Classes and methods marked as deprecated might vanish without further notice in the future (earliest though with version 1.2).
