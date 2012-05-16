/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Implementation class for MYSQL-specific getNextId function
 */
package com.psrtoolkit.datashaper.idmgr;

/**
 * Implementation class for MYSQL-specific getNextId function MySql doesn't have
 * sequence generator; however, it could be simulating by using a table and two
 * PL/SQL functions - nextval and currval
 *
 * @author Wan
 *
 */
public class IdMgrMYSQL extends IdMgrBase {

    public IdMgrMYSQL(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    @Override
    public long getNextId() {
        return 0L;
    }
}
