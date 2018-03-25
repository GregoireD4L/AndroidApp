package com.example.android.display;


import java.util.ArrayList;

/**
 * Created by kokoghlanian on 20/03/2018.
 */

public class DisplayTempImpl implements IDisplayDataWithMultipleDataSeries {

    @Override
    public ArrayList<Double> displayData(String data, int channelSelected) {

        ArrayList<Double> dataResult = new ArrayList<>();
        if (data != null) {
            final String[] dataList = data.split("\n");
            final String dataDecoded = dataList[dataList.length - 1].replace(" ", "");
            final double dataDecodedTemp = Integer.parseInt(dataDecoded.substring(196, 200), 16);
            dataResult.add(dataDecodedTemp);
        }
        return dataResult;
    }
}
