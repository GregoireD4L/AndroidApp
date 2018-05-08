package com.example.dataforlife.display;

import android.util.Log;

import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.extensions.builders.SciChartBuilder;

import java.io.IOException;
import java.util.ArrayList;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Created by kokoghlanian on 20/03/2018.
 */

public class DisplayEcgImpl implements IDisplayData {

    private Butterworth mBtwFilterLow;
    private Butterworth mBtwFilterHigh;

    public DisplayEcgImpl() {

        mBtwFilterLow = new Butterworth();
        mBtwFilterLow.lowPass(4, 500, 40);
        mBtwFilterHigh = new Butterworth();
        mBtwFilterHigh.highPass(2, 500, 0.05);
    }


    @Override
    public ArrayList<Double> getDataFromString(String data) {

        ArrayList<Double> mDataList = new ArrayList<Double>();
        if (data != null) {
            String[] dataList = data.split("\n");
            String dataDecoded = dataList[dataList.length - 1].replace(" ", "");
            for (int i = 0; i < 30; i++) {
                double mPoint = (double) Integer.parseInt(dataDecoded.substring(4 * i, 4 * i + 4), 16) * 2.4 / (32768 - 1);
                if (mPoint > 2.4) {
                    mDataList.add((-4.8 + mPoint));
                } else {
                    mDataList.add(mPoint);
                }
            }
        }
        return mDataList;
    }

    @Override
    public ArrayList<Double> getDataWhithoutFilter(ArrayList<Double> dataList, int channelSelected){
        ArrayList<Double> dataFromChannel = new ArrayList<>();
        if(dataList.size()> 0){
            for(int i = 0; i < 10; i++){
                dataFromChannel.add(dataList.get(3 * i + channelSelected - 1));
            }
        }
        return dataFromChannel;
    }

    @Override
    public ArrayList<Double> getDataWithFilter(ArrayList<Double> dataList, int channelSelected){
        ArrayList<Double> dataFromChannel = new ArrayList<>();
        if(dataList.size()> 0){
            for(int i = 0; i < 10; i++){
                dataFromChannel.add(mBtwFilterLow.filter(mBtwFilterHigh.filter(dataList.get(3 * i + channelSelected - 1))));
            }
        }
        return dataFromChannel;
    }


    /* Methode qui ne servira surement plus servait a récupéré les XyDataSeries, qui ne sont pas utile pour nos traitement

    public XyDataSeries createDataFromDouble(boolean isFilteringOn, ArrayList<Double> mDataList, int channelSelected, int compteur) {

        final SciChartBuilder builder = SciChartBuilder.instance();
        XyDataSeries ecgData = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(15).build();

        for (int j = 0; j < 10; j++) {
            if (isFilteringOn) {
                ecgData.append((compteur + j) * 2, mBtwFilterLow.filter(mBtwFilterHigh.filter(mDataList.get(3 * j + channelSelected - 1))));

            } else {
                ecgData.append((compteur + j) * 2, mDataList.get(3 * j + channelSelected - 1));
            }

        }
        return ecgData;
    }*/

    @Override
    public ArrayList<Double> displayData(String data, boolean isDataSave, int channelSelected, boolean isFilteringOn) {

        ArrayList<Double> ecgData;
        ArrayList<Double> dataList = this.getDataFromString(data);
        if (isFilteringOn) {
            //revoir gestion de la sauvegarde sur csv plus tard;
            ecgData = getDataWhithoutFilter(dataList,channelSelected);
        } else {
            ecgData = getDataWithFilter(dataList,channelSelected);
        }
        return ecgData;
    }
}
