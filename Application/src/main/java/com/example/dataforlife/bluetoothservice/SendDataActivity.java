package com.example.dataforlife.bluetoothservice;

import android.app.Activity;

import android.os.Bundle;

import com.example.dataforlife.R;

import influxdb.InfluxDbSingleton;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;

public class SendDataActivity extends Activity {
    private InfluxDB influxDB;
    private InfluxDbSingleton influxSingleton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        influxSingleton = InfluxDbSingleton.getInstance();
        influxDB= influxSingleton.getInfluxDB();

       /* for(int i=0; i<1500;i++){
            Point point = Point.measurement("cpu").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField("value", i).build();
            write(point);
        }*/
    }
    public void write(Point point){
        this.influxDB.write(point);
    }

    public void writeFromDoubleList(List<Double> pointList){

        BatchPoints batchPoints = BatchPoints.database(influxSingleton.getDbName()).tag("async", "true")
                .retentionPolicy(influxSingleton.getRpName())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

    }
}
