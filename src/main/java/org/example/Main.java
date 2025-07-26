package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {

        var mapper = new ObjectMapper();
        var server = HttpServer.create();
        var listen = new InetSocketAddress("0.0.0.0", 8080);
        server.bind(listen, 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", exchange -> {
            try (var out = exchange.getResponseBody()) {
                var str = ("Simplest HTTP with VirtualThreads\n\n" + Thread.currentThread());
                var mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                str += "\n\nUsed Memory: " + formatSize(mem);
                var asBytes = str.getBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, asBytes.length);
                out.write(asBytes);
            } catch (Exception e) {

            }
        });

        server.createContext("/dbroles", exchange -> {
            try (var out = exchange.getResponseBody()) {
                var roles = List.<Role>of();
                if (args.length > 2) {
                    roles = getRoles(args[0], args[1], args[2]);
                }
                var resp = mapper.writeValueAsBytes(roles);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                out.write(resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        server.start();
        System.out.println("Server started on " + listen);
    }

    private static List<Role> getRoles(String jdbcUrl, String user, String password) {
        var roles = new ArrayList<Role>();

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             var stmt = conn.prepareStatement("select * from pg_roles");
             ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                roles.add(new Role(rs.getString("rolname"), rs.getBoolean("rolsuper")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return roles;
    }

    record Role(String name, boolean isSuperuser) {
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }
}

