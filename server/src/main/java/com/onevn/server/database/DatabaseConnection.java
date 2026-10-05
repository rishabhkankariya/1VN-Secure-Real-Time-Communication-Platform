package com.onevn.server.database;


import com.onevn.server.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {


    public static Connection getConnection()
            throws SQLException {


        String url = "jdbc:mysql://"
                + DatabaseConfig.HOST
                + ":"
                + DatabaseConfig.PORT
                + "/"
                + DatabaseConfig.DATABASE;


        return DriverManager.getConnection(
                url,
                DatabaseConfig.USER,
                DatabaseConfig.PASSWORD
        );
    }
}
