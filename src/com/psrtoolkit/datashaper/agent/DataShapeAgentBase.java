/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Implementation for generic data shape generation behavior
 */
package com.psrtoolkit.datashaper.agent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.table.TableMgr;
import com.psrtoolkit.datashaper.util.CleanupUtil;

/**
 * Skeletal implementation for data shape generation behavior
 *
 * @author Wan
 *
 */
public abstract class DataShapeAgentBase implements DataShapeAgent {

    protected static Connection conn = null;
    protected IdMgr idMgr = null;
    protected TableMgr tableMgr = null;

    protected DataShapeAgentBase() {
    }

    /*
     * (non-Javadoc) @see
     * com.psrtoolkit.datashaper.agent.DataShapeAgent#connect(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public boolean connect(String connectionInfo, String userName, String password) {
        try {
            conn = DriverManager.getConnection(connectionInfo, userName, password);
            idMgr.setConnection(conn); //attached the conn to ID Manager
        } catch (Exception ex) {
            StringBuilder message = new StringBuilder();
            message.append("Error: failed to connect to db - ").append(connectionInfo);
            throw new DataShaperException(message.toString(), ex);
        }
        return true;
    }

    /*
     * (non-Javadoc) @see
     * com.psrtoolkit.datashaper.agent.DataShapeAgent#disconnect()
     */
    @Override
    public void disconnect() {
        if (idMgr != null) {
            idMgr.cleanup();
            idMgr = null;
        }
        if (tableMgr != null) {
            tableMgr.cleaup();
            tableMgr = null;
        }
        CleanupUtil.close(conn);
    }

    /*
     * (non-Javadoc) @see
     * com.psrtoolkit.datashaper.agent.DataShapeAgent#generate(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean generate() {
        return false;
    }

    /*
     * (non-Javadoc) @see
     * com.psrtoolkit.datashaper.agent.DataShapeAgent#setIdManager(com.psrtoolkit.datashaper.idmgr.IdMgr)
     */
    @Override
    public void setIdManager(IdMgr idMgr) {
        this.idMgr = idMgr;
    }

    /*
     * (non-Javadoc) @see
     * com.psrtoolkit.datashaper.agent.DataShapeAgent#setIdManager(com.psrtoolkit.datashaper.table.TableMgr)
     */
    @Override
    public void setTableMgr(TableMgr tableMgr) {
        this.tableMgr = tableMgr;
    }
}
