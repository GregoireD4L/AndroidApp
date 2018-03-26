package com.example.android.databaseservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.example.android.bluetoothlegatt.BluetoothLeService;
import com.example.android.display.DisplayDataAcceleroImpl;
import com.example.android.display.DisplayEcgImpl;
import com.example.android.display.DisplayRespirationImpl;
import com.example.android.display.DisplaySpO2Impl;
import com.example.android.display.DisplayTempImpl;
import com.example.android.display.IDisplayData;
import com.example.android.display.IDisplayDataWithMultipleDataSeries;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import influxdb.InfluxDbSingleton;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 *
 * Intent Service pour la gestion de la création des points
 */
public class InfluxDbIntentService extends IntentService{


    private InfluxDbSingleton mInfluxDbSingleton = null;
    private InfluxDB mInfluxDb = null;
    private IDisplayData ecgToInflux = null;
    private IDisplayData spo2ToInflux = null;
    private IDisplayDataWithMultipleDataSeries tempToInflux = null;
    private IDisplayDataWithMultipleDataSeries respiToInflux = null;
    private IDisplayDataWithMultipleDataSeries accToInflux = null;


    public InfluxDbIntentService() {
        super("InfluxDbIntentService");
        mInfluxDbSingleton = InfluxDbSingleton.getInstance();
        mInfluxDb = mInfluxDbSingleton.getInfluxDB();

        ecgToInflux = new DisplayEcgImpl();
        spo2ToInflux = new DisplaySpO2Impl();
        tempToInflux = new DisplayTempImpl();
        respiToInflux =  new DisplayRespirationImpl();
        accToInflux = new DisplayDataAcceleroImpl();
    }

    /* appel des méthodes de récupération de points.
     * Récupere les points sur l'intent
     * A refactoriser*/

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
           String streamFromBluetooth =  intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            this.createEcgPointInInflux(streamFromBluetooth);
        }
    }

    private void createEcgPointInInflux(String ecgData){
       if(ecgData != null)
       {
           List<Double> ecgDataList = ecgToInflux.displayData(ecgData,false,1,false);
           if(!ecgDataList.isEmpty())
           {
              ArrayList<HashMap<String,Object>> ecgMap = createHashMapForPointList(ecgDataList);
              if(!ecgData.isEmpty())
              {
                  List<Point> points = createPoints("ecgChannelOne",ecgMap);
                  if(!points.isEmpty())
                  {
                      BatchPoints batchPointsEcg = createBatchPoints(points);
                      writeInInfluxWithBatchPoints(batchPointsEcg);
                  }
              }
           }
       }
    }

    private ArrayList<HashMap<String,Object>> createHashMapForPointList(List<Double> points){
        ArrayList<HashMap<String,Object>> resultArray = new ArrayList<>();
        for(Double pointValue : points){
            HashMap<String,Object> ecgMap = new HashMap<>();
            ecgMap.put("ecgvalue",pointValue);
            resultArray.add(ecgMap);
        }
       return resultArray;
    }

    private List<Point> createPoints(String table,ArrayList<HashMap<String,Object>> fieldMap){
        List<Point> points = new ArrayList<>();
        for(int i = 0; i < fieldMap.size(); i++){
            points.add(this.createPoint(table,fieldMap.get(i)));
        }
        return points;
    }

    private Point createPoint(String table, HashMap<String,Object> fieldMap){

        Point point = Point.measurement(table)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .fields(fieldMap).build();

        return point;
    }


    private BatchPoints createBatchPoints(List<Point> pointList)
    {
        BatchPoints batchPoints = BatchPoints.database(this.mInfluxDbSingleton.getDbName()).tag("async", "true")
                .retentionPolicy(this.mInfluxDbSingleton.getRpName())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for (Point pointToAdd : pointList){
            batchPoints.point(pointToAdd);
        }

        return batchPoints;
    }

    private void writeInInfluxWithPoint(Point point){
        this.mInfluxDb.write(point);
    }

    private void writeInInfluxWithBatchPoints(BatchPoints batchPoints){
        this.mInfluxDb.write(batchPoints);
    }


}
