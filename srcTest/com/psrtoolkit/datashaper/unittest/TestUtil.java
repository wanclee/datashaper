/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Utility for managing configuration parameters for test setup purposes
 */
package com.psrtoolkit.datashaper.unittest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.config.ConfigReader;
//import com.psrtoolkit.datashaper.license.LicenseMgr;
import com.psrtoolkit.datashaper.table.TableMgr;
import com.psrtoolkit.datashaper.util.CleanupUtil;
import com.psrtoolkit.datashaper.util.StringUtil;
import com.psrtoolkit.datashaper.enumeration.DbType;

/**
 * @author Wan
 *
 */
public class TestUtil {

    final static String connectionInfoORCL = "jdbc:oracle:thin:@localhost:1521:xe";
    final static String connectionInfoPostgreSQL = "";
//	final static String userName = "system";
//	final static String password = "oracle";
    final static String userNameORCL = "ACTPROJ";
    final static String passwordORCL = "ACTPROJ";
    final static String userNamePostgreSQL = "actproj";
    final static String passwordPostgreSQL = "actproj";
    final static String sequenceNameORCL = "ACTPROJ";
    final static String sequenceNamePostgreSQL = "ACTPROJ";
    final static String sequenceNameDB2 = "ACTPROJ";
    final static String sequenceNameMSSQLServer = "ACTPROJ";
    final static String connectionInfoMySql = "jdbc:mysql://localhost:3306/actproj";

    private TestUtil() {
    }

    public static Connection getConnection(DbType dbType) {
        Connection conn = null;
        try {
            if (dbType == DbType.MYSQL) {
                conn = DriverManager.getConnection(connectionInfoMySql, "actproj", "actproj");
            } else {
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn;
    }
    
    public static Connection getConnectionORCL() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionInfoORCL, userNameORCL, passwordORCL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn;
    }

    public static Connection getConnectionPostgreSQL() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionInfoPostgreSQL, userNamePostgreSQL, passwordPostgreSQL);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return conn;
    }

    public static String getSequenceNameORCL() {
        return sequenceNameORCL;
    }

    public static String getSequenceNamePostgreSQL() {
        return sequenceNamePostgreSQL;
    }

    public static String getSequenceNameDB2() {
        return sequenceNameDB2;
    }

    public static String getSequenceNameMSSQLServer() {
        return sequenceNameMSSQLServer;
    }

    public static void cleanup(Connection conn) {
        CleanupUtil.close(conn, (Statement) null, (ResultSet) null);
    }

    public static boolean testDataShape(String configurationFile) {

        Properties defaultLicense = new Properties();
//		if (LicenseMgr.verifyLicense(defaultLicense, StringUtil.FILE_DEFAULT_LICENSE, StringUtil.VENDOR_NAME) == false)
//			System.exit(1);

        Properties license = new Properties();
//		if (LicenseMgr.verifyLicense(license, StringUtil.FILE_DATASHAPER_LICENSE, StringUtil.VENDOR_NAME) == false)
//			System.exit(1);

        TableMgr tableMgr = new TableMgr(false, defaultLicense);
        //TableMgr tableMgr = new TableMgr();
        ConfigReader.read(configurationFile, tableMgr);
        DataShapeAgent agent = DataShapeAgentFactory.createAgent(ConfigReader.getDbType(),
                ConfigReader.getSharedSequence());


        String connectionInfo = ConfigReader.getConnectionStr();
        String userName = "actproj";
        String password = "actproj";

        Boolean result = agent.connect(connectionInfo, userName, password);
        agent.setTableMgr(tableMgr);
        result = agent.generate();
        return result;
    }
}
