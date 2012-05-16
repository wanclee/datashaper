/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Implementation class for ORCL-specific getNextId function *
 */
package com.psrtoolkit.datashaper.idmgr;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;

import com.psrtoolkit.datashaper.constants.NumericLimit;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.util.CleanupUtil;

import oracle.jdbc.OracleTypes;

/**
 * Implementation class for ORCL-specific getNextId function <p> Assumption:
 * there exists a global sequence (for all tables) in the schema in the database
 * the agent is connecting to. <p> TODO: support PL/SQL package calls to obtain
 * ids (if the package name and input parameters could be obtained from the
 * schema or customer) <p> TODO: support per-table Sequence Generator (if the
 * name could be obtained from the schema)
 *
 * @author Wan
 *
 */
public class IdMgrORCL extends IdMgrBase {

    boolean bAlternativePrefetch = false;
    int curIdIndexAlternative = -1;
    int nextIdIndexAlternative = -1;
    int sequenceIncrementSize = 0;

    public IdMgrORCL(String sequenceName) {
        this.sequenceName = sequenceName;
        selectCurvalStr = "SELECT " + sequenceName + ".currval from dual";
        selectNextvalStr = "SELECT " + sequenceName + ".nextval from dual";
    }

    @Override
    public long getNextId() {
        try {
            if (bAlternativePrefetch == true) {
                return getNextIdAlternative();
            }

            if (conn == null) {
                throw new DataShaperException("Error: connection can not be null in ID Manager \n");
            }

            if (stmt == null) {
                stmt = conn.createStatement();

                //Note: To avoid java.sql.SQLException: ORA-08002: sequence ACTPROJ.CURRVAL is not yet defined in this session,
                //start by calling NEXTVAL.
                curMaxId = getCurMaxIdFromDB();
                //System.out.println("The initial curMaxId is: " + curMaxId + "\n");

                curId = getCurIdFromDB();
                //System.out.println("The initial curId is: " + curId + "\n");

                //Note: the first NEXTVAL call above is wasted, but that is OK.
                curMaxId = getCurMaxIdFromDB();
                //System.out.println("The initial curMaxId is: " + curMaxId + "\n");

                //TODO: decide if it's worth to support pre-fetching of new block IDs when sequence increment is too small
				/*
                 * long diff = curMaxId - curId; sequenceIncrementSize = new
                 * Long(diff).intValue(); if ((curMaxId - curId) <
                 * NumericLimit.ONEHUNDRED) {
                 * prefetchIdsBySequenceBeforeInsert(sequenceName,
                 * NumericLimit.ONEHUNDRED, sequenceIncrementSize);
                 * bAlternativePrefetch = true; return getNextIdAlternative();
				}
                 */
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
    public long getCurId() {
        if (bAlternativePrefetch == true) {
            return getCurIdAlternative();
        }
        return curId;
    }

    @Override
    public void cleanup() {
        CleanupUtil.close(stmt);
        CleanupUtil.close(callableStmt);
        conn = null;
    }

    @Override
    protected long getCurIdFromDB() {
        return getId(selectCurvalStr);
    }

    @Override
    protected long getCurMaxIdFromDB() {
        return getId(selectNextvalStr);
    }

    @Override
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
    public DynamicLongArray prefetchIdsBySequenceBeforeInsert(String sequence,
            int blockSize, int incSize) {
        ResultSet result = null;
        try {
            if (incSize > blockSize) {
                throw new DataShaperException("Error: sequence increment size cannot be greater than block size");
            }

            longArrAlternative.clear();
            if (callableStmt == null) {
                callableStmt = conn.prepareCall("begin ?:=pkg_datashaper.getIdBlockBySequence(?,?,?);end;");
                callableStmt.registerOutParameter(1, OracleTypes.ARRAY, "DATASHAPEIDARRAY");
                callableStmt.setString(2, sequence);
                callableStmt.setInt(3, blockSize);
                callableStmt.setInt(4, incSize);
            }
            callableStmt.executeUpdate();
            Array idArr = callableStmt.getArray(1);
            if (idArr == null) {
                throw new DataShaperException("Error: array block is null");
            }

            BigDecimal[] values = (BigDecimal[]) idArr.getArray();
            for (int i = 0; i < values.length; i++) {
                longArrAlternative.add(values[i].longValue());
                System.out.println("Id returned from calling pkg_datashaper.getIdBlockBySequence is: " + "index " + i + " = " + values[i]);
            }
            return longArrAlternative;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new DataShaperException("Error: failed to pre-fetch new block of IDs from the database");
        } finally {
            CleanupUtil.close(result);
        }
    }

    public long getNextIdAlternative() {
        if (nextIdIndexAlternative == -1) {
            nextIdIndexAlternative = 0;
            curIdIndexAlternative = 0;
        } else {
            curIdIndexAlternative = nextIdIndexAlternative;
            nextIdIndexAlternative++;
        }
        if (longArrAlternative.lastpos() > 0 && nextIdIndexAlternative == longArrAlternative.lastpos()) {
            prefetchIdsBySequenceBeforeInsert(sequenceName, NumericLimit.ONEHUNDRED, sequenceIncrementSize);
            nextIdIndexAlternative = 0;
            curIdIndexAlternative = 0;
        }
        return longArrAlternative.get(nextIdIndexAlternative);
    }

    public long getCurIdAlternative() {
        return longArrAlternative.get(curIdIndexAlternative);
    }
}
