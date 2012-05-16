/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.agent;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * @author Wan
 *
 */
public class DataShapeAgentMSSQLServer extends DataShapeAgentBase {

    public DataShapeAgentMSSQLServer() {
    }

    @Override
    public boolean generate() {
        if (tableMgr == null) {
            throw new DataShaperException("Error: table manager cannot be null");
        }
        tableMgr.populate(conn, idMgr);
        return true;
    }
}
