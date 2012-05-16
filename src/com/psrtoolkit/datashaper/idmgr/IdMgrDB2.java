/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Implementation class for DB2-specific getNextId function
 */
package com.psrtoolkit.datashaper.idmgr;

/**
 * Implementation class for DB2-specific getNextId function
 *
 * @author Wan
 *
 */
public class IdMgrDB2 extends IdMgrBase {

    public IdMgrDB2(String sequenceName) {
        this.sequenceName = sequenceName;
        selectCurvalStr = "SELECT previous value for " + sequenceName + " FROM SYSIBM.SYSDUMMY1";
        selectNextvalStr = "SELECT next value for " + sequenceName + " FROM SYSIBM.SYSDUMMY1";
    }
}
