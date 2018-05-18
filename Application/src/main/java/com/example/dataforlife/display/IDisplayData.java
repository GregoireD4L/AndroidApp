package com.example.dataforlife.display;

import android.util.Log;

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

        Log.e("DISPLAY XYDATASERIES", "X =" +dataToClear.getXValues().size()+"      Y = "+ dataToClear.getYValues().size() );

        //dataToClear.removeRange(0, dataToClear.getCount());
        dataToClear.clear();
        return dataToClear;
    }
}
