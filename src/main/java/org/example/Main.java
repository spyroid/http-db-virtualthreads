package org.example;

import com.sun.net.httpserver.HttpServer;
import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {

        Jsonb jsonb = Jsonb.instance();
        JsonType<Response> responseType = jsonb.type(Response.class);

        var server = HttpServer.create();
        var listen = new InetSocketAddress("0.0.0.0", 8080);
        server.bind(listen, 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", exchange -> {
            try (var out = exchange.getResponseBody()) {
                var mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                var response = new Response("Simplest HTTP with VirtualThreads", formatSize(mem));
                var bytes = responseType.toJsonBytes(response);

                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, bytes.length);
                out.write(bytes);
            } catch (Exception e) {
            }
        });

        server.start();
        System.out.println("Server started on " + listen);
    }

    @Json
    record Response(String body, String usedMemory) {
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}
