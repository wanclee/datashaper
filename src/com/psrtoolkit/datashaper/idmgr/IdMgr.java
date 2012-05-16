/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Interface definition for IdMgr
 */
package com.psrtoolkit.datashaper.idmgr;

import java.sql.Connection;

/**
 * Interface definition for IdMgr
 *
 * @author Wan
 *
 */
public interface IdMgr {

    public void setConnection(Connection conn);

    public Connection getConnection();

    public long getNextId();

    public String getNextAlphaNumericId();

    public long getCurId();

    public String getCurAlphaNumericId();

    public boolean hasSequenceInConfig();

    public void cleanup();
}
