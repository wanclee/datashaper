/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Utility for supporting batch operations
 */
package com.psrtoolkit.datashaper.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * Utility for supporting batch operations
 *
 * @author Wan
 *
 */
public class OperationUtil {

    private OperationUtil() {
    }

    public static boolean areBatchUpdatesSupported(Connection conn) {
        try {
            DatabaseMetaData dbMData = conn.getMetaData();
            return dbMData.supportsBatchUpdates();
        } catch (SQLException e) {
            throw new DataShaperException("Error: failed in calling areBatchUpdatesSupported");
        }
    }

    public static void commit(Connection conn) {
        try {
            conn.commit();
        } catch (Exception ex) {
            //ex.printStackTrace();
            throw new DataShaperException("Error: failed to commit transaction", ex);
        }
    }
}
