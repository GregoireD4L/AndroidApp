package com.example.dataforlife.display;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.extensions.builders.SciChartBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by kokoghlanian on 20/03/2018.
 */

public class DisplayDataAcceleroImpl implements IDisplayDataWithMultipleDataSeries
{

    @Override
    public ArrayList<Double> displayData(String data, int channelSelected) {

        ArrayList<Double> dataSeriesList = new ArrayList<>();
        if (data != null) {

            String dataDecoded;
            double dataDecodedX;
            double dataDecodedY;
            double dataDecodedZ;

            if (channelSelected == 1) {
                String[] dataList = data.split("\n");
                dataDecoded = dataList[dataList.length - 1].replace(" ", "");
                dataDecodedX = (double) Integer.parseInt(dataDecoded.substring(24, 36).substring(0, 4), 16);
                dataDecodedY = (double) Integer.parseInt(dataDecoded.substring(24, 36).substring(4, 8), 16);
                dataDecodedZ = (double) Integer.parseInt(dataDecoded.substring(24, 36).substring(8, 12), 16);

                if (dataDecodedX > 32767) {
                    dataDecodedX = (-65536 + dataDecodedX) * 2 / 32768;
                } else {
                    dataDecodedX = dataDecodedX * 2 / 32767;
                }
                if (dataDecodedY > 32767) {
                    dataDecodedY = (-65536 + dataDecodedY) * 2 / 32768;
                } else {
                    dataDecodedY = dataDecodedY * 2 / 32767;
                }
                if (dataDecodedZ > 32767) {
                    dataDecodedZ = (-65536 + dataDecodedZ) * 2 / 32768;
                } else {
                    dataDecodedZ = dataDecodedZ * 2 / 32767;
                }

            } else if (channelSelected == 2) {
                String[] dataList = data.split("\n");
                dataDecoded = dataList[dataList.length - 1].replace(" ", "");
                dataDecodedX = (double) Integer.parseInt(dataDecoded.substring(12, 24).substring(0, 4), 16);
                dataDecodedY = (double) Integer.parseInt(dataDecoded.substring(12, 24).substring(4, 8), 16);
                dataDecodedZ = (double) Integer.parseInt(dataDecoded.substring(12, 24).substring(8, 12), 16);
                if (dataDecodedX > 32767) {
                    dataDecodedX = (-65536 + dataDecodedX) * 250 / 32768;
                } else {
                    dataDecodedX = dataDecodedX * 250 / 32767;
                }
                if (dataDecodedY > 32767) {
                    dataDecodedY = (-65536 + dataDecodedY) * 250 / 32768;
                } else {
                    dataDecodedY = dataDecodedY * 250 / 32767;
                }
                if (dataDecodedZ > 32767) {
                    dataDecodedZ = (-65536 + dataDecodedZ) * 250 / 32768;
                } else {
                    dataDecodedZ = dataDecodedZ * 250 / 32767;
                }
            } else {
                String[] dataList = data.split("\n");
                dataDecoded = dataList[dataList.length - 1].replace(" ", "");
                dataDecodedX = (double) Integer.parseInt(dataDecoded.substring(0, 12).substring(0, 4), 16);
                dataDecodedY = (double) Integer.parseInt(dataDecoded.substring(0, 12).substring(4, 8), 16);
                dataDecodedZ = (double) Integer.parseInt(dataDecoded.substring(0, 12).substring(8, 12), 16);
                if (dataDecodedX > 32767) {
                    dataDecodedX = (-65536 + dataDecodedX) * 2 / 32768;
                } else {
                    dataDecodedX = dataDecodedX * 2 / 32767;
                }
                if (dataDecodedY > 32767) {
                    dataDecodedY = (-65536 + dataDecodedY) * 2 / 32768;
                } else {
                    dataDecodedY = dataDecodedY * 2 / 32767;
                }
                if (dataDecodedZ > 32767) {
                    dataDecodedZ = (-65536 + dataDecodedZ) * 2 / 32768;
                } else {
                    dataDecodedZ = dataDecodedZ * 2 / 32767;
                }
            }
            dataSeriesList.add(dataDecodedX);
            dataSeriesList.add(dataDecodedY);
            dataSeriesList.add(dataDecodedZ);
        }
        return dataSeriesList;
    }

}
