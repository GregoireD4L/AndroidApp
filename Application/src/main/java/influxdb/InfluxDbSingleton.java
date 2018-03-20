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
    private static InfluxDbSingleton instance=null;

    public static InfluxDbSingleton getInstance()
    {
        if(instance==null)
            instance= new InfluxDbSingleton();
        return instance;
    }

    private InfluxDbSingleton() {
        String influxDbIp = Resources.getSystem().getString(R.string.influx_db_ip);
        String influxDbId = Resources.getSystem().getString(R.string.influx_db_id);
        String influxDbPwd = Resources.getSystem().getString(R.string.influx_db_pwd);
        this.influxDB = InfluxDBFactory.connect(influxDbIp,influxDbId,influxDbPwd);
    }

    public InfluxDB getInfluxDB(){
        return influxDB;
    }

    public String getDbName(){
        return dbName;
    }
    public String getRpName(){
        return rpName;
    }
}
