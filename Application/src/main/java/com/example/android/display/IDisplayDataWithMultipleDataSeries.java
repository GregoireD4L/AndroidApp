package com.example.android.display;

import com.scichart.charting.model.dataSeries.XyDataSeries;

import java.util.ArrayList;

/**
 * Created by kokoghlanian on 25/03/2018.
 */

public interface IDisplayDataWithMultipleDataSeries {


    ArrayList<Double> displayData(String data, int channelSelected);

    default ArrayList<XyDataSeries> clearGraph(ArrayList<XyDataSeries> dataToClear){
        ArrayList<XyDataSeries> clearedList = dataToClear;
        for(int i = 0; i < dataToClear.size(); i++ ){
            clearedList.get(i).removeRange(0,dataToClear.get(i).getCount());
        }
        return clearedList;
    }
}
