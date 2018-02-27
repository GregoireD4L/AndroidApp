package influxdb;

import android.content.res.Resources;

import com.example.android.bluetoothlegatt.R;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.List;


/**
 * Created by kokoghlanian on 22/02/2018.
 */

public class InfluxDbSingleton {

    private InfluxDB  influxDB = null;
    private static String dbName = "dataforlifeDB";
    private static String rpName = "aRetentionPolicy";
    private static final InfluxDbSingleton ourInstance = new InfluxDbSingleton();

    public static InfluxDbSingleton getInstance() {
        return ourInstance;
    }

    private InfluxDbSingleton() {
        String influxDbIp = Resources.getSystem().getString(R.string.influx_db_ip);
        String influxDbId = Resources.getSystem().getString(R.string.influx_db_id);
        String influxDbPwd = Resources.getSystem().getString(R.string.influx_db_pwd);
        this.influxDB = InfluxDBFactory.connect(influxDbIp,influxDbId,influxDbPwd);
    }

    public void write(Point point){
        this.influxDB.write(point);
    }

    public void writeFromDoubleList(List<Double> pointList){

        BatchPoints batchPoints = BatchPoints.database(this.dbName).tag("async", "true")
                .retentionPolicy(rpName)
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

    }
}
