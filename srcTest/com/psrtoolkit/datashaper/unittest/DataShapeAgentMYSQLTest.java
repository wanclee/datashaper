/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.config.ConfigReader;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.table.TableMgr;

/**
 * @author Wan
 *
 */
public class DataShapeAgentMYSQLTest {

    final static String connectionInfo = "jdbc:mysql://localhost:3306/actproj";
    final static String userName = "actproj";
    final static String password = "actproj";
    final static String sequenceName = "";
    final static String dataShapeDefLocation = "samples/mysql/";
    DataShapeAgent agent = null;

    @Test
    public void testConnect() {
        //positive test
        String connectionInfoMySql = "jdbc:mysql://localhost:3306/actproj";

        agent = DataShapeAgentFactory.createAgent(DbType.MYSQL, sequenceName);
        Boolean result = agent.connect(connectionInfoMySql, userName, password);
        assertTrue(result);
        agent.disconnect();
        assertTrue(true);

    }

    @Test
    public void testGenerateWithDataShapeConfigSimpleCreateCustomers() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleSimpleCreateCustomers.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigMany2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleMany2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2ManyRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleOne2ManyRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigOne2OneRelationship() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleOne2OneRelationship.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseQuery() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseQuery.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigSeedData() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleSeedData.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigDenormColumnAndPickListColumn() {
        assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleDenormColumnAndPickListColumn.xml"));
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCasesDataPreparation() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseCasesDataPreparation.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseCase1SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase2SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseCase2SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase3SmallScale() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseCase3SmallScale.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

    }

    @Test
    public void testGenerateWithDataShapeConfigUseCase1and2and3Combined() {
        try {
            assertTrue(TestUtil.testDataShape(dataShapeDefLocation + "MYSQLExampleUseCase1and2and3Combined.xml"));
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }

    }
}
