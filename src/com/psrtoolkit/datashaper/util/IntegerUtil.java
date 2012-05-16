/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Utility for handling Integer-related processing
 */
package com.psrtoolkit.datashaper.util;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * @author Wan
 *
 */
public class IntegerUtil {

    private IntegerUtil() {
    }

    /**
     * Utilize Integer.parseInt to return integer value
     *
     * @param integerStr string containing the digits
     * @param minValue minimum valid value
     * @param maxValue maximum valid value
     * @param context context variable string arguments that provides the
     * context of the call
     * @return throws DataShaperException runtime exception
     */
    public static int parseInt(String integerStr, int minValue, int maxValue, String... context) {
        int size = 0;
        try {
            size = Integer.parseInt(integerStr);
            if (size < minValue || size > maxValue) {
                StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
                message.append("Error: The value '").append(integerStr).append("'");
                if (size < minValue) {
                    message.append(" is smaller than the minimum value of ").append(minValue);
                } else {
                    message.append(" is greater than the maximum value of ").append(maxValue);
                }
                message.append(".").append(" Found in ").append(StringUtil.constructTrailingMsg(context));
                throw new DataShaperException(message.toString());
            }
            return size;
        } catch (NumberFormatException ex) {
            StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
            message.append("Error: invalid value '").append(integerStr).append("' found in '").append(StringUtil.constructTrailingMsg(context));
            throw new DataShaperException(message.toString(), ex);
        }
    }
}
