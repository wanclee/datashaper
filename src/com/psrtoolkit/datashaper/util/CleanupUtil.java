/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Utility for taking care of closing system resources
 */
package com.psrtoolkit.datashaper.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility for taking care of closing system resources or objects references in
 * arrays
 *
 * @author Wan
 *
 */
public class CleanupUtil {

    public static void close(ResultSet result) {
        try {
            if (result != null /*
                     * && result.isClosed() == false
                     */) {
                result.close();
                result = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void close(Statement stmt) {
        try {
            //TODO: comment out due to exception from ORACLE driver ojdbc5.jar:
            //java.lang.AbstractMethodError: oracle.jdbc.driver.OracleStatementWrapper.isClosed()Z
            //if (stmt != null && stmt.isClosed() == false) {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void close(Connection conn) {
        try {
            if (conn != null && conn.isClosed() == false) {
                conn.close();
                conn = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void close(Statement stmt, ResultSet result) {
        close(result);
        close(stmt);
    }

    public static void close(Connection conn, Statement stmt, ResultSet result) {
        close(result);
        close(stmt);
        close(conn);
    }
}
