/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unit test of table manager functionality
 */
package com.psrtoolkit.datashaper.unittest;

//import static org.junit.Assert.*;
import java.sql.Connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.psrtoolkit.datashaper.enumeration.FKeyAccessMethod;
import com.psrtoolkit.datashaper.enumeration.PopulationCalcMethod;
import com.psrtoolkit.datashaper.enumeration.TableRelationship;
import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.idmgr.IdMgrMYSQL;
import com.psrtoolkit.datashaper.table.DataTable;
import com.psrtoolkit.datashaper.table.FKeyProvider;
import com.psrtoolkit.datashaper.table.TableMgr;
import com.psrtoolkit.datashaper.util.CleanupUtil;
import com.psrtoolkit.datashaper.util.StringUtil;
import com.psrtoolkit.datashaper.enumeration.DbType;


/**
 * Unit test of table manager functionality
 *
 * @author Wan
 *
 */
public class TableMgrTest {

    private Connection conn = null;
    private IdMgr idMgr = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
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
     * Test method for {@link com.psrtoolkit.datashaper.table.TableMgr#populate(int, java.sql.Connection)}.
     */
    @Test
    public void testPopulate() {
        TableMgr tableMgr = new TableMgr();
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");
        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setPopulationCalcMethod(PopulationCalcMethod.BY_SPECIFIED_NUMBER);
        table.setMaxLimitInThisRun(5);
        table.setTableRelationship(TableRelationship.NONE);
        //table.setChunkSize(5);

        DataTable table2 = new DataTable("LOC");
        String insertStmt2 = "INSERT INTO actproj.LOC (NAME) VALUES (?)";
        String dataType2 = "java.lang.String";
        String inputData2 = "'Main Residence${ds-default-signature}'";
        String[][] inputDataArray2 = new String[1][0];
        inputDataArray2[0] = StringUtil.split(inputData2, ",", "'");
        String[] dataTypeList2 = StringUtil.split(dataType2, ",", "");
        table2.setSqlStmt(insertStmt2);
        table2.addInputDataList(inputDataArray2);
        table2.addDataTypeList(dataTypeList2);
        table2.setPopulationCalcMethod(PopulationCalcMethod.BY_SPECIFIED_NUMBER);
        table2.setMaxLimitInThisRun(10); //create 10 new records
        table.setTableRelationship(TableRelationship.NONE);
        //table2.setChunkSize(10);

        DataTable table3 = new DataTable("USR_LOC");
        String insertStmt3 = "INSERT INTO actproj.USR_LOC (LOC_ID, USR_ID) VALUES (?, ?)";
        String dataType3 = "java.math.BigDecimal, java.math.BigDecimal";
        String inputData3 = "'${ds-fkey-provider}=LOC', '${ds-fkey-provider}=USR'";
        String[][] inputDataArray3 = new String[1][0];
        inputDataArray3[0] = StringUtil.split(inputData3, ",", "'");
        String[] dataTypeList3 = StringUtil.split(dataType3, ",", "");
        table3.setSqlStmt(insertStmt3);
        table3.addInputDataList(inputDataArray3);
        table3.addDataTypeList(dataTypeList3);
        table3.setPopulationCalcMethod(PopulationCalcMethod.BY_DERIVATION);
        table3.setTableRelationship(TableRelationship.MANY2MANY);
        //table3.setMaxLimitInThisRun(50); 
        //table3.setChunkSize(10);

        FKeyProvider fKeyProvider = new FKeyProvider(StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + table.getName(), table);
        fKeyProvider.setPercentage(100);
        fKeyProvider.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);

        FKeyProvider fKeyProvider2 = new FKeyProvider(StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + table2.getName(), table2);
        fKeyProvider2.setPercentage(100);
        fKeyProvider2.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);

        table3.addFKeyProvider(fKeyProvider.getName(), fKeyProvider);
        table3.addFKeyProvider(fKeyProvider2.getName(), fKeyProvider2);

        table.setHasChildren(true);
        table2.setHasChildren(true);
        tableMgr.addTable(table);
        tableMgr.addTable(table2);
        tableMgr.addTable(table3);

        tableMgr.populate(conn, idMgr);
    }

    @Test
    public void testPopulatePostiveMoreInputData() {
        TableMgr tableMgr = new TableMgr();
        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String inputData2 = "'2johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String inputData3 = "'3johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String[][] inputDataArray = new String[3][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        inputDataArray[1] = StringUtil.split(inputData2, ",", "'");
        inputDataArray[2] = StringUtil.split(inputData3, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");
        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        //table.setHierarchyLevel(1);
        table.setMaxLimitInThisRun(50);
        table.setChunkSize(7);

        DataTable table2 = new DataTable("LOC");
        String insertStmt2 = "INSERT INTO actproj.LOC (NAME) VALUES (?)";
        String dataType2 = "java.lang.String";
        String inputData4 = "'Main Residence${ds-default-signature}'";
        String inputData5 = "'2Main Residence${ds-default-signature}'";
        String inputData6 = "'3Main Residence${ds-default-signature}'";
        String[][] inputDataArray2 = new String[3][0];
        inputDataArray2[0] = StringUtil.split(inputData4, ",", "'");
        inputDataArray2[1] = StringUtil.split(inputData5, ",", "'");
        inputDataArray2[2] = StringUtil.split(inputData6, ",", "'");
        String[] dataTypeList2 = StringUtil.split(dataType2, ",", "");
        table2.setSqlStmt(insertStmt2);
        table2.addInputDataList(inputDataArray2);
        table2.addDataTypeList(dataTypeList2);
        table2.setMaxLimitInThisRun(100); //create 10 new records
        table2.setChunkSize(9);

        tableMgr.addTable(table);
        tableMgr.addTable(table2);

        tableMgr.populate(conn, idMgr);
    }

    @Test
    public void testBuildDependencies() {
        TableMgr tableMgr = new TableMgr();

        DataTable table = new DataTable("USR");
        String insertStmt = "INSERT INTO actproj.USR (EMAIL, NAME, USR_TYPE, SYS_TENANT_ID, DESCRIPTION) VALUES (?, ?, ?, ?, ?)";
        String dataType = "java.lang.String, java.lang.String, java.lang.String, java.math.BigDecimal, java.lang.String";
        String inputData = "'johndoe111@yahoo.com', 'John Doe ${ds-current-record-id}', 'user', '0', 'A software developer from Sunnyvale, California. ${ds-current-date} ${ds-default-signature} ${ds-current-datetime}'";
        String[][] inputDataArray = new String[1][0];
        inputDataArray[0] = StringUtil.split(inputData, ",", "'");
        String[] dataTypeList = StringUtil.split(dataType, ",", "");
        table.setSqlStmt(insertStmt);
        table.addInputDataList(inputDataArray);
        table.addDataTypeList(dataTypeList);
        table.setMaxLimitInThisRun(5);
        table.setChunkSize(5);

        DataTable table2 = new DataTable("LOC");
        String insertStmt2 = "INSERT INTO actproj.LOC (NAME) VALUES (?)";
        String dataType2 = "java.lang.String";
        String inputData2 = "'Main Residence${ds-default-signature}'";
        String[][] inputDataArray2 = new String[1][0];
        inputDataArray2[0] = StringUtil.split(inputData2, ",", "'");
        String[] dataTypeList2 = StringUtil.split(dataType2, ",", "");
        table2.setSqlStmt(insertStmt2);
        table2.addInputDataList(inputDataArray2);
        table2.addDataTypeList(dataTypeList2);
        table2.setMaxLimitInThisRun(10); //create 10 new records
        table2.setChunkSize(10);

        DataTable table4 = new DataTable("LOC_BOGUS");
        String insertStmt4 = "INSERT INTO actproj.LOC (NAME) VALUES (?)";
        String dataType4 = "java.lang.String";
        String inputData4 = "'Main Residence${ds-default-signature}'";
        String[][] inputDataArray4 = new String[1][0];
        inputDataArray4[0] = StringUtil.split(inputData4, ",", "'");
        String[] dataTypeList4 = StringUtil.split(dataType4, ",", "");
        table4.setSqlStmt(insertStmt4);
        table4.addInputDataList(inputDataArray4);
        table4.addDataTypeList(dataTypeList4);
        table4.setMaxLimitInThisRun(10); //create 10 new records
        table4.setChunkSize(10);

        DataTable table3 = new DataTable("USR_LOC");
        String insertStmt3 = "INSERT INTO actproj.USR_LOC (LOC_ID, USR_ID) VALUES (?, ?)";
        String dataType3 = "java.math.BigDecimal, java.math.BigDecimal";
        String inputData3 = "'${ds-fkey-provider}=LOC', '${ds-fkey-provider}=USR'";
        String[][] inputDataArray3 = new String[1][0];
        inputDataArray3[0] = StringUtil.split(inputData3, ",", "'");
        String[] dataTypeList3 = StringUtil.split(dataType3, ",", "");
        table3.setSqlStmt(insertStmt3);
        table3.addInputDataList(inputDataArray3);
        table3.addDataTypeList(dataTypeList3);
//		table3.setMaxLimitInThisRun(50); 
//		table3.setChunkSize(10);
        table3.setPopulationCalcMethod(PopulationCalcMethod.BY_DERIVATION);
        table3.setTableRelationship(TableRelationship.MANY2MANY);

        FKeyProvider fKeyProvider = new FKeyProvider(StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + table.getName(), table);
        fKeyProvider.setPercentage(100);
        fKeyProvider.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);

        FKeyProvider fKeyProvider2 = new FKeyProvider(StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + table2.getName(), table2);
        fKeyProvider2.setPercentage(100);
        fKeyProvider2.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);

        table3.addFKeyProvider(fKeyProvider.getName(), fKeyProvider);
        table3.addFKeyProvider(fKeyProvider2.getName(), fKeyProvider2);

        FKeyProvider fKeyProvider4 = new FKeyProvider(StringUtil.DS_FOREIGN_KEY_PROVIDER + "=" + table4.getName(), table4);
        fKeyProvider4.setPercentage(100);
        fKeyProvider4.setFKeyAccessMethod(FKeyAccessMethod.SEQUENTIAL);

        table.addFKeyProvider(fKeyProvider4.getName(), fKeyProvider4);

        table.setHasChildren(true);
        table2.setHasChildren(true);
        table4.setHasChildren(true);

        tableMgr.addTable(table);
        tableMgr.addTable(table2);
        tableMgr.addTable(table3);
        tableMgr.addTable(table4);

        tableMgr.populate(conn, idMgr);
    }
}
