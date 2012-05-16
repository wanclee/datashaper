/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Implementation of basic behavior for Id Manager
 */
package com.psrtoolkit.datashaper.idmgr;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.util.CleanupUtil;

/**
 * Implementation of basic behavior for Id Manager
 *
 * @author Wan
 *
 */
public abstract class IdMgrBase implements IdMgr {

    protected Connection conn = null;
    protected Statement stmt = null; //for executing query against DUAL to obtain id values
    protected CallableStatement callableStmt = null; //for calling PL/SQL package, stored procedures and functions
    protected long curId = 0L;
    protected long curMaxId = 0L;
    protected String sequenceName = ""; //sequence generator name
    protected String selectCurvalStr = "";
    protected String selectNextvalStr = "";
    DynamicLongArray longArrAlternative = new DynamicLongArray();

    protected IdMgrBase() {
    }

    @Override
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Connection getConnection() {
        return conn;
    }

    @Override
    public long getNextId() {
        try {
            if (conn == null) {
                throw new DataShaperException("Error: connection can not be null in ID Manager \n");
            }

            if (stmt == null) {
                stmt = conn.createStatement();
                curMaxId = getCurMaxIdFromDB();
                //System.out.println("The initial curMaxId is: " + curMaxId + "\n");

                curId = getCurIdFromDB();
                //System.out.println("The initial curId is: " + curId + "\n");
            }

            if (curId == curMaxId) {
                //System.out.println("The curId == curMaxId and is: " + curMaxId + "\n");
                curMaxId = getCurMaxIdFromDB();
                curId++;
            } else if (curId < curMaxId) {
                curId++;
            } else {
                curId = 0L;
                curMaxId = 0L;
                throw new DataShaperException("Error: invalid state - current ID is greater than current maximum ID in ID Manager \n");
            }

            return curId;
        } catch (Exception ex) {
            throw new DataShaperException("Error: unable to get next id\n", ex);
        }

    }

    @Override
    public String getNextAlphaNumericId() {
        throw new DataShaperException("Pre-fetching alphanumeric id is not supported");
    }

    @Override
    public long getCurId() {
        return curId;
    }

    public String getCurAlphaNumericId() {
        return "";
    }

    @Override
    public boolean hasSequenceInConfig() {
        return (sequenceName.isEmpty() == false);
    }

    @Override
    public void cleanup() {
        CleanupUtil.close(stmt);
        CleanupUtil.close(callableStmt);
        conn = null;
    }

    protected long getCurIdFromDB() {
        return getId(selectCurvalStr);
    }

    protected long getCurMaxIdFromDB() {
        return getId(selectNextvalStr);
    }

    protected long getId(String queryStr) {
        ResultSet result = null;
        long id = 0L;

        try {
            //ensure thread safety in obtaining id value
            synchronized (this) {
                result = stmt.executeQuery(queryStr.toString());
                while (result.next()) {
                    id = Long.valueOf(result.getString(1));
                }
            }
            //TODO: not sure we want to close cursor every time a result is retrieved.
            result.close();
            result = null;
            return id;
        } catch (Exception ex) {
            StringBuilder message = new StringBuilder();
            message.append("Error: unable to execute query - ").append(queryStr).append("\n");
            throw new DataShaperException(message.toString(), ex);
        } finally {
            //TODO: not sure we want to do the clean up at this level;
            //the result object could already be invalid
            CleanupUtil.close(result);
        }
    }

    /**
     * Alternative way to get new block of IDs from the sequence. <p> Caution:
     * It requires a PL/SQL package and a function to be created on the db and
     * the right privilege grant to the user to execute the function. <p>
     * Caution: It relies on java.util.ARRAY structure; the returned IDs are
     * BIGDECIMAL objects, and too many of them can result in short-life objects
     * effect on garbage collection behavior. <p> Caution: use this approach
     * only if the INCREMENT BY amount is 1 and user has no privilege to alter
     * it through SQL call to increase the increment amount.
     *
     * @param sequence
     * @param blockSize
     * @return
     */
    protected DynamicLongArray prefetchIdsBeforeInsert(String sequence, int blockSize) {
        return null;
    }
}
