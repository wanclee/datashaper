/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 *
 */
package com.psrtoolkit.datashaper.structure;

import java.util.Arrays;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * Dynamic array of primitive long types. <p> The data structure should be used
 * in place of java.util.ArrayList<Long> when a large number of long values are
 * used and purged frequently. DataShapers keeps thousands of record ID in
 * memory and throw them away when they are no longer need; therefore,
 * performance can be achieved by avoiding the use of java.lang.Long wrapper
 * class and java.util.ArrayList
 *
 * @author Wan
 *
 */
public class DynamicLongArray {

    long[] longArr = null;
    int defaultSize = 100;
    //marker for the last indexed cell in the array;
    //it is not the length of the array
    int lastPos = 0;

    public DynamicLongArray() {
        longArr = new long[defaultSize];
    }

    /**
     * @param size the size of array to be used in the array initialization; if
     * size < defaultSize (which is 100), the default size will be used instead
     * @exception throws DataShaperException runtime exception;
     */
    public DynamicLongArray(int size) {
        //TODO: handle size > Integer.MAX_VALUE scenario

        if (size <= 0) {
            throw new DataShaperException("Error: dynamic long array cannot be"
                    + " initialized with zeor or negative size value)");
        }

        if (size < defaultSize) {
            longArr = new long[defaultSize];
        } else {
            longArr = new long[size];
        }
        lastPos = 0;
    }

    /**
     * Get the element at the indexed position in the array
     *
     * @param position the zero-based integer value
     * @return the long value at the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public long get(int position) {
        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic long array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }
        if (position >= longArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index (").append(position).append(")").append(" is outside the dynamic long array boundary (").append(longArr.length).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        return longArr[position];
    }

    /**
     * Set into the array the value at the indexed position If the array is too
     * small, grow the array to the position value + default size and move the
     * last indexed position marker down
     *
     * @param position the zero-based integer value
     * @param value the long value to be placed in the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public void set(int position, long value) {

        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic long array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        if (lastPos > longArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(longArr.length).append(")");
            throw new DataShaperException(message.toString());
        }

        if (position >= longArr.length) {
            long[] temp = new long[position + defaultSize];
            System.arraycopy(longArr, 0, temp, 0, longArr.length);
            longArr = temp;
            lastPos = position + 1;
        } else {
            if (position >= lastPos) {
                lastPos = position + 1;
            }
        }
        longArr[position] = value;
    }

    /**
     * Add the value to the last unoccupied cell of the array. If the array is
     * too small, grow the array by the default size and move the last indexed
     * position marker down
     *
     * @param value the primitive long value to be added to the last unoccupied
     * array cell
     * @exception throws DataShaperException runtime exception;
     */
    public void add(long value) {
        if (lastPos > longArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(longArr.length).append(")");
        }

        if (lastPos == longArr.length) {
            long[] temp = new long[longArr.length + defaultSize];
            System.arraycopy(longArr, 0, temp, 0, longArr.length);
            longArr = temp;
        }
        longArr[lastPos] = value;
        lastPos++; //move the last indexed position marker down
    }

    /**
     * Return the current physical length of the array
     *
     * @return the length of the array
     */
    public int length() {
        return longArr.length;
    }

    /**
     * Return the last indexed position of the array This is the logical length
     * of the array.
     *
     * @return the last indexed position of the array; indicating the first
     * unoccupied cell position.
     */
    public int lastpos() {
        return lastPos;
    }

    /**
     * Set the content of the array to 0L
     */
    public void clear() {
        Arrays.fill(longArr, 0L);
        lastPos = 0;
    }
    //public void setlastpos(int lastpos) { this.lastPos = lastpos;}
}
