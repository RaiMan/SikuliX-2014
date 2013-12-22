SikuliX-Remote 1.1.0
==============

**Lightweight remote server for local Sikuli scripts or Sikuli Java programs**
<br />to make screenshots and use mouse and keyboard on a remote system.

It runs on a remote system or in a virtual app environment, that is accessible with an hostname or an IP address. The local Sikuli script acts on the remote screen/mouse/keyboard by creating a special screen object, that is then used like any other local screen/mouse/keyboard. The remote system must have a real screen buffer (headless does not work).

Since it is a Sikuli specific solution, It is not compatible to any other remote solution based on VNC, RFB or even classical remote desktop solutions and it does not have any features, to use it as a viewer for the remote screen.
 
The remote system only needs a valid Java installation to start the server from command line using:
java -jar sikulix-remoteserver.jar portnumber

If port number is a valid (free and > 1024) (we take 50000, if not given), the server will start and listen on the given port for incoming requests.

Currently this is supported: (only primary monitor for now)
- return the monitor physics
- take a screenshot on request and return the image
- return current mouse location
- accept and run all Sikuli mouse and keyboard actions

Feature ideas:
- transfer files in both directions (e.g. commandfiles)
- run command files and start applications
- access the remote clipboard (implement the paste feature)

Since it is a Maven project: use *mvn clean install* to create the jar.

**Usage Information** [... look here](https://github.com/RaiMan/SikuliX-Remote/wiki/Sikuli-Remote:-how-to-use-it)

**BE AWARE: this is very experimental, not yet complete and not fully tested nor documented.**
