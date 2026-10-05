package com.onevn.server.database;


import java.sql.Connection;


public class DatabaseTest {


    public static void main(String[] args) {


        try (Connection connection =
                     DatabaseConnection.getConnection()) {


            System.out.println(
                    "MySQL connection successful."
            );


        } catch (Exception e) {


            System.out.println(
                    "MySQL connection failed."
            );


            e.printStackTrace();
        }
    }
}
