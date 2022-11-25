package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        var roles = new ArrayList<Role>();

        try (Connection conn = DriverManager.getConnection(args[0], args[1], args[2]);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("select * from pg_roles");
        ) {
            while (rs.next()) {
                roles.add(new Role(rs.getString("rolname"), rs.getBoolean("rolsuper")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        var mapper = new ObjectMapper();

        var server = HttpServer.create();
        var listen = new InetSocketAddress("0.0.0.0", 8080);
        server.bind(listen, 0);

        server.createContext("/dbroles", exchange -> Thread.startVirtualThread(() -> {
            try (var out = exchange.getResponseBody()) {
                var resp = mapper.writeValueAsBytes(roles);
                exchange.getResponseHeaders().add("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, resp.length);
                out.write(resp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        server.start();
        System.out.println("Server started on " + listen);
    }

    record Role(String name, boolean isSuperuser) {}
}

