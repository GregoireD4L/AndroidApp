package com.example.android.databaseservice;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.example.android.bluetoothlegatt.BluetoothLeService;

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
public class influxDbIntentService extends IntentService{


    private InfluxDbSingleton mInfluxDbSingleton = null;
    private InfluxDB mInfluxDb = null;


    public influxDbIntentService() {
        super("influxDbIntentService");
        mInfluxDbSingleton = InfluxDbSingleton.getInstance();
        mInfluxDb = mInfluxDbSingleton.getInfluxDB();
    }




    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
           String streamFromBluetooth =  intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
           /* appel des méthodes de récupération de points.
            * Récupere les points sur l'intent*/
        }
    }

    private ArrayList<Point> createPoints(String table,ArrayList<HashMap<String,Object>> fieldMap){
        ArrayList<Point> points = new ArrayList<>();
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
