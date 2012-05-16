/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Custom utility for string manipulation
 */
package com.psrtoolkit.datashaper.util;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.psrtoolkit.datashaper.exception.DataShaperException;

/**
 * Custom utility for string manipulation
 *
 * @author Wan
 */
public class StringUtil {

    private static final int MAX_LIMIT = 1000;
    public static final int MAX_RULES_EXECUTION_RUNAWAY_LIMIT = 10000;
    public static final int MAX_NEW_RECORDS = 300;
    public static final int MAX_TABLES = 5;
    public static final int RULES_SATISFIED = 1;
    public static final int RULES_NOTSATISFIED = -1;
    public static final int BUFFER_SIZE_100 = 100;
    public static final int BUFFER_SIZE_256 = 256;
    public static final int BUFFER_SIZE_512 = 512;
    public static final String DS_NEXTVAL = "${ds-nextval}";
    public static final String DS_FOREIGN_KEY_PROVIDER = "${ds-fkey-provider}";
    public static final String DS_FOREIGN_KEY_SECONDARY_PROVIDER = "${ds-fkey-secondary-provider}";
    public static final String DS_PICKLIST = "${ds-picklist}";
    public static final String DS_DEFAULT_SIGNATURE = "${ds-default-signature}";
    public static final String DS_DEFAULT_SIGNATURE_VAL = " -DataShaper ";
    public static final String DS_DENORM_VAL = "${ds-denorm-val}";
    public static final String DS_CURRENT_CREATOR = "${ds-current-creator}";
    public static final String DS_CURRENT_DATE = "${ds-current-date}";
    public static final String DS_CURRENT_DATETIME = "${ds-current-datetime}";
    public static final String DS_CURRENT_RECORD_ID = "${ds-current-record-id}";
    public static final String DS_ROW_ID = "${ds-row-id}";
    public static final String DS_UUID = "${ds-uuid}";
    public static final String DS_TEXTFILE = "${ds-textfile}";
    public static final String DS_BINARYFILE = "${ds-binaryfile}";
    public static final String DS_USER_SIGNATURE = "${ds-user-signature}";
    public static final String FILE_DATASHAPER_LICENSE = "datashaper.license";
    public static final String FILE_DEFAULT_LICENSE = "default.license";
    public static final String JARFILE_DATASHAPER = "datashaper.jar";
    public static final String VENDOR_NAME = "www.psrtoolkit.com";
    public static final String COPYRIGHT_NOTICE = "Copyright(C) 2010-2012, Wan Lee, wan5332@gmail.com";
    public static final String MARKER_DSH = "DSH";
    public static final String MARKER_HSD = "HSD";
    public static final String VERSION_MAJOR = "1";

    private StringUtil() {
    }

    /**
     *
     */
    /**
     * Custom String split method, which handle the splitting of string
     * containing a delimeter and enclosure <p> Use case #1: where delimeter
     * character doesn't appear within the pair(s) of enclosure characters in
     * input data <p> Example: inputData = "'20003', 'johndoe111@yahoo.com',
     * 'John Doe', 'user', '0'"; <p> Use case #2: where delimeter DOES appear
     * within the pair(s) of enclosure <p> Example: inputData = "'20003',
     * 'johndoe111@yahoo.com', 'John,, ,Doe', 'user', '0'"; <p> Use case #3:
     * where no enclosure pairs are specified in the input data <p> Example:
     * inputData = "20003, johndoe111@yahoo.com, John Doe, user, 0"; <p> The
     * maximum splitting limit is imposed and set at 1000 internally. When
     * delimeter appears within a pair of enclosures around a token in the input
     * string, the delimeter in the string token will be preserved. All white
     * spaces, if exist, at both ends of the token are trimmed.
     *
     * @param inputStr input string to be split
     * @param delimeter string containing one or more characters; e.g. ","
     * @param enclosureStr string containing one or more characters; e.g. "'"
     * @return String[] string array; when inputStr null or empty, returns
     * zero-size string array
     */
    public static String[] split(String inputStr, String delimeter, String enclosureStr) {

        //Note: do not change order of these checks
        if (inputStr == null || inputStr.isEmpty()) {
            return new String[0]; //return a zero-size string array
        }
        if (delimeter == null || delimeter.isEmpty()) {
            String[] strArr = new String[1];
            strArr[0] = inputStr;
            return strArr;
        }

        String enclosure = (enclosureStr != null) ? enclosureStr : "";

        //prevent run-away splitting
        String[] strArr = inputStr.split(delimeter, MAX_LIMIT);

        StringBuilder temp = new StringBuilder();
        boolean notProperlyEnclosed = false;
        int resetAtIndex = -1;

        for (int i = 0; i < strArr.length; i++) {
            //keep the original token before trimming;
            //and accumulate it for handling case 2 if it happens
            if (!enclosure.isEmpty()) {
                temp.append(strArr[i]);
            }
            strArr[i] = strArr[i].trim(); //remove white spaces

            if (strArr[i].startsWith(enclosure)
                    && strArr[i].endsWith(enclosure)) { //straight forward case 1
                strArr[i] = strArr[i].substring(enclosure.length(), strArr[i].length() - enclosure.length());

                //empty the temp buffer
                temp.delete(0, temp.length());

            } else { //handle case 2
                notProperlyEnclosed = true;
                if (strArr[i].startsWith(enclosure)) {

                    //give back the delimeter character
                    temp.append(delimeter);
                    resetAtIndex = i;

                } else if (strArr[i].endsWith(enclosure)) {
                    // all tokens have been re-assembled
                    strArr[resetAtIndex] = temp.toString();

                    //remove any white spaces at both ends
                    strArr[resetAtIndex] = strArr[resetAtIndex].trim();

                    strArr[resetAtIndex] =
                            strArr[resetAtIndex].substring(enclosure.length(),
                            strArr[resetAtIndex].length() - enclosure.length());

                    temp.delete(0, temp.length()); //empty the temp buffer

                    //empty the string at this index location
                    strArr[i] = "{{**marked-as-dont-care**}}";
                    resetAtIndex = -1; //reset

                } else {
                    //the is the case where the token has no enclosure
                    //character at both ends

                    //give back the delimeter character
                    temp.append(delimeter);

                    //empty the string at this index location
                    strArr[i] = "{{**marked-as-dont-care**}}";
                }
            }
        }

        if (notProperlyEnclosed) {
            List<String> newStrArr = new ArrayList<String>();
            for (int i = 0; i < strArr.length; i++) {
                //if (strArr[i] != null && !strArr[i].isEmpty()) {
                if (strArr[i] != null && !strArr[i].equals("{{**marked-as-dont-care**}}")) {
                    newStrArr.add(strArr[i]);
                }
            }
            String str[] = (String[]) newStrArr.toArray(new String[newStrArr.size()]);
            return str;
        }
        return strArr;
    }

    /**
     * Place-holders of the form ${text} are substituted. <p> The place-holder
     * name constants are: StringUtil.DS_DEFAULT_SIGNATURE,
     * StringUtil.DS_USER_SIGNATURE, StringUtil..DS_CURRENT_DATE,
     * StringUtil..DS_CURRENT_DATETIME, and StringUtil.DS_CURRENT_RECORD_ID
     *
     * @param inputData the string that contains the place holders to be
     * substituted
     * @param userSignature user defined signature string
     * @param curId current record id
     * @param maxLimit maximum size of the modified string after substitution
     * @return new string if substitution occurred; otherwise return original
     * inputData
     */
    public static String doSubstitution(String inputData, String userSignature, long curId, String curAlphaNumericId, int maxLimit) {
        boolean modified = false;
        int start = -1;
        int end = -1;
        StringBuilder buf = null;

        if (inputData == null || inputData.isEmpty()) {
            return inputData;
        }

        if (inputData.contains(StringUtil.DS_DEFAULT_SIGNATURE)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() < 20 ? 40 : inputData.length() * 2);
                buf.append(inputData);
            }
            end = StringUtil.DS_DEFAULT_SIGNATURE.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_DEFAULT_SIGNATURE)) >= 0) {
                buf.replace(start, start + end, StringUtil.DS_DEFAULT_SIGNATURE_VAL);
            }
            modified = true;
        }

        if (inputData.contains(StringUtil.DS_USER_SIGNATURE)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() < 20 ? 40 : inputData.length() * 2);
                buf.append(inputData);
            }
            end = StringUtil.DS_USER_SIGNATURE.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_USER_SIGNATURE)) >= 0) {
                buf.replace(start, start + end, userSignature == null ? "" : userSignature);
            }
            modified = true;
        }

        if (inputData.contains(StringUtil.DS_CURRENT_DATE)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() + 20);
                buf.append(inputData);
            }
            Date udate = new Date();
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

            end = StringUtil.DS_CURRENT_DATE.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_CURRENT_DATE)) >= 0) {
                buf.replace(start, start + end, df.format(udate));
            }
            modified = true;
        }

        if (inputData.contains(StringUtil.DS_CURRENT_DATETIME)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() + 30);
                buf.append(inputData);
            }
            Date udate = new Date();
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT);

            end = StringUtil.DS_CURRENT_DATETIME.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_CURRENT_DATETIME)) >= 0) {
                buf.replace(start, start + end, df.format(udate));
            }
            modified = true;
        }

        if (inputData.contains(StringUtil.DS_CURRENT_RECORD_ID)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() + 20);
                buf.append(inputData);
            }
            end = StringUtil.DS_CURRENT_RECORD_ID.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_CURRENT_RECORD_ID)) >= 0) {
                if (curAlphaNumericId.isEmpty()) {
                    buf.replace(start, start + end, String.valueOf(curId));
                } else {
                    buf.replace(start, start + end, curAlphaNumericId);
                }
            }
            modified = true;
        }

        if (inputData.contains(StringUtil.DS_UUID)) {
            if (buf == null) {
                buf = new StringBuilder(inputData.length() + 20);
                buf.append(inputData);
            }
            end = StringUtil.DS_UUID.length();
            while ((start = buf.indexOf(
                    StringUtil.DS_UUID)) >= 0) {
                buf.replace(start, start + end, StringUtil.getUUID());
            }
            modified = true;
        }

        if (modified) {
            if (buf.length() > maxLimit) {
                buf.delete(maxLimit, buf.length());
            }
            //System.out.println("Substituted string is now: " + buf.toString());
        }
        String s = modified ? buf.toString() : inputData;
        return s;
    }

    public static void HandleUndefinedValue(String value, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: undefined value '").append(value).append("' found in '").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static void HandleInvalidValue(String value, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: invalid value '").append(value).append("' found in '").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static void HandleInsufficientValue(String value, String minValue, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: insufficient value '").append(value).append("' found in '").append(constructTrailingMsg(context)).append(" The value must be a minimum of ").append(minValue);
        throw new DataShaperException(message.toString());
    }

    public static void HandleUndefinedElement(String element, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: undefined element '<").append(element).append(">' found in '").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static void HandleEmptyness(String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: empty value").append(" found in '").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static void HandleInvalidOptions(String option1, String option2, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: invalid option combination ").append("'").append(option1).append("'").append(" and ").append("'").append(option2).append("'").append(",").append(" found in '").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static void handleIncompleteDefinition(String definition, String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: insufficient definition elements '").append(definition).append("' found in '").append(constructTrailingMsg(context)).append("\n").append("Either the table or column name is missing; use the format '").append(StringUtil.DS_DENORM_VAL).append("=").append("table-name").append(".").append("column-name").append("\'");
        throw new DataShaperException(message.toString());
    }

    public static void HandleOnlyNumericAllowed(String... context) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        message.append("Error: only numerical value allowed in ").append(constructTrailingMsg(context));
        throw new DataShaperException(message.toString());
    }

    public static StringBuilder constructTrailingMsg(String[] msgItems) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_100);
        for (int i = 0; i < msgItems.length; i++) {
            message.append(msgItems[i]);
            if (i == (msgItems.length - 1)) {
                message.append("'");
            } else {
                message.append("' in '");
            }
        }
        return message;
    }

    public static String getCommandLineUsageMessage(String str1, String str2) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_256);
        message.append(str1).append(str2).append("\n").append("  Usage: datashaper -c configeFileName -u loginName  -p userPassword [-s userSignature]\n");
        return message.toString();
        //throw new DataShaperException(message.toString());
    }

    public static String getUUID() {
        UUID uuid = UUID.randomUUID();
        return String.valueOf(Math.abs(uuid.hashCode()));
    }

    public static void handleChainOfNextExceptionsAndCauses(Exception ex, String sqlStmt) {
        StringBuilder message = new StringBuilder(StringUtil.BUFFER_SIZE_512);
        message.append("Error: failed to populate data - ");
        if (ex instanceof BatchUpdateException || ex instanceof SQLException) {
            SQLException exx = (SQLException) ex;
            while (exx != null) {
                message.append(exx.toString()).append("\n");
                exx = exx.getNextException();
            }
        } else if (ex instanceof Exception) {
            Throwable e = ex.getCause();
            while (e != null) {
                message.append(e.toString()).append("\n");
                e = e.getCause();
            }
        }
        message.append(sqlStmt);
        throw new DataShaperException(message.toString(), ex);
    }

    public static String bytesToHex(byte[] byteArr) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < byteArr.length; i++) {
            buf.append(Integer.toHexString(0xFF & byteArr[i]).toUpperCase()).append(" ");
        }
        return buf.toString();
    }

    public static String bytesToHexChecksum(byte[] byteArr) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < byteArr.length; i++) {
            buf.append(Integer.toHexString(0xFF & byteArr[i]).toUpperCase());
        }
        return buf.toString();
    }

    public static byte[] hexToIntegerBytes(String hexStr) {
        String[] strArr = hexStr.split(" ");
        int size = strArr.length;
        byte[] byteArr = new byte[size];
        Integer val = 0;
        for (int i = 0; i < size; i++) {
            val = Integer.valueOf(strArr[i], 16);
            byteArr[i] = val.byteValue();
        }
        return byteArr;
    }

    public static void splashScreen(Properties license, String licenseFile, String vendorName) {
        StringBuilder message = new StringBuilder();
        if (license.isEmpty()) {
            message.append("No license key detected. The functionality of DataShaper without a license is restricted to maximum ").append(StringUtil.MAX_NEW_RECORDS).append(" new records or maximum ").append(StringUtil.MAX_TABLES).append(" logical tables processing");
            System.out.println(message.toString());
            message.delete(0, message.length());
            message.append("You can purchase the license key from ").append(vendorName).append(" to remove the restriction");

            System.out.println(message.toString());
            message.delete(0, message.length());

            message.append("If you have received the license key, please put the content in a text file named '").append(licenseFile).append("'\n");
            System.out.println(message.toString());

        } else {
            String expiryDate = license.getProperty("expiry-date");
            Date edate = null;
            if (expiryDate != null && expiryDate.equals("none") == false) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    edate = df.parse(expiryDate);
                } catch (ParseException ep) {
                    throw new DataShaperException("Error: " + ep);
                }
            }

            message.append("This copy of DataShaper is licensed to ").append(license.getProperty("licensee")).append(". ").append(edate == null ? "" : "The license will expire on ").append(edate == null ? "" : edate.toString()) /*
                     * .append(". ") .append("The product version is ")
				   .append(license.getProperty("version"))
                     */.append("\n");
            System.out.println(message.toString());
        }

    }
}
