package com.sikulix.remoteserver.server;

import org.apache.commons.lang.math.NumberUtils;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Author: Sergey Kuts
 */
public class Server {

    private static final int DEFAULT_PORT = 4041;
    private static final Logger SERVER_LOGGER = Logger.getLogger(Server.class.getName());

    public static void startServer(final int port) throws Exception {
        final URI baseUri = URI.create("http://" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "/sikuli");
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, new ServerConfiguration());

        System.in.read();
        server.shutdown();
    }

    public static void main(final String[] args) {
        try {
            startServer(args.length > 0 && NumberUtils.isNumber(args[0]) ? Integer.parseInt(args[0]) : DEFAULT_PORT);
        } catch (Exception e) {
            SERVER_LOGGER.severe(e.toString());
        }
    }
}
