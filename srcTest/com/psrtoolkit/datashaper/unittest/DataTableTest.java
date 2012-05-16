/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unit test of DataTable API
 */
package com.psrtoolkit.datashaper.unittest;

import com.psrtoolkit.datashaper.enumeration.DbType;
import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.idmgr.IdMgrORCL;
import com.psrtoolkit.datashaper.idmgr.IdMgrMYSQL;
import com.psrtoolkit.datashaper.table.DataTable;
import com.psrtoolkit.datashaper.util.CleanupUtil;
import com.psrtoolkit.datashaper.util.OperationUtil;
import com.psrtoolkit.datashaper.util.StringUtil;

/**
 * @author Wan
 *
 */
public class DataTableTest {

    private Connection conn = null;
    private IdMgr idMgr = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
//        conn = TestUtil.getConnectionORCL();
//        idMgr = new IdMgrORCL(TestUtil.getSequenceNameORCL());
        conn = TestUtil.getConnection(DbType.MYSQL);
        idMgr = new IdMgrMYSQL("");
      
        idMgr.setConnection(conn);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        CleanupUtil.close(conn);
        idMgr = null;
    }

    /**
     * Test method for {@link com.psrtoolkit.datashaper.table.DataTable#populate(java.sql.Connection)}.
     */
    @Test
    public void testPopulate() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID) VALUES (?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal";
        String inputData = "'johndoe111@yahoo.com', 'John Doe', 'user', '0'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(1);
        table.setChunkSize(1);
        table.setIdMgr(idMgr);

        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }

    @Test
    public void testPopulateMoreInputData() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID) VALUES (?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal";
        String inputData1 = "'johndoe111@yahoo.com', 'John Doe1', 'user', '0'";
        String inputData2 = "'johndoe111@yahoo.com', 'John Doe2', 'user', '0'";
        String inputData3 = "'johndoe111@yahoo.com', 'John Doe3', 'user', '0'";
        String inputData4 = "'johndoe111@yahoo.com', 'John Doe4', 'user', '0'";
        String inputData5 = "'johndoe111@yahoo.com', 'John Doe5', 'user', '0'";
        String[][] inputDataArray = new String[5][0];
        inputDataArray[0] = StringUtil.split(inputData1, ",", "'");
        inputDataArray[1] = StringUtil.split(inputData2, ",", "'");
        inputDataArray[2] = StringUtil.split(inputData3, ",", "'");
        inputDataArray[3] = StringUtil.split(inputData4, ",", "'");
        inputDataArray[4] = StringUtil.split(inputData5, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(5);
        table.setChunkSize(5);

        table.setIdMgr(idMgr);
        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }

    @Test
    public void testPopulateSubstitutionSignatureMarker() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe${ds-default-signature}', 'user', '0', '${ds-default-signature} A software developer${ds-default-signature} from Sunnyvale, California. ${ds-default-signature}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(1);
        table.setChunkSize(1);

        table.setIdMgr(idMgr);
        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }

    @Test
    public void testPopulateSubstitutionDateMarker() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-date}', 'user', '0', '${ds-current-date} A software developer ${ds-current-date} from Sunnyvale, California. ${ds-current-date}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(1);
        table.setChunkSize(1);

        table.setIdMgr(idMgr);
        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }

    @Test
    public void testPopulateSubstitutionDateTimeMarker() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-datetime}', 'user', '0', '${ds-current-datetime} A software developer ${ds-current-datetime} from Sunnyvale, California. ${ds-current-datetime}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(1);
        table.setChunkSize(1);

        table.setIdMgr(idMgr);
        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }

    @Test
    public void testPopulateSubstitutionAllMarkers() {
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer ${ds-user-signature} from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");

        table.setSqlStmt(insertStmt);
        table.setUserSignature("johndoe111@yahoo.com");
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(1);
        table.setChunkSize(1);

        table.setIdMgr(idMgr);
        boolean result = table.populate(conn);
        OperationUtil.commit(conn);
        assertTrue(result);
        //TODO: add code to retrieve new record and verify the data.
    }
}
