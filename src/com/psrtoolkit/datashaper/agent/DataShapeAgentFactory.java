/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Factory for data shape agent creation.
 */
package com.psrtoolkit.datashaper.agent;

import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.idmgr.IdMgrDB2;
import com.psrtoolkit.datashaper.idmgr.IdMgrMSSQLServer;
import com.psrtoolkit.datashaper.idmgr.IdMgrMYSQL;
import com.psrtoolkit.datashaper.idmgr.IdMgrORCL;
import com.psrtoolkit.datashaper.idmgr.IdMgrPostgreSQL;

/**
 * Factory for data shape agent creation.
 *
 * @author Wan
 *
 */
public class DataShapeAgentFactory {

    private DataShapeAgentFactory() {
    }

    public static DataShapeAgent createAgent(DbType dbType, String sequenceName) {
        DataShapeAgent agent = null;

        if (dbType == null) {
            throw new DataShaperException("Error: dbType cannot be null");
        }

        switch (dbType) {
            case DB2:
                agent = new DataShapeAgentDB2();
                agent.setIdManager(new IdMgrDB2(sequenceName));
                break;
            case MYSQL:
                agent = new DataShapeAgentMYSQL();
                agent.setIdManager(new IdMgrMYSQL(sequenceName));
                break;
            case MSSQLSERVER:
                agent = new DataShapeAgentMSSQLServer();
                agent.setIdManager(new IdMgrMSSQLServer(sequenceName));
                break;
            case ORCL:
                agent = new DataShapeAgentORCL();
                agent.setIdManager(new IdMgrORCL(sequenceName));
                break;
            case POSTGRESQL:
                agent = new DataShapeAgentPostgreSQL();
                agent.setIdManager(new IdMgrPostgreSQL(sequenceName));
                break;
            default:
                throw new DataShaperException("Error: unsupported or not yet supported db type - " + dbType);

        }
        return agent;
    }
}
