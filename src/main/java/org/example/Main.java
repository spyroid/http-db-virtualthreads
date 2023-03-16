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

public class Main {
    public static void main(String[] args) throws Exception {

        var mapper = new ObjectMapper();
        var server = HttpServer.create();
        var listen = new InetSocketAddress("0.0.0.0", 8080);
        server.bind(listen, 0);

        server.createContext("/", exchange -> Thread.startVirtualThread(() -> {
            try (var out = exchange.getResponseBody()) {
                var resp = ("Simplest HTTP with VirtualThreads\n\n" + Thread.currentThread()).getBytes();
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, resp.length);
                out.write(resp);
            } catch (Exception e) {

            }
        }));

        server.createContext("/dbroles", exchange -> Thread.startVirtualThread(() -> {
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
        }));
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
}

