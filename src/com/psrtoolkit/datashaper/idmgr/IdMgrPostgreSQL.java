/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.idmgr;

/**
 * @author Wan
 *
 */
public class IdMgrPostgreSQL extends IdMgrBase {

    public IdMgrPostgreSQL(String sequenceName) {
        this.sequenceName = sequenceName;
        selectCurvalStr = "SELECT currval ('" + sequenceName + "')";
        selectNextvalStr = "SELECT nextval ('" + sequenceName + "')";
    }
}
