package com.example.android.display;

import com.scichart.charting.model.dataSeries.XyDataSeries;

import java.util.ArrayList;

/**
 * Created by kokoghlanian on 20/03/2018.
 */

public class DisplayRespirationImpl implements IDisplayDataWithMultipleDataSeries {


    @Override
    public ArrayList<Double> displayData(String data, int channelSelected) {

        ArrayList<Double> dataSeriesList = new ArrayList<>();
        if (data != null) {
            final String[] dataList = data.split("\n");
            final String dataDecoded = dataList[dataList.length - 1].replace(" ", "");
            final double dataDecodedT = Integer.parseInt(dataDecoded.substring(120, 124), 16);
            final double dataDecodedA = Integer.parseInt(dataDecoded.substring(124, 128), 16);
            dataSeriesList.add(dataDecodedT);
            dataSeriesList.add(dataDecodedA);
        }
        return dataSeriesList;
    }

}
