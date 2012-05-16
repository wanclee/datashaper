/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Test the factory in creating the right agent based on db type
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.psrtoolkit.datashaper.agent.DataShapeAgent;
import com.psrtoolkit.datashaper.agent.DataShapeAgentFactory;
import com.psrtoolkit.datashaper.enumeration.DbType;
import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * @author Wan
 *
 */
public class DataShapeAgentFactoryTest {

    @Test
    public void testCreateAgent() {
        DataShapeAgent agent;

        agent = DataShapeAgentFactory.createAgent(DbType.ORCL, "ACTPROJ");
        assertTrue(agent != null);
    }

    @Test
    public void testCreateAgentNagative() {
        DataShapeAgent agent;

        try {
            agent = DataShapeAgentFactory.createAgent(null, "ACTPROJ");
            assertFalse(agent != null);
        } catch (DataShaperException dsex) {
            assertTrue(true);
        }

        try {
            agent = DataShapeAgentFactory.createAgent(DbType.SYBASE, "ACTPROJ");
            assertFalse(agent != null);
        } catch (DataShaperException dsex) {
            assertTrue(true);
        }

    }
}
