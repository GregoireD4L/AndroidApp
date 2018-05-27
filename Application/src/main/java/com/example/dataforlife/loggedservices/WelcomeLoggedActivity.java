package com.example.dataforlife.loggedservices;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dataforlife.R;
import com.example.dataforlife.bluetoothservice.BluetoothLeService;
import com.example.dataforlife.databaseservice.MessageProducer;
import com.example.dataforlife.display.DisplayEcgImpl;
import com.example.dataforlife.display.IDisplayData;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import uk.me.berndporr.iirj.Butterworth;

/**
 * Author Yousria
 */

public class WelcomeLoggedActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private final static String TAG = ECGFragment.class.getSimpleName();

    // Propriétés du service connecté
    private String mDeviceName;
    private String mDeviceAddress;
    private String mCharUuid;
    private String mServiceUuid;
    // Selection des données à voir
    private int mServiceSelected;
    private int mChannelSelected;

    // Selection des paramètres
    private boolean isFilteringOn;
    private boolean isDataSave;

    // Lancement de l'enregistrement
    private boolean isRecording;

    // Objets de visualisation
    private int mCompteur;
    private SciChartSurface surface;

    private XyDataSeries ecgData;

    private IRenderableSeries ecgDataSeries;

    // Objet de sauvegarde des données
    private FileOutputStream outputStream;

    // Objets view
    private RadioGroup mServiceSelection;
    private Button mRecord;
    private Button mClearGraph;
    private RadioGroup mChannelSelection;
    private RadioButton mChannel1;
    private RadioButton mChannel2;
    private RadioButton mChannel3;
    private TextView mBTStatus;
    private TextView mCharText;
    private CheckBox mSaveData;
    private CheckBox mFilterOn;
    private LinearLayout newGraph;

    // Objets BlueTooth
    private BluetoothLeService mBTLeService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected = false;

    // Filtres
    private Butterworth mBtwFilterLow;
    private Butterworth mBtwFilterHigh;

    // Clé de l'intent
    private static final String EXTRAS_DEVICE_NAME = "NAME";
    private static final String EXTRAS_DEVICE_ADDRESS = "ADDRESS";
    private static final String EXTRAS_CHAR_UUID = "CHAR_UUID";
    private static final String EXTRAS_SERVICE_UUID = "SERVICE_UUID";

    private boolean wrongFrame = false;

    // RABITTMQServices
    private MessageProducer mMessageProducer = null;

    private IDisplayData mDisplayEcgData = new DisplayEcgImpl();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBTLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBTLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBTLeService.connect(mDeviceAddress);
            if(mBTLeService == null){
                Log.e(TAG, "null");
            } else {
                Log.e(TAG, "bien" + mBTLeService.getmBluetoothGatt().getService(UUID.fromString(mServiceUuid)).getCharacteristic(UUID.fromString(mCharUuid)).getUuid().toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBTLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG, "bien co");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                Log.e(TAG, "Déco");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

                String intentData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //lancer l'intentService d'influx db ici
                //Intent dataService = new Intent(context, InfluxDbIntentService.class);
                // dataService.putExtra(BluetoothLeService.EXTRA_DATA,intentData);
                //startService(dataService);



                //Connect to broker
                //mMessageProducer.connectToRabbitMQ();
                mMessageProducer.publishToRabbitMQ(intentData);
                displayDataECG(intentData);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_logged);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu_icon);
        toolbar.setNavigationIcon(R.drawable.menu_icon);
        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mCharUuid = intent.getStringExtra(EXTRAS_CHAR_UUID);
        mServiceUuid = intent.getStringExtra(EXTRAS_SERVICE_UUID);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        if(menuItem.getTitle().toString().equals("ECG")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.add(R.id.content_frame, new ECGFragment(), ECGFragment.class.getSimpleName());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayDataECG(String data) {

        if (data != null) {
            ArrayList<Double> dataToAddToDataSeries = mDisplayEcgData.displayData(data,isDataSave,mChannelSelected,isFilteringOn);
            for (int j = 0; j < dataToAddToDataSeries.size(); j++) {
                ecgData.append((mCompteur + j) * 2, dataToAddToDataSeries.get(j));
            }
            surface.zoomExtents();
            mCompteur += 10;
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void clearGraph() {
        if(mCompteur !=0){
            if(mServiceSelected == 1) {
                ecgData = this.mDisplayEcgData.clearGraph(ecgData);
            }
            mCompteur = 0;
        } else {
            Toast.makeText(this, "Graph is already cleared", Toast.LENGTH_SHORT);
        }
    }

    public void executeInsideEcgFragment(){
        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");
        surface = new SciChartSurface(WelcomeLoggedActivity.this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(WelcomeLoggedActivity.this);
        final SciChartBuilder builder = SciChartBuilder.instance();

        ecgData = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(3500).build();
        ecgDataSeries = builder.newLineSeries()
                .withDataSeries(ecgData)
                .withStrokeStyle(ColorUtil.rgb(17, 193, 255), 2f, true)
                .build();

        ModifierGroup modifier = builder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .build();

        surface.getChartModifiers().add(modifier);
        // Application par défaut de la visualisation ECG

        surface.getRenderableSeries().add(ecgDataSeries);
        // Application par défaut des axes

        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);

        // Récupération des données de connexion BT dans l'intent

        mChannelSelection = (RadioGroup) findViewById(R.id.channel_selection);
        mChannel1 = (RadioButton) findViewById(R.id.channel1);
        mChannel2 = (RadioButton) findViewById(R.id.channel2);
        mChannel3 = (RadioButton) findViewById(R.id.channel3);
        // Initialisation des filtres ECG

        mBtwFilterLow = new Butterworth();
        mBtwFilterLow.lowPass(4,500,40);
        mBtwFilterHigh = new Butterworth();
        mBtwFilterHigh.highPass(2,500, 0.05);

        mCompteur = 0;
        mServiceSelected = 1;
        mChannelSelected = 1;
        isFilteringOn = false;
        isRecording = false;
        isDataSave = false;

        mChannelSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.channel1){
                    if(mChannelSelected !=1) {
                        mChannelSelected = 1;
                        clearGraph();
                    }
                } else if(i == R.id.channel2){
                    if(mChannelSelected !=2) {
                        mChannelSelected = 2;
                        clearGraph();
                    }
                } else {
                    if(mChannelSelected !=3) {
                        mChannelSelected = 3;
                        clearGraph();
                    }
                }
            }
        });

        Intent gattServiceIntent = new Intent(WelcomeLoggedActivity.this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


    }
}
