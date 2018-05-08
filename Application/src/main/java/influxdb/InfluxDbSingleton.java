package influxdb;

import com.example.dataforlife.*;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;


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
        if(instance == null)
            instance = new InfluxDbSingleton();
        return instance;
    }

    private InfluxDbSingleton() {
        /* A changer plus tard pour la securité de l'application */

        String influxDbIp = "http://18.219.195.119:8086";//Resources.getSystem().getString(R.string.influx_db_ip);
        String influxDbId = "esgi";//Resources.getSystem().getString(R.string.influx_db_id);
        String influxDbPwd = "dataforlife2018";//Resources.getSystem().getString(R.string.influx_db_pwd);

        this.influxDB = InfluxDBFactory.connect(influxDbIp, influxDbId, influxDbPwd);
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
