/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Unit testing for IdMgr implementation classes
 */
package com.psrtoolkit.datashaper.unittest;

import static org.junit.Assert.*;

import java.sql.Connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.idmgr.IdMgr;
import com.psrtoolkit.datashaper.idmgr.IdMgrORCL;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;

/**
 * @author Wan
 *
 */
public class IdMgrORCLTest {

    Connection conn = null;
    String sequence = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        conn = TestUtil.getConnectionORCL();
        sequence = TestUtil.getSequenceNameORCL();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        TestUtil.cleanup(conn);
    }

    /**
     * Test method for {@link com.psrtoolkit.datashaper.idmgr.IdMgrBase#setConnection(java.sql.Connection)}.
     */
    @Test
    public void testSetConnection() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        idmgr.setConnection(conn);
        id = idmgr.getNextId();
        assertTrue(id > 0L);
    }

    @Test
    public void testSetConnectionNagative() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        idmgr.setConnection(null);
        try {
            id = idmgr.getNextId();
            assertFalse(id > 0L); //don't expect success
        } catch (DataShaperException ex) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link com.psrtoolkit.datashaper.idmgr.IdMgrBase#getNextId()}.
     */
    @Test
    public void testGetNextId() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        idmgr.setConnection(conn);
        id = idmgr.getNextId();
        assertTrue(id > 0L);
    }

    @Test
    public void testGetNextIdTwice() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        idmgr.setConnection(conn);
        id = idmgr.getNextId();
        assertTrue(id > 0L);
        long id2 = 0L;
        id2 = idmgr.getNextId();
        assertTrue(id2 > id);
        assertTrue((id2 - id) == 1);
    }

    @Test
    public void testGetNextIdFiveHundredTimes() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        idmgr.setConnection(conn);
        id = idmgr.getNextId();
        assertTrue(id > 0L);
        long id2 = 0L;
        for (int i = 0; i < 500; i++) {
            id2 = idmgr.getNextId();
        }
        assertTrue(id2 > id);
        assertTrue((id2 - id) == 500);
    }

    @Test
    public void testGetNextIdNagative() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        long id = 0L;
        try {
            id = idmgr.getNextId();
            assertFalse(id > 0L); //don't expect success
        } catch (DataShaperException ex) {
            assertTrue(true);
        }
    }

    /**
     * Test method for {@link com.psrtoolkit.datashaper.idmgr.IdMgrBase#cleanup()}.
     */
    @Test
    public void testCleanup() {
        IdMgr idmgr = new IdMgrORCL(sequence);
        idmgr.setConnection(conn);
        idmgr.cleanup();
        long id = 0L;
        try {
            id = idmgr.getNextId();
            assertFalse(id > 0L); //don't expect success
        } catch (DataShaperException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testPrefetchIdsBeforeInsert() {
        try {
            IdMgrORCL idMgr = new IdMgrORCL(sequence);
            idMgr.setConnection(conn);
            DynamicLongArray longArr = idMgr.prefetchIdsBySequenceBeforeInsert(sequence, 100, 5);
            assertTrue(longArr != null);
            assertTrue(longArr.lastpos() > 0);
        } catch (Exception ex) {
            ex.printStackTrace();
            assertFalse(true);
        }
    }
}
