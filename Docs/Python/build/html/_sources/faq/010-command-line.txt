How to run Sikuli from Command Line
===================================

SikuliX IDE or Script can be used on command line to run a Sikuli script or open it in the IDE. 

The usage on each platform:

.. windows::

   :command:`PATH-TO-SIKULIX/runIDE.cmd [options]` or 
   :command:`PATH-TO-SIKULIX/runScript.cmd [options]` 

.. mac::

   :command:`PATH-TO-SIKULIX/runIDE [options]` or
   :command:`PATH-TO-SIKULIX/runScript [options]`

.. linux::

   :command:`PATH-TO-SIKULIX/runIDE [options]` or
   :command:`PATH-TO-SIKULIX/runScript [options]`
   
**runIDE(.cmd) without any options** simply starts SikuliX IDE.

**PATH-TO-SIKULIX** is the folder containing the Sikuli stuff after having run setup.

Command Line Options
--------------------

:program:`SikuliX IDE or Script`

.. option:: -- <arguments>          

   the space delimeted and optionally quoted arguments are passed to Jython's sys.argv and hence are available to your script

.. option::  -h,--help                      

   print the help message showing the available options

.. option::  -r,--run <sikuli-folder/file>         

   run .sikuli or .skl file
   
.. option::  -c,--console                    

   all output goes to stdout

.. option::  -i,--interactive                    

   open an interactive Jython session that is prepared for the usage of the Sikuli features
