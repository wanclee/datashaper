/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.table.TableMgr;

public class DataShapeAgentDB2Test {

    final static String connectionInfo = "jdbc:db2://localhost:50000/actproj";
    final static String userName = "actproj";
    final static String password = "actproj";
    final static String sequence = TestUtil.getSequenceNameDB2();
    final static String dataShapeDefLocation = "samples/db2/";
    DataShapeAgent agent = null;

    @Test
    public void testConnect() {
        //positive test
        agent = DataShapeAgentFactory.createAgent(DbType.DB2, sequence);
        Boolean result = agent.connect(connectionInfo, userName, password);
        assertTrue(result);
        agent.disconnect();
        assertTrue(true);

    }

    @Test
    public void testConnectNegative() {
        //negative test - wrong connection info
        agent = DataShapeAgentFactory.createAgent(DbType.DB2, sequence);
        String connectionStr = connectionInfo + "junk";
        try {
            Boolean result1 = agent.connect(connectionStr, userName, password);
            assertFalse(result1); //we don't expect success
        } catch (DataShaperException dsex) {
            //dsex.printStackTrace();
            assertTrue(true);
        }
        agent.disconnect();
        assertTrue(true);
    }

    @Test
    public void testGenerateWithDataShapeSimpleCreateCustomers() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleSimpleCreateCustomers.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigMany2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleMany2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleOne2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2OneRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleOne2OneRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseQuery() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseQuery.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigSeedData() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleSeedData.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigDenormColumnAndPickListColumn() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleDenormColumnAndPickListColumn.xml"));
    }

    @Test
    public void testGenerateWithDataShapeUseCasesDataPreparation() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseCasesDataPreparation.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeUseCase1SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseCase1SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeUseCase2SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseCase2SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeUseCase3SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseCase3SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeUseCase1and2and3Combined() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleUseCase1and2and3Combined.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    //Before running this test, please create the schema with this DDL: actproj-db2-identity-column-option.ddl
    @Test
    public void testGenerateWithDataShapeConfigMany2ManyRelationshipUseIdentityColumn() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "DB2ExampleMany2ManyRelationshipUseIdentityColumn.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
