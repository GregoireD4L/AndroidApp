package com.example.dataforlife.loggedservices;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import com.example.dataforlife.bluetoothservice.SampleGattAttributes;
import com.example.dataforlife.databaseservice.MessageProducer;
import com.example.dataforlife.display.DisplayDataAcceleroImpl;
import com.example.dataforlife.display.DisplayEcgImpl;
import com.example.dataforlife.display.DisplayRespirationImpl;
import com.example.dataforlife.display.DisplaySpO2Impl;
import com.example.dataforlife.display.DisplayTempImpl;
import com.example.dataforlife.display.IDisplayData;
import com.example.dataforlife.display.IDisplayDataWithMultipleDataSeries;
import com.example.dataforlife.model.CustomMessage;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
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
    private XyDataSeries inertialDataX;
    private XyDataSeries inertialDataY;
    private XyDataSeries inertialDataZ;
    private XyDataSeries respirationDataThorax;
    private XyDataSeries respirationDataAbdo;
    private XyDataSeries tempData;
    private XyDataSeries spo2Data;

    private IRenderableSeries ecgDataSeries;
    private IRenderableSeries inertialDataXSeries;
    private IRenderableSeries inertialDataYSeries;
    private IRenderableSeries inertialDataZSeries;
    private IRenderableSeries respirationDataThoraxSeries;
    private IRenderableSeries respirationDataAbdoSeries;
    private IRenderableSeries tempDataSeries;
    private IRenderableSeries spo2DataSeries;

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


    // classe de display

    private IDisplayData mDisplayEcgData = new DisplayEcgImpl();
    private IDisplayDataWithMultipleDataSeries mDataAccelero = new DisplayDataAcceleroImpl();
    private IDisplayDataWithMultipleDataSeries mDataRespiration = new DisplayRespirationImpl();
    private IDisplayDataWithMultipleDataSeries mDataTempArray = new DisplayTempImpl();
    private IDisplayData mDisplayspo2Data = new DisplaySpO2Impl();


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
                //nothing
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String intentData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //Connect to broker

                CustomMessage customMessage = new CustomMessage();
                customMessage.setData(intentData);
                customMessage.setId("1");
                customMessage.setTime(Instant.now().toEpochMilli());

                mMessageProducer.publishToRabbitMQ(customMessage);
                if (mServiceSelected == 1){
                    displayDataECG(intentData);
                } else if (mServiceSelected == 2){
                    displayRespiration(intentData);
                } else if (mServiceSelected == 3){
                    displayDataAccelero(intentData);
                }else if (mServiceSelected == 4){
                    displayTemp(intentData);
                } else {
                    displaySpO2(intentData);
                }
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

                        if(menuItem.getTitle().toString().equals("ECG")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_frame, new ECGFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        if(menuItem.getTitle().toString().equalsIgnoreCase("BREATHING")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_frame, new BreathingFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        if(menuItem.getTitle().toString().equalsIgnoreCase("ACCELERO")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_frame, new AcceleroFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        if(menuItem.getTitle().toString().equalsIgnoreCase("TEMPERATURE")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_frame, new TemperatureFragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }

                        if(menuItem.getTitle().toString().equalsIgnoreCase("SPO2")){
                            menuItem.setChecked(true);
                            // close drawer when item is tapped
                            mDrawerLayout.closeDrawers();
                            //start fragment
                            FragmentManager manager = getFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.replace(R.id.content_frame, new SpO2Fragment());
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }


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

    @Override
    protected void onResume() {
        super.onResume();
        /*if(mMessageProducer!= null)
            mMessageProducer.connectToRabbitMQ();*/
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBTLeService != null) {
            final boolean result = mBTLeService.connect(mDeviceAddress);
            Log.e(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //if(mMessageProducer!=null)
        //  mMessageProducer.dispose();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if(mMessageProducer!=null)
        //   mMessageProducer.dispose();
        unbindService(mServiceConnection);
        mBTLeService = null;
    }

    private void displayDataECG(String data) {

        if (data != null) {
            ArrayList<Double> dataToAddToDataSeries = mDisplayEcgData.displayData(data,isDataSave,mChannelSelected,isFilteringOn);
            for (int j = 0; j < dataToAddToDataSeries.size(); j++) {
                ecgData.append((mCompteur + j) * 2, dataToAddToDataSeries.get(j));
            }
            mCompteur += 10;
            surface.zoomExtents();
        }
    }
    private void displayDataAccelero(String data) {
        if (data != null) {

            ArrayList<Double> dataList =  mDataAccelero.displayData(data,mChannelSelected);

            mCompteur += 1;
            inertialDataX.append(20*mCompteur, dataList.get(0));
            inertialDataY.append(20*mCompteur, dataList.get(1));
            inertialDataZ.append(20*mCompteur, dataList.get(2));
            surface.zoomExtents();
        }
    }

    private void displayRespiration(String data){
        if (data != null){


            ArrayList<Double> dataList = mDataRespiration.displayData(data,mChannelSelected);
            mCompteur += 1;

            Double dataDecodedT = dataList.get(0);
            Double dataDecodedA = dataList.get(1);

            respirationDataThorax.append(20*mCompteur, dataDecodedT);
            respirationDataAbdo.append(20*mCompteur,dataDecodedA);

            /*if (wrongFrame) {
                Log.e(TAG, dataDecoded);
            }*/
            if (dataDecodedA > 1023){
                //Log.e(TAG, dataDecoded);
                wrongFrame = true;
            } else {
                wrongFrame = false;
            }
            surface.zoomExtents();
        }
    }
    private void displayTemp(String data){
        if (data != null){

            mCompteur += 1;
            tempData.append(20*mCompteur, 175.72*mDataTempArray.displayData(data,mChannelSelected).get(0)/65536 - 46.85);
        }
    }
    private void displaySpO2(String data){

        if (data != null) {

            ArrayList<Double>  dataToAddToDataSeries = mDisplayspo2Data.displayData(data,isDataSave,mChannelSelected,isFilteringOn);
            for (int j = 0; j < dataToAddToDataSeries.size(); j++) {
                ecgData.append((mCompteur + j) * 2, dataToAddToDataSeries.get(j));
            }
            mCompteur += 1;
            surface.zoomExtents();
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
                ecgData.clear();
            } else if(mServiceSelected == 2){
                inertialDataX.clear();
                inertialDataY.clear();
                inertialDataZ.clear();
            } else if(mServiceSelected == 3){
                respirationDataAbdo.clear();
                respirationDataThorax.clear();
            } else if(mServiceSelected == 4){
                tempData.removeRange(0,tempData.getCount());
            } else {
                spo2Data.clear();
            }
            mCompteur = 0;
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
        mRecord = findViewById(R.id.record_button);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    record();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

    public void executeInsideBreathingFragment(){
        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");

        surface = new SciChartSurface(this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(this);
        final SciChartBuilder builder = SciChartBuilder.instance();

        respirationDataThorax = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(350).build();
        respirationDataThoraxSeries = builder.newLineSeries()
                .withDataSeries(respirationDataThorax)
                .withStrokeStyle(ColorUtil.LightBlue, 2f, true)
                .build();

        respirationDataAbdo = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(350).build();
        respirationDataAbdoSeries = builder.newLineSeries()
                .withDataSeries(respirationDataAbdo)
                .withStrokeStyle(ColorUtil.LimeGreen, 2f, true)
                .build();

        ModifierGroup modifier = builder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .build();

        surface.getChartModifiers().add(modifier);
        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);

        mCompteur = 0;
        mServiceSelected = 2;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    public void executeInsideAcceleroFragment(){
        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");

        surface = new SciChartSurface(this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(this);
        final SciChartBuilder builder = SciChartBuilder.instance();
        inertialDataX  = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(350).build();
        inertialDataXSeries  = builder.newLineSeries()
                .withDataSeries(inertialDataX)
                .withStrokeStyle(ColorUtil.LimeGreen, 2f, true)
                .build();

        inertialDataY  = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(350).build();
        inertialDataYSeries  = builder.newLineSeries()
                .withDataSeries(inertialDataY)
                .withStrokeStyle(ColorUtil.LightBlue, 2f, true)
                .build();

        inertialDataZ  = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(350).build();
        inertialDataZSeries  = builder.newLineSeries()
                .withDataSeries(inertialDataZ)
                .withStrokeStyle(ColorUtil.Red, 2f, true)
                .build();
        ModifierGroup modifier = builder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .build();

        surface.getChartModifiers().add(modifier);
        // Application par défaut des axes

        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);
        mCompteur = 0;
        mServiceSelected = 3;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void executeInsideTemperatureFragment(){
        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");

        surface = new SciChartSurface(this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(this);
        final SciChartBuilder builder = SciChartBuilder.instance();
        tempData = builder.newXyDataSeries(Integer.class,Double.class).withFifoCapacity(350).build();
        tempDataSeries = builder.newLineSeries()
                .withDataSeries(tempData)
                .withStrokeStyle(ColorUtil.Red,2f,true)
                .build();
        ModifierGroup modifier = builder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .build();

        surface.getChartModifiers().add(modifier);
        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);
        mCompteur = 0;
        mServiceSelected = 4;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    public void executeInsideSpO2Fragment(){
        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");

        surface = new SciChartSurface(this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(this);
        final SciChartBuilder builder = SciChartBuilder.instance();
        spo2Data = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(700).build();
        spo2DataSeries = builder.newLineSeries()
                .withDataSeries(spo2Data)
                .withStrokeStyle(ColorUtil.Red, 2f, true)
                .build();


        ModifierGroup modifier = builder.newModifierGroup()
                .withPinchZoomModifier().build()
                .withZoomPanModifier().withReceiveHandledEvents(true).build()
                .withZoomExtentsModifier().withReceiveHandledEvents(true).build()
                .build();

        surface.getChartModifiers().add(modifier);
        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);
        mCompteur = 0;
        mServiceSelected = 5;
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    public void record() throws IOException {
        if(!isRecording){
            mNotifyCharacteristic = mBTLeService.getmBluetoothGatt().getService(UUID.fromString(mServiceUuid)).getCharacteristic(UUID.fromString(mCharUuid));
            mBTLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
            BluetoothGattDescriptor clientConfig = mNotifyCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mRecord.setText("Stop Recording");

            isRecording = true;
        } else {
            mNotifyCharacteristic = mBTLeService.getmBluetoothGatt().getService(UUID.fromString(mServiceUuid)).getCharacteristic(UUID.fromString(mCharUuid));
            mBTLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
            BluetoothGattDescriptor clientConfig = mNotifyCharacteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            clientConfig.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            isRecording = false;
            mRecord.setText("Record");
        }
    }

}
