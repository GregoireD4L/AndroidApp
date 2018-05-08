package com.example.dataforlife.display;

import android.util.Log;

import com.scichart.charting.model.dataSeries.XyDataSeries;

import java.util.ArrayList;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Created by kokoghlanian on 20/03/2018.
 */

public class DisplaySpO2Impl implements IDisplayData {

    private Butterworth mBtwFilterLow;
    private Butterworth mBtwFilterHigh;
    private double dataDecodedR1MSB;
    private double dataDecodedR1LSB;
    private double dataDecodedIr1MSB;
    private double dataDecodedIr1LSB;
    private double dataDecodedR2MSB ;
    private double dataDecodedR2LSB ;
    private double dataDecodedIr2MSB ;
    private double dataDecodedIr2LSB ;


    public DisplaySpO2Impl(){

        mBtwFilterLow = new Butterworth();
        mBtwFilterLow.lowPass(4, 500, 40);
        mBtwFilterHigh = new Butterworth();
        mBtwFilterHigh.highPass(2, 500, 0.05);
    }

    @Override
    public ArrayList<Double> getDataFromString(String data) {
     return null;
    }

    @Override
    public ArrayList<Double> getDataWhithoutFilter(ArrayList<Double> dataList, int channelSelected) {

        ArrayList<Double> spo2Data = new ArrayList<>();
        if (channelSelected == 1) {
            //double databuged = dataDecodedR1LSB + ((dataDecodedR1MSB % 2) + ((dataDecodedR1MSB - (dataDecodedR1MSB % 2)) % 4)) * 65536 + dataDecodedR2LSB + ((dataDecodedR2MSB % 2) + ((dataDecodedR2MSB - (dataDecodedR2MSB % 2)) % 4)) * 65536;

            spo2Data.add(dataDecodedR1LSB + ((dataDecodedR1MSB % 2) + ((dataDecodedR1MSB - (dataDecodedR1MSB % 2)) % 4)) * 65536);
            spo2Data.add(dataDecodedR2LSB + ((dataDecodedR2MSB % 2) + ((dataDecodedR2MSB - (dataDecodedR2MSB % 2)) % 4)) * 65536);
        } else {
            //double databuged = dataDecodedIr1LSB + ((dataDecodedIr1MSB % 2) + ((dataDecodedIr1MSB - (dataDecodedIr1MSB % 2)) % 4)) * 65536 + dataDecodedIr2LSB + ((dataDecodedIr2MSB % 2) + ((dataDecodedIr2MSB - (dataDecodedIr2MSB % 2)) % 4)) * 65536;

            spo2Data.add(dataDecodedIr1LSB + ((dataDecodedIr1MSB % 2) + ((dataDecodedIr1MSB - (dataDecodedIr1MSB % 2)) % 4)) * 65536);
            spo2Data.add(dataDecodedIr2LSB + ((dataDecodedIr2MSB % 2) + ((dataDecodedIr2MSB - (dataDecodedIr2MSB % 2)) % 4)) * 65536);
        }
        return spo2Data;
    }

    @Override
    public ArrayList<Double> getDataWithFilter(ArrayList<Double> dataList, int channelSelected) {

        ArrayList<Double> spo2Data = new ArrayList<>();
        if (channelSelected == 1) {
            spo2Data.add(mBtwFilterLow.filter(mBtwFilterHigh.filter(dataDecodedR1LSB + ((dataDecodedR1MSB % 2) + ((dataDecodedR1MSB - (dataDecodedR1MSB % 2)) % 4)) * 65536)));
            spo2Data.add(mBtwFilterLow.filter(mBtwFilterHigh.filter(dataDecodedR2LSB + ((dataDecodedR2MSB % 2) + ((dataDecodedR2MSB - (dataDecodedR2MSB % 2)) % 4)) * 65536)));
        } else {
            spo2Data.add(mBtwFilterLow.filter(mBtwFilterHigh.filter(dataDecodedIr1LSB + ((dataDecodedIr1MSB % 2) + ((dataDecodedIr1MSB - (dataDecodedIr1MSB % 2)) % 4)) * 65536)));
            spo2Data.add( mBtwFilterLow.filter(mBtwFilterHigh.filter(dataDecodedIr2LSB + ((dataDecodedIr2MSB % 2) + ((dataDecodedIr2MSB - (dataDecodedIr2MSB % 2)) % 4)) * 65536)));
        }
        return spo2Data;
    }

    @Override
    public ArrayList<Double> displayData(String data, boolean isDataSave, int channelSelected, boolean isFilteringOn) {
        ArrayList<Double> spo2Data = new ArrayList<>();
        if (data != null) {
            final String[] dataList = data.split("\n");
            final String dataDecoded = dataList[dataList.length - 1].replace(" ", "");
            this.dataDecodedR1MSB = Integer.parseInt(dataDecoded.substring(173, 174), 16);
            this.dataDecodedR1LSB = Integer.parseInt(dataDecoded.substring(174, 178), 16);
            this.dataDecodedIr1MSB = Integer.parseInt(dataDecoded.substring(179, 180), 16);
            this.dataDecodedIr1LSB = Integer.parseInt(dataDecoded.substring(180, 184), 16);
            this.dataDecodedR2MSB = Integer.parseInt(dataDecoded.substring(185, 186), 16);
            this.dataDecodedR2LSB = Integer.parseInt(dataDecoded.substring(186, 190), 16);
            this.dataDecodedIr2MSB = Integer.parseInt(dataDecoded.substring(191, 192), 16);
            this.dataDecodedIr2LSB = Integer.parseInt(dataDecoded.substring(192, 196), 16);

            if (isFilteringOn) {
                spo2Data = this.getDataWithFilter(null,channelSelected);
            } else {
                spo2Data = this.getDataWhithoutFilter(null,channelSelected);
            }
        }
        return spo2Data;
    }

}
