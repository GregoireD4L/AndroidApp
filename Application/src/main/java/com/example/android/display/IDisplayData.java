package com.example.android.display;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;

import java.util.ArrayList;

/**
 *
 * Created by kokoghlanian on 20/03/2018.
 */

public interface IDisplayData{

    ArrayList<Double> getDataFromString(String data);

    ArrayList<Double> getDataWhithoutFilter(ArrayList<Double> dataList, int channelSelected);

    ArrayList<Double> getDataWithFilter(ArrayList<Double> dataList, int channelSelected);

    ArrayList<Double> displayData(String data, boolean isDataSave, int mChannelSelected, boolean isFilteringOn);


    default XyDataSeries clearGraph(XyDataSeries dataToClear){
        dataToClear.removeRange(0, dataToClear.getCount());
        return dataToClear;
    }
}
