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
 * Dynamic array of primitive integer types. <p> The data structure should be
 * used in place of java.util.ArrayList<Integer> when a large number of integer
 * values are used and purged frequently. DataShapers keeps a counter of usage
 * for thousands of record ID in memory and throw them away when they are no
 * longer need; therefore, performance can be achieved by avoiding the use of
 * java.lang.Integer wrapper class and java.util.ArrayList
 *
 * This is an identical structure implementation as DynamicLongArray except for
 * the native data type
 *
 * @author Wan
 *
 */
public class DynamicIntArray {

    int[] intArr = null;
    int defaultSize = 100;
    //marker for the last indexed cell in the array;
    //it is not the length of the array
    int lastPos = 0;

    public DynamicIntArray() {
        intArr = new int[defaultSize];
    }

    /**
     * @param size the size of array to be used in the array initialization; if
     * size < defaultSize (which is 100), the default size will be used instead
     * @exception throws DataShaperException runtime exception;
     */
    public DynamicIntArray(int size) {
        //TODO: handle size > Integer.MAX_VALUE scenario

        if (size <= 0) {
            throw new DataShaperException("Error: dynamic integer array cannot be"
                    + " initialized with zeor or negative size value)");
        }

        if (size < defaultSize) {
            intArr = new int[defaultSize];
        } else {
            intArr = new int[size];
        }
        lastPos = 0;
    }

    /**
     * Get the element at the indexed position in the array
     *
     * @param position the zero-based integer value
     * @return the int value at the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public int get(int position) {
        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic integer array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }
        if (position >= intArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index (").append(position).append(")").append(" is outside the dynamic int array boundary (").append(intArr.length).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        return intArr[position];
    }

    /**
     * Set into the array the value at the indexed position If the array is too
     * small, grow the array to the position value + default size and move the
     * last indexed position marker down
     *
     * @param position the zero-based integer value
     * @param value the integer value to be placed in the indexed position
     * @exception throws DataShaperException runtime exception; wraps
     * ArrayIndexOutOfBoundsException when it occurred
     */
    public void set(int position, int value) {

        if (position < 0) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: index to the dynamic integer array").append(" cannot be negative (").append(position).append(")");
            throw new DataShaperException(message.toString(),
                    new ArrayIndexOutOfBoundsException());
        }

        if (lastPos > intArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(intArr.length).append(")");
            throw new DataShaperException(message.toString());
        }

        if (position >= intArr.length) {
            int[] temp = new int[position + defaultSize];
            System.arraycopy(intArr, 0, temp, 0, intArr.length);
            intArr = temp;
            lastPos = position + 1;
        } else {
            if (position >= lastPos) {
                lastPos = position + 1;
            }
        }
        intArr[position] = value;
    }

    /**
     * Add the value to the last unoccupied cell of the array. If the array is
     * too small, grow the array by the default size and move the last indexed
     * position marker down
     *
     * @param value the primitive int value to be added to the last unoccupied
     * array cell
     * @exception throws DataShaperException runtime exception;
     */
    public void add(int value) {
        if (lastPos > intArr.length) {
            StringBuilder message = new StringBuilder(200);
            message.append("Error: internal state is inconsistent - the last ").append("indexed position (").append(lastPos).append(")").append(" is greater than the size of the array (").append(intArr.length).append(")");
        }

        if (lastPos == intArr.length) {
            int[] temp = new int[intArr.length + defaultSize];
            System.arraycopy(intArr, 0, temp, 0, intArr.length);
            intArr = temp;
        }
        intArr[lastPos] = value;
        lastPos++; //move the last indexed position marker down
    }

    /**
     * Return the current physical length of the array
     *
     * @return the length of the array
     */
    public int length() {
        return intArr.length;
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
     * Clear the content and move lastPos to zero
     */
    public void clear() {
        Arrays.fill(intArr, 0);
        lastPos = 0;
    }

    /**
     * Reset the content to zero but do not move lastPos to zero
     */
    public void reset() {
        Arrays.fill(intArr, 0);
    }
    //public void setlastpos(int lastpos) { this.lastPos = lastpos;}
}
