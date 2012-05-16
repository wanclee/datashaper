/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Interface definition for general behavior of data shape agents
 */
package com.psrtoolkit.datashaper.agent;

import java.util.Properties;

import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.table.TableMgr;

/**
 * Interface definition for general behavior of data shape agents
 *
 * @author Wan
 */
public interface DataShapeAgent {

    /**
     * Connect to the target database based on connection info, user name and
     * password
     *
     * @param connectionInfo connection string for the target database; e.g.
     * "jdbc:oracle:thin:@localhost:1521:xe"
     * @param userName user name
     * @param password password
     * @return true if connection is successful; false otherwise
     * @exception throws DataShaperException runtime exception
     */
    public boolean connect(String connectionInfo, String userName, String password);

    /**
     * Disconnect from the database
     */
    public void disconnect();

    /**
     * Generate the data based on configuration of data shape and the database
     * schema
     *
     * @return true if the operation is successful; otherwise false
     * @exception throws DataShaperException runtime exception
     */
    public boolean generate();

    /**
     * Associate the ID manager with the agent. Agent uses the manager to obtain
     * the next id or current id
     *
     * @param idMgr ID Manager
     */
    public void setIdManager(IdMgr idMgr);

    /**
     * Associate the table manager with the agent. The table manager manages all
     * table operations.
     *
     * @param tablMgr
     */
    public void setTableMgr(TableMgr tablMgr);
}
