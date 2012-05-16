/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.agent.DataShapeAgentMSSQLServer;
import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.table.TableMgr;

/**
 * @author Wan
 *
 */
public class DataShapeAgentMSSQLServerTest extends DataShapeAgentMSSQLServer {

    final static String connectionInfo = "jdbc:sqlserver://localhost:1433;DatabaseName=actproj";
    final static String userName = "actproj";
    final static String password = "actproj";
    final static String sequence = TestUtil.getSequenceNameMSSQLServer();
    final static String dataShapeDefLocation = "samples/mssqlserver/";
    DataShapeAgent agent = null;

    @Test
    public void testConnect() {
        //positive test
        agent = DataShapeAgentFactory.createAgent(DbType.MSSQLSERVER, sequence);
        Boolean result = agent.connect(connectionInfo, userName, password);
        assertTrue(result);
        agent.disconnect();
        assertTrue(true);

    }

    @Test
    public void testConnectNegative() {
        //negative test - wrong connection info
        agent = DataShapeAgentFactory.createAgent(DbType.MSSQLSERVER, sequence);
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
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleSimpleCreateCustomers.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigMany2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleMany2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleOne2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2OneRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleOne2OneRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigSeedData() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleSeedData.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseQuery() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseQuery.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigDenormColumnAndPickListColumn() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleDenormColumnAndPickListColumn.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCasesDataPreparation() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseCasesDataPreparation.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseCase1SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase2SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseCase2SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase3SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseCase3SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1and2and3Combined() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MSSQLServerExampleUseCase1and2and3Combined.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
