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

import org.junit.Test;

import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;

/**
 * Unit test of DynamicLongArrayTest implementation
 *
 * @author Wan
 *
 */
public class DynamicLongArrayTest {

    @Test
    public void testIntialization() {
        DynamicLongArray arr = new DynamicLongArray();
        assertTrue(arr.length() == 100);
        assertTrue(arr.lastpos() == 0);

        DynamicLongArray arr2 = new DynamicLongArray(200);
        assertTrue(arr2.length() == 200);
        arr2.add(999L);
        assertTrue(arr2.lastpos() == 1);
        assertTrue(arr2.get(0) == 999L);
    }

    @Test
    public void testIntializationNegative() {
        try {
            DynamicLongArray arr = new DynamicLongArray(-300);
            assertTrue(false); //don't expect to arrive here
            assertTrue(arr.length() > 0);
        } catch (DataShaperException ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testGet() {
        DynamicLongArray arr = new DynamicLongArray(200);
        for (int i = 0; i < arr.length(); i++) {
            arr.add(i + 399);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        for (int k = 0; k < arr.length(); k++) {
            assertTrue(arr.get(k) == (k + 399));
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);
    }

    @Test
    public void testGetNegative() {
        DynamicLongArray arr = new DynamicLongArray(200);
        for (int i = 0; i < arr.length(); i++) {
            arr.add(i + 399);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        try {
            arr.get(201);
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(true);
        }

        try {
            arr.get(-888);
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assertTrue(true);
        }

    }

    @Test
    public void testSet() {
        DynamicLongArray arr = new DynamicLongArray(200);
        for (int i = 0; i < arr.length(); i++) {
            arr.set(i, i + 399);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);
        int len = arr.length();
        for (int k = 0; k < 100; k++) {
            arr.set(len + k, k + 399);
        }

        //System.out.println("array length is: " + arr.length());
        //System.out.println("array last indexed position is: " + arr.lastpos());

        assertTrue(arr.length() == 300); //proved that the array grows
        assertTrue(arr.lastpos() == 300);
    }

    @Test
    public void testSetPositive2() {
        DynamicLongArray arr = new DynamicLongArray(200);
        for (int i = 0; i < arr.length(); i++) {
            arr.set(i, i + 399);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        int len = arr.length();
        arr.set(len + 400, 99999);

        //System.out.println("array length is: " + arr.length());
        //System.out.println("array last indexed position is: " + arr.lastpos());

        assertTrue(arr.length() == 700); //proved that the array grows
        assertTrue(arr.lastpos() == 601);
    }

    @Test
    public void testSetNegative() {
        DynamicLongArray arr = new DynamicLongArray(200);

        try {
            arr.set(-777, 9999);
            assert (false); //don't expect to arrive here
        } catch (DataShaperException ex) {
            //ex.printStackTrace();
            assert (true);
        }
    }

    @Test
    public void testAdd() {
        DynamicLongArray arr = new DynamicLongArray(200);
        for (int i = 0; i < arr.length(); i++) {
            arr.add(i + 399);
        }
        assertTrue(arr.length() == 200);
        assertTrue(arr.lastpos() == 200);

        for (int k = 0; k < 100; k++) {
            arr.add(k + 399);
        }
        assertTrue(arr.length() == 300); //proved that the array grows
        assertTrue(arr.lastpos() == 300);
    }
    /*
     * @Test public void testInternalStateNegative() { DynamicLongArray arr =
     * new DynamicLongArray(200); for (int i = 0; i < arr.length() ; i++) {
     * arr.add(i + 399); } arr.setlastpos(300);
     *
     * try { arr.set(300, 88888); assert(false); //don't expect to arrive here }
     * catch (DataShaperException ex) { //ex.printStackTrace(); assert(false); }
     * }
     */
}
