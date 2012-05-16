/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
//import com.psrtoolkit.datashaper.license.LicenseMgr;
import com.psrtoolkit.datashaper.table.TableMgr;
import com.psrtoolkit.datashaper.util.StringUtil;


/*
 * Junky Statement stmt = conn.createStatement(); ResultSet result =
 * stmt.executeQuery("SELECT * FROM DUAL"); while (result.next())
 * System.out.println("The output is: " + result.getString(1) + "\n");
 * stmt.close(); return true;
 *
 */
public class DataShapeAgentORCLTest {

    final static String connectionInfo = "jdbc:oracle:thin:@localhost:1521:xe";
//	final static String userName = "system";
//	final static String password = "oracle";
    final static String userName = "actproj";
    final static String password = "actproj";
    final static String sequence = TestUtil.getSequenceNameORCL();
    final static String dataShapeDefLocation = "samples/oracle/";
    DataShapeAgent agent = null;

    @Test
    public void testConnect() {
        //positive test
        agent = DataShapeAgentFactory.createAgent(DbType.ORCL, sequence);
        Boolean result = agent.connect(connectionInfo, userName, password);
        assertTrue(result);
        agent.disconnect();
        assertTrue(true);

    }

    @Test
    public void testConnectNegative() {
        //negative test - wrong connection info
        agent = DataShapeAgentFactory.createAgent(DbType.ORCL, sequence);
        String connectionStr = connectionInfo + "junk";
        try {
            Boolean result1 = agent.connect(connectionStr, userName, password);
            assertFalse(result1); //we don't expect success
        } catch (DataShaperException dsex) {
            assertTrue(true);
        }
        agent.disconnect();
        assertTrue(true);
    }

    @Test
    public void testGenerateWithDataShapeConfigSimpleCreateCustomers() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleSimpleCreateCustomers.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigManyToManyRelationship() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleMany2ManyRelationship.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2ManyRelationship() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleOne2ManyRelationship.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2OneRelationship() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleOne2OneRelationship.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigSeedData() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleSeedData.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigDenormColumnAndPickListColumn() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleDenormColumnAndPickListColumn.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseQuery() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseQuery.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCasesDataPreparation() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCasesDataPreparation.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1SmallScale() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCase1SmallScale.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase2SmallScale() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCase2SmallScale.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase3SmallScale() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCase3SmallScale.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1and2and3Combined() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCase1and2and3Combined.xml"));
    }

    //change the schema to aplhanumeric based ids by running actproj-orcl-alphanumeric-rowid-implicit-sequence-with-trigger.ddl
    @Test
    public void testGenerateWithDataShapeConfigAlphaNumericRowIdSimulatedWithSequenceAndTrigger() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleAlphaNumericRowIdSimulatedWithSequenceAndTrigger.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1and2and3CombinedAndUseAlphaNumericIds() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "ORCLExampleUseCase1and2and3CombinedAndUseAlphaNumericIds.xml"));
    }
}
