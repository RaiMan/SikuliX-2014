public static void main(String[] args){

    Socket s = new Socket("192.168.1.17", 5900); //open a socket to vnc server on listening port
    s.setSoTimeout(1000);
    s.setKeepAlive(true); //some socket configuration

    ConnectionController cc = new ConnectionController(s);
    cc.openConnection(0); //opens the vnc connection for connection 0, multiple are supported

    cc.setPixelFormat(0, "Truecolor", 32, 0);  //for connection 0, set pixel data to Truecolor, 32 bits per pixel, little endian
    cc.start(0); //start thread that keeps BufferedImage updated by polling server for remote desktop changes

    Thread.sleep(2000); //wait for buffered image to be updated before we do sikuli stuff

    VNCScreen vnc = new VNCScreen();  //default constructor uses ConnectionController index 0

    vnc.click(new Pattern("untitled.png"));

    //do other sikuli operations

    cc.closeConnection(0); //clean up socket, stop thread

}

Try and catch blocks for exceptions are omitted from example.  The VNC protocol supports multiple pixel formats.  Currently, the VNC client code only supports truecolor, 32 bits per pixel, little endian pixel format.  Depending on which VNC server you are using, it may initialize the connection with some other pixel format that is not currently supported.  In this case, a message is printed to stderr saying "Error: PixelFormat not supported, setPixelFormat required" to let the user know.  After that, the setPixelFormat line changes the connection to the format supported by the client.  That line could probably be better integrated into Sikuli's logging system but I am less familiar with the details of while level would be appropriate.
