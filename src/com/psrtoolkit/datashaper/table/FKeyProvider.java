/*
 * Copyright (C) 2010-2012, Wan Lee, wan5332@gmail.com
 * Source can be obtained from git://github.com/wanclee/datashaper.git
 * BSD-style license. Please read license.txt that comes with source files
 */
/**
 * Class definition of foreign key provider
 */
package com.psrtoolkit.datashaper.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.psrtoolkit.datashaper.enumeration.FKeyAccessMethod;
import com.psrtoolkit.datashaper.enumeration.FKeyProviderType;
import com.psrtoolkit.datashaper.exception.DataShaperException;
import com.psrtoolkit.datashaper.structure.DynamicIntArray;
import com.psrtoolkit.datashaper.structure.DynamicLongArray;
import com.psrtoolkit.datashaper.structure.DynamicStringArray;

/**
 * FKeyProvider is a wrapper over a DataTable, it provides more support for
 * foreign key references
 *
 * @author Wan
 *
 */
public class FKeyProvider {

    private FKeyProviderType type = FKeyProviderType.MAIN;
    private final static int STATUS_SELECTED = -1;
    private double percentage = 0.0;
    private FKeyAccessMethod fKeyAccessMethod = FKeyAccessMethod.SEQUENTIAL;
    private DataTable table = null;
    private String name = null;
    private String filterName = null;
    private Random randomGenerator = new Random();
    private int poolsize = 0;
    private boolean hasFilter = false;
    private boolean hasDenorm = false;
    private boolean secondaryProvider = false;
    private int forcedPopulationSize = 0;
    //variables for tracking usage count of IDs
    DynamicIntArray usageCountArray = new DynamicIntArray();
    int curIndex = 0;
    int usageMaxCountPerRecord = 0;
    //Note: curDataIndex remembers the last index into the data cache after curIndex is reset to zero; 
    //this is because doBinding is still processing fields after id field
    int curDataIndex = 0;
    //variables for managing random selection
    DynamicIntArray selectedIndicatorArray = new DynamicIntArray();
    int curUniqueSelectionCount = 0;
    DynamicIntArray recycleArray = new DynamicIntArray(); //recycle used CurIndexes
    int curPosition = 0;
    //variables for supporting denormalized column behavior
    private List<String> denormSpecArray = null;
    private List<String> denormColumnArray = null;

    public FKeyProvider(String name, DataTable table) {
        this.name = name;
        this.table = table;
    }

    public void setUsageMaxCount(int maxCount) {
        usageMaxCountPerRecord = maxCount;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void setFKeyAccessMethod(FKeyAccessMethod fKeyAccessMethod) {
        this.fKeyAccessMethod = fKeyAccessMethod;
    }

    public void setTable(DataTable table) {
        this.table = table;
    }

    public void setFilter(String filterName) {
        this.filterName = filterName;
        this.hasFilter = true;
    }

    public void setSecondaryProviderFlag(boolean secondaryFlag) {
        this.secondaryProvider = secondaryFlag;
    }

    public void setType(FKeyProviderType type) {
        this.type = type;
    }

    public void setForcedPopulationSize(int size) {
        this.forcedPopulationSize = size;
    }

    public void addDenormColumnSpecs(String denormSpec, String denormColumn) {
        if (denormSpecArray == null) {
            denormSpecArray = new ArrayList<String>();
        }

        if (denormColumnArray == null) {
            denormColumnArray = new ArrayList<String>();
        }

        denormSpecArray.add(denormSpec);
        denormColumnArray.add(denormColumn);
        hasDenorm = true;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return table.getName();
    }

    public DataTable getTable() {
        return table;
    }

    public double getPercentage() {
        return percentage;
    }

    public String getFilter() {
        return filterName;
    }

    public boolean hasFilter() {
        return hasFilter;
    }

    public boolean hasDenorm() {
        return hasDenorm;
    }

    public boolean isSecondary() {
        return secondaryProvider;
    }

    public FKeyProviderType getType() {
        return type;
    }

    public List<String> getDenormSpecArray() {
        return denormSpecArray;
    }

    public List<String> getDenormColumnArray() {
        return denormColumnArray;
    }

    public int getForcedPopulationSize() {
        return forcedPopulationSize;
    }

    long getNextFKeyId() {
        if (usageMaxCountPerRecord == 0) {
            StringBuilder message = new StringBuilder();
            message.append("Error: the maximum usage count in table '").append(table.getName()).append("'").append(" must be greater than zero");
            throw new DataShaperException(message.toString());
        }

        DynamicLongArray longArr = null;
        if (hasFilter) {
            longArr = table.getFilteredNewIdList(filterName);
        } else {
            longArr = table.getNewIdList();
        }

        if (longArr == null || longArr.lastpos() == 0) {
            StringBuilder message = new StringBuilder();
            message.append("Error: unable to obtain the new or existing").append(" row-id array from provider '").append(table.getName()).append("'");
            if (hasFilter) {
                message.append("\n Please check the filter specification - \"").append(filterName).append("\"");
            }
            throw new DataShaperException(message.toString());
        }

        if (curIndex == longArr.lastpos()) {
            StringBuilder message = new StringBuilder();
            message.append("Error: inconsistent state - tracking index").append(" in usage count array").append(" exceeds the size of IDs array in table '").append(table.getName()).append("'");
            throw new DataShaperException(message.toString());
        }

        long id = 0L;
        //IMPORTANT ASSUMPTION: if the specified percentage is more than 50 
        //and access method is RANDOM, then the access method will be treated
        //as SEQUENTIAL. This is due to performance concern in selecting unique 
        //random index for access - the higher the percentage, the worse the uniqueness
        if (fKeyAccessMethod == FKeyAccessMethod.SEQUENTIAL
                || ((percentage > 50) && fKeyAccessMethod == FKeyAccessMethod.RANDOM)) {
            int count = usageCountArray.get(curIndex);

            if (count == usageMaxCountPerRecord) {
                curIndex++;
                count = 0;
            }
            count++;
            usageCountArray.set(curIndex, count);
            id = longArr.get(curIndex);
            curDataIndex = curIndex;
            //reset counter after all record ids have been used up to the maximum limit
            if (curIndex == (longArr.lastpos() - 1) && count == usageMaxCountPerRecord) {
                resetUsageCount();
            }

        } else if (fKeyAccessMethod == FKeyAccessMethod.RANDOM) {

            if (usageCountArray.lastpos() < longArr.lastpos()) {
                usageCountArray.set(longArr.lastpos(), 0); //increase the array
                selectedIndicatorArray.set(longArr.lastpos(), 0);
            }

            if (poolsize == 0) {
                poolsize = (int) (longArr.lastpos() * percentage) / 100;
                poolsize = (poolsize < 1) ? 1 : poolsize;
            }

            //Pre-select randomly all the indexes and keep them in recycle array
            //It may take a while for a large pool with high percentage of random selection
            while (curUniqueSelectionCount < poolsize) {
                curIndex = randomGenerator.nextInt(longArr.lastpos());
                int status = selectedIndicatorArray.get(curIndex);
                if (status == STATUS_SELECTED) {
                    continue;
                }
                selectedIndicatorArray.set(curIndex, STATUS_SELECTED);
                curUniqueSelectionCount++;
                recycleArray.add(curIndex); //store the selected curIndex in recycle array
            }

            //recycle the used Indexes from the recycleArray
            curIndex = recycleArray.get(curPosition);
            int count = usageCountArray.get(curIndex);
            if (count == usageMaxCountPerRecord) {
                curPosition++;
                curIndex = recycleArray.get(curPosition);
                count = 0;
            }
            id = longArr.get(curIndex);
            curDataIndex = curIndex;
            count++;
            usageCountArray.set(curIndex, count);

            //reset before staring next round
            if (curPosition == (recycleArray.lastpos() - 1) && count == usageMaxCountPerRecord) {
                resetUsage(); //reset everything except curDataIndex which is needed by doBinding to process fields after id field.
            }
        }
        return id;
    }

    String getNextFKeyAlphaNumericId() {
        if (usageMaxCountPerRecord == 0) {
            StringBuilder message = new StringBuilder();
            message.append("Error: the maximum usage count in table '").append(table.getName()).append("'").append(" must be greater than zero");
            throw new DataShaperException(message.toString());
        }

        DynamicStringArray stringArr = null;
        if (hasFilter) {
            stringArr = table.getFilteredNewAlphaNumericIdList(filterName);
        } else {
            stringArr = table.getNewAlphaNumericIdList();
        }

        if (stringArr == null || stringArr.lastpos() == 0) {
            StringBuilder message = new StringBuilder();
            message.append("Error: unable to obtain the new or existing").append(" row-id array from '").append(table.getName()).append("'");
            throw new DataShaperException(message.toString());
        }

        if (curIndex == stringArr.lastpos()) {
            StringBuilder message = new StringBuilder();
            message.append("Error: inconsistent state - tracking index").append(" in usage count array").append(" exceeds the size of IDs array in table '").append(table.getName()).append("'");
            throw new DataShaperException(message.toString());
        }

        String id = null;
        //IMPORTANT ASSUMPTION: if the specified percentage is more than 50 
        //and access method is RANDOM, then the access method will be treated
        //as SEQUENTIAL. This is due to performance concern in selecting unique 
        //random index for access - the higher the percentage, the worse the uniqueness
        if (fKeyAccessMethod == FKeyAccessMethod.SEQUENTIAL
                || ((percentage > 50) && fKeyAccessMethod == FKeyAccessMethod.RANDOM)) {
            int count = usageCountArray.get(curIndex);

            if (count == usageMaxCountPerRecord) {
                curIndex++;
                count = 0;
            }
            count++;
            usageCountArray.set(curIndex, count);
            id = stringArr.get(curIndex);
            curDataIndex = curIndex;

            //reset counter after all record ids have been used up to the maximum limit
            if (curIndex == (stringArr.lastpos() - 1) && count == usageMaxCountPerRecord) {
                resetUsageCount();
            }

        } else if (fKeyAccessMethod == FKeyAccessMethod.RANDOM) {

            if (usageCountArray.lastpos() < stringArr.lastpos()) {
                usageCountArray.set(stringArr.lastpos(), 0); //increase the array
                selectedIndicatorArray.set(stringArr.lastpos(), 0);
            }

            if (poolsize == 0) {
                poolsize = (int) (stringArr.lastpos() * percentage) / 100;
                poolsize = (poolsize < 1) ? 1 : poolsize;
            }

            //Pre-select randomly all the indexes and keep them in recycle array
            //It may take a while for a large pool with high percentage of random selection
            while (curUniqueSelectionCount < poolsize) {
                curIndex = randomGenerator.nextInt(stringArr.lastpos());
                int status = selectedIndicatorArray.get(curIndex);
                if (status == STATUS_SELECTED) {
                    continue;
                }
                selectedIndicatorArray.set(curIndex, STATUS_SELECTED);
                curUniqueSelectionCount++;
                recycleArray.add(curIndex); //store the selected curIndex in recycle array
            }

            //recycle the used Indexes from the recycleArray
            curIndex = recycleArray.get(curPosition);
            int count = usageCountArray.get(curIndex);
            if (count == usageMaxCountPerRecord) {
                curPosition++;
                curIndex = recycleArray.get(curPosition);
                count = 0;
            }
            id = stringArr.get(curIndex);
            curDataIndex = curIndex;
            count++;
            usageCountArray.set(curIndex, count);

            //reset before staring next round
            if (curPosition == (recycleArray.lastpos() - 1) && count == usageMaxCountPerRecord) {
                resetUsage(); //reset everything except curDataIndex which is needed by doBinding to process fields after id field.
            }
        }
        return id;
    }

    public String getCurrentDenormValue(String denormSpecStr) {
        String dataStr = null;
        //Note: curDataIndex is used instead of curIndex; this is because curIndex is reset to 0 after
        //max usage count has exhausted on getNextId() call, but there are still denorm-val fields to be processed after
        //the id is obtained. 'curDataIndex' preserves the last valid index after curIndex is reset.
        if (hasFilter) {
            dataStr = table.getFilterDenormValue(filterName, curDataIndex, denormSpecStr);
        } else {
            dataStr = table.getDenormValue(curDataIndex, denormSpecStr);
        }
        return dataStr;
    }

    /**
     * Reset usage count
     */
    public void resetUsageCount() {
        usageCountArray.reset();
        curIndex = 0;
    }

    private void resetUsage() {
        resetUsageCount();
        selectedIndicatorArray.reset();
        recycleArray.clear();
        curUniqueSelectionCount = 0;
        curPosition = 0;
    }
}
