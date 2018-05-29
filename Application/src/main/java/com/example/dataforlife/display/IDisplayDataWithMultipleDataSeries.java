package com.example.dataforlife.display;

import com.scichart.charting.model.dataSeries.XyDataSeries;

import java.util.ArrayList;

/**
 * Created by kokoghlanian on 25/03/2018.
 */

public interface IDisplayDataWithMultipleDataSeries {

    ArrayList<Double> displayData(String data, int channelSelected);

}
