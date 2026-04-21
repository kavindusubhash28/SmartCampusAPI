package com.mycompany.smart.campus.api;

import java.io.IOException;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

public class SmartCampusApi {

    public static final String BASE_URI = "http://localhost:8081/api/v1/";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .packages("com.mycompany.smart.campus.api");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = startServer();
        System.out.println("Smart Campus API started.");
        System.out.println("Open: " + BASE_URI);
        System.out.println("Press Enter to stop the server...");
        System.in.read();
        server.shutdownNow();
    }
}