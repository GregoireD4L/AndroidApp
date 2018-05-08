package com.example.dataforlife.bluetoothservice;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dataforlife.R;
import com.example.dataforlife.databaseservice.MessageProducer;
import com.example.dataforlife.display.DisplayDataAcceleroImpl;
import com.example.dataforlife.display.DisplayEcgImpl;
import com.example.dataforlife.display.DisplayRespirationImpl;
import com.example.dataforlife.display.DisplaySpO2Impl;
import com.example.dataforlife.display.DisplayTempImpl;
import com.example.dataforlife.display.IDisplayData;
import com.example.dataforlife.display.IDisplayDataWithMultipleDataSeries;
import com.scichart.charting.model.dataSeries.XyDataSeries;
import com.scichart.charting.modifiers.ModifierGroup;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries;
import com.scichart.drawing.utility.ColorUtil;
import com.scichart.extensions.builders.SciChartBuilder;

import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import java.util.UUID;


import uk.me.berndporr.iirj.Butterworth;

public class DataDisplayActivity extends Activity {
    private final static String TAG = DataDisplayActivity.class.getSimpleName();

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

    //////////////////// Fin de la déclaration des variables globales //////////////////////////////

    ////////////////////////////// Instanciation des objets BT /////////////////////////////////////

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
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                Log.e(TAG, "bien co");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                Log.e(TAG, "Déco");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBTLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

             String intentData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                //lancer l'intentService d'influx db ici
                //Intent dataService = new Intent(context, InfluxDbIntentService.class);
                // dataService.putExtra(BluetoothLeService.EXTRA_DATA,intentData);
                //startService(dataService);



                //Connect to broker
                //mMessageProducer.connectToRabbitMQ();
                mMessageProducer.publishToRabbitMQ(intentData);


                if (mServiceSelected == 1){
                    displayDataECG(intentData);
                } else if (mServiceSelected == 2){
                    displayDataAccelero(intentData);
                } else if (mServiceSelected == 3){
                    displayRespiration(intentData);
                 }else if (mServiceSelected == 4){
                    displayTemp(intentData);
                } else {
                    displaySpO2(intentData);
                }
            }
        }
    };

    /////////////////////////// Fin de l'instanciation des objets BT ///////////////////////////////

    ////////////////////////////////// Déclaration des Méthodes ////////////////////////////////////

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_display);

        // Déclaration de la view graphe

        mMessageProducer = new MessageProducer("51.38.185.206",
                "logs",
                "fanout");

        surface = new SciChartSurface(this);
        newGraph = (LinearLayout) findViewById(R.id.newGraph);
        newGraph.addView(surface);
        SciChartBuilder.init(this);
        final SciChartBuilder builder = SciChartBuilder.instance();

        // Déclaration des conteneurs de données

        ecgData = builder.newXyDataSeries(Integer.class, Double.class).withFifoCapacity(3500).build();
        ecgDataSeries = builder.newLineSeries()
                .withDataSeries(ecgData)
                .withStrokeStyle(ColorUtil.LimeGreen, 2f, true)
                .build();

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

        tempData = builder.newXyDataSeries(Integer.class,Double.class).withFifoCapacity(350).build();
        tempDataSeries = builder.newLineSeries()
                .withDataSeries(tempData)
                .withStrokeStyle(ColorUtil.Red,2f,true)
                .build();

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

        // Application par défaut de la visualisation ECG

        surface.getRenderableSeries().add(ecgDataSeries);

        // Application par défaut des axes

        final IAxis xAxis = builder.newNumericAxis().withAxisTitle("Temps (ms)").build();
        final IAxis yAxis = builder.newNumericAxis().withAxisTitle("Potentiel").build();

        Collections.addAll(surface.getYAxes(), yAxis);
        Collections.addAll(surface.getXAxes(), xAxis);

        // Récupération des données de connexion BT dans l'intent

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mCharUuid = intent.getStringExtra(EXTRAS_CHAR_UUID);
        mServiceUuid = intent.getStringExtra(EXTRAS_SERVICE_UUID);

        // Initialisation des objets view

        mBTStatus = (TextView) findViewById(R.id.bt_status);
        mCharText = (TextView) findViewById(R.id.char_value);
        mServiceSelection = (RadioGroup) findViewById(R.id.service_selection);
        mRecord = (Button) findViewById(R.id.record_button);
        mClearGraph = (Button) findViewById(R.id.clear_graph);
        mChannelSelection = (RadioGroup) findViewById(R.id.channel_selection);
        mChannel1 = (RadioButton) findViewById(R.id.channel1);
        mChannel2 = (RadioButton) findViewById(R.id.channel2);
        mChannel3 = (RadioButton) findViewById(R.id.channel3);
        mSaveData = (CheckBox) findViewById(R.id.saveData);
        mFilterOn = (CheckBox) findViewById(R.id.filterOn);

        // Initialisation des filtres ECG

        mBtwFilterLow = new Butterworth();
        mBtwFilterLow.lowPass(4,500,40);
        mBtwFilterHigh = new Butterworth();
        mBtwFilterHigh.highPass(2,500, 0.05);

        // Initialisation des objets pour écriture dans le stockage externe

        File root = Environment.getExternalStorageDirectory();
        File file = null;

        if (root.canWrite()) {
            File dir = new File(root.getAbsolutePath() + "/Log_ECG_app");
            dir.mkdirs();
            file = new File(dir,Calendar.getInstance().getTime().toString().replace(":", "") + ".csv");
            try {
                outputStream = new FileOutputStream(file);
                Log.e(TAG,root.getAbsolutePath());
            } catch (IOException e) {
                Log.e(TAG,"Fail to open file");
            }

            Log.e(TAG,root.getAbsolutePath());
        } else {
            Log.e(TAG,"root is not writable");

        }

        // Initialisation du compteur et des données par défaut

        mCompteur = 0;
        mServiceSelected = 1;
        mChannelSelected = 1;
        isFilteringOn = false;
        isRecording = false;
        isDataSave = false;

        // Instanciation des listener

        mServiceSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.ecg){
                    if(mServiceSelected == 2){
                        clearGraph();
                        mServiceSelected = 1;
                        mChannel1.setText("Channel 1");
                        mChannel2.setText("Channel 2");
                        mChannel3.setText("Channel 3");

                        surface.getRenderableSeries().remove(inertialDataXSeries);
                        surface.getRenderableSeries().remove(inertialDataYSeries);
                        surface.getRenderableSeries().remove(inertialDataZSeries);

                        surface.getRenderableSeries().add(ecgDataSeries);
                    } else if(mServiceSelected == 3){
                        clearGraph();
                        mServiceSelected = 1;
                        mChannel1.setText("Channel 1");
                        mChannel2.setText("Channel 2");
                        mChannel3.setText("Channel 3");

                        surface.getRenderableSeries().remove(respirationDataThoraxSeries);
                        surface.getRenderableSeries().remove(respirationDataAbdoSeries);

                        surface.getRenderableSeries().add(ecgDataSeries);
                    } else if(mServiceSelected == 4){
                        clearGraph();
                        mServiceSelected = 1;
                        mChannel1.setText("Channel 1");
                        mChannel2.setText("Channel 2");
                        mChannel3.setText("Channel 3");

                        surface.getRenderableSeries().remove(tempDataSeries);

                        surface.getRenderableSeries().add(ecgDataSeries);
                    }else if(mServiceSelected == 5){
                        clearGraph();
                        mServiceSelected = 1;
                        mChannel1.setText("Channel 1");
                        mChannel2.setText("Channel 2");
                        mChannel3.setText("Channel 3");

                        surface.getRenderableSeries().remove(spo2DataSeries);

                        surface.getRenderableSeries().add(ecgDataSeries);
                    }
                }else if(i == R.id.inertial){
                    if(mServiceSelected == 1){
                        clearGraph();
                        mServiceSelected = 2;
                        mChannel1.setText("Accéléro");
                        mChannel2.setText("Gyroscope");
                        mChannel3.setText("Magnéto");

                        surface.getRenderableSeries().remove(ecgDataSeries);

                        surface.getRenderableSeries().add(inertialDataXSeries);
                        surface.getRenderableSeries().add(inertialDataYSeries);
                        surface.getRenderableSeries().add(inertialDataZSeries);
                    } else if(mServiceSelected == 3){
                        clearGraph();
                        mServiceSelected = 2;
                        mChannel1.setText("Accéléro");
                        mChannel2.setText("Gyroscope");
                        mChannel3.setText("Magnéto");

                        surface.getRenderableSeries().remove(respirationDataThoraxSeries);
                        surface.getRenderableSeries().remove(respirationDataAbdoSeries);

                        surface.getRenderableSeries().add(inertialDataXSeries);
                        surface.getRenderableSeries().add(inertialDataYSeries);
                        surface.getRenderableSeries().add(inertialDataZSeries);

                    } else if(mServiceSelected == 4){
                        clearGraph();
                        mServiceSelected = 2;
                        mChannel1.setText("Accéléro");
                        mChannel2.setText("Gyroscope");
                        mChannel3.setText("Magnéto");

                        surface.getRenderableSeries().remove(tempDataSeries);

                        surface.getRenderableSeries().add(inertialDataXSeries);
                        surface.getRenderableSeries().add(inertialDataYSeries);
                        surface.getRenderableSeries().add(inertialDataZSeries);
                    }else if(mServiceSelected == 5) {
                        clearGraph();
                        mServiceSelected = 2;
                        mChannel1.setText("Accéléro");
                        mChannel2.setText("Gyroscope");
                        mChannel3.setText("Magnéto");

                        surface.getRenderableSeries().remove(spo2DataSeries);

                        surface.getRenderableSeries().add(inertialDataXSeries);
                        surface.getRenderableSeries().add(inertialDataYSeries);
                        surface.getRenderableSeries().add(inertialDataZSeries);
                    }
                } else if(i == R.id.respiration){
                    if(mServiceSelected == 1){
                        clearGraph();
                        mServiceSelected = 3;
                        mChannel1.setText("No choice");
                        mChannel2.setText("No choice");
                        mChannel3.setText("No choice");

                        surface.getRenderableSeries().remove(ecgDataSeries);

                        surface.getRenderableSeries().add(respirationDataThoraxSeries);
                        surface.getRenderableSeries().add(respirationDataAbdoSeries);
                    } else if(mServiceSelected == 2){
                        clearGraph();
                        mServiceSelected = 3;
                        mChannel1.setText("No choice");
                        mChannel2.setText("No choice");
                        mChannel3.setText("No choice");

                        surface.getRenderableSeries().remove(inertialDataXSeries);
                        surface.getRenderableSeries().remove(inertialDataYSeries);
                        surface.getRenderableSeries().remove(inertialDataZSeries);

                        surface.getRenderableSeries().add(respirationDataThoraxSeries);
                        surface.getRenderableSeries().add(respirationDataAbdoSeries);
                    } else if(mServiceSelected == 4){
                        clearGraph();
                        mServiceSelected = 3;
                        mChannel1.setText("No choice");
                        mChannel2.setText("No choice");
                        mChannel3.setText("No choice");

                        surface.getRenderableSeries().remove(tempDataSeries);

                        surface.getRenderableSeries().add(respirationDataThoraxSeries);
                        surface.getRenderableSeries().add(respirationDataAbdoSeries);
                    }else if(mServiceSelected == 5){
                        clearGraph();
                        mServiceSelected = 3;
                        mChannel1.setText("No choice");
                        mChannel2.setText("No choice");
                        mChannel3.setText("No choice");

                        surface.getRenderableSeries().remove(spo2DataSeries);

                        surface.getRenderableSeries().add(respirationDataThoraxSeries);
                        surface.getRenderableSeries().add(respirationDataAbdoSeries);
                    }
                }else if(i == R.id.temperature){
                        if(mServiceSelected == 1){
                            clearGraph();
                            mServiceSelected = 4;
                            mChannel1.setText("No choice");
                            mChannel2.setText("No choice");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(ecgDataSeries);

                            surface.getRenderableSeries().add(tempDataSeries);
                        } else if(mServiceSelected == 2){
                            clearGraph();
                            mServiceSelected = 4;
                            mChannel1.setText("No choice");
                            mChannel2.setText("No choice");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(inertialDataXSeries);
                            surface.getRenderableSeries().remove(inertialDataYSeries);
                            surface.getRenderableSeries().remove(inertialDataZSeries);

                            surface.getRenderableSeries().add(tempDataSeries);
                        } else if(mServiceSelected == 3){
                            clearGraph();
                            mServiceSelected = 4;
                            mChannel1.setText("No choice");
                            mChannel2.setText("No choice");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(respirationDataThoraxSeries);
                            surface.getRenderableSeries().remove(respirationDataAbdoSeries);

                            surface.getRenderableSeries().add(tempDataSeries);
                        }else if(mServiceSelected == 5){
                            clearGraph();
                            mServiceSelected = 4;
                            mChannel1.setText("No choice");
                            mChannel2.setText("No choice");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(spo2DataSeries);

                            surface.getRenderableSeries().add(tempDataSeries);
                        }
                    } else {
                        if(mServiceSelected == 1){
                            clearGraph();
                            mServiceSelected = 5;
                            mChannel1.setText("Red");
                            mChannel2.setText("IR");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(ecgDataSeries);

                            surface.getRenderableSeries().add(spo2DataSeries);

                        } else if(mServiceSelected == 2){
                            clearGraph();
                            mServiceSelected = 5;
                            mChannel1.setText("Red");
                            mChannel2.setText("IR");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(inertialDataXSeries);
                            surface.getRenderableSeries().remove(inertialDataYSeries);
                            surface.getRenderableSeries().remove(inertialDataZSeries);

                            surface.getRenderableSeries().add(spo2DataSeries);
                        } else if(mServiceSelected == 3){
                            clearGraph();
                            mServiceSelected = 5;
                            mChannel1.setText("Red");
                            mChannel2.setText("IR");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(respirationDataThoraxSeries);
                            surface.getRenderableSeries().remove(respirationDataAbdoSeries);

                            surface.getRenderableSeries().add(spo2DataSeries);
                        } else if(mServiceSelected == 4){
                            clearGraph();
                            mServiceSelected = 5;
                            mChannel1.setText("Red");
                            mChannel2.setText("IR");
                            mChannel3.setText("No choice");

                            surface.getRenderableSeries().remove(tempDataSeries);

                            surface.getRenderableSeries().add(spo2DataSeries);
                        }
                    }
            }
        });

        mSaveData.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    isDataSave = true;
                    Log.e(TAG,"Data are saved");
                } else {
                    isDataSave = false;
                    Log.e(TAG,"Data are not saved");
                }
            }
        });

        mFilterOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    isFilteringOn = true;
                    Log.e(TAG,"Data are filtered");
                } else {
                    isFilteringOn = false;
                    Log.e(TAG, "Data are not filtered");
                }
            }
        });

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

        mClearGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearGraph();
            }
        });

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

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        mCharText.setText(mCharUuid);
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


    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBTStatus.setText(resourceId);
            }
        });
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

    private void displayDataAccelero(String data) {
        if (data != null) {

            ArrayList<Double> dataList =  mDataAccelero.displayData(data,mChannelSelected);

            mCompteur += 1;
            inertialDataX.append(20*mCompteur, dataList.get(0));
            inertialDataY.append(20*mCompteur, dataList.get(1));
            inertialDataZ.append(20*mCompteur, dataList.get(2));

            surface.zoomExtents();
        }
    };

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
            surface.zoomExtents();
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

    private void clearUI() {
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
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
            //Toast.makeText(this, outputStream.getFD().toString(), Toast.LENGTH_SHORT);
            mRecord.setText("Record");
        }
    }

    public void clearGraph() {
        if(mCompteur !=0){
            if(mServiceSelected == 1) {
                ecgData = this.mDisplayEcgData.clearGraph(ecgData);
            } else if(mServiceSelected == 2){
                inertialDataX.removeRange(0, inertialDataX.getCount());
                inertialDataY.removeRange(0, inertialDataY.getCount());
                inertialDataZ.removeRange(0, inertialDataZ.getCount());
            } else if(mServiceSelected == 3){
                respirationDataAbdo.removeRange(0, respirationDataAbdo.getCount());
                respirationDataThorax.removeRange(0,respirationDataThorax.getCount());
            } else if(mServiceSelected == 4){
                tempData.removeRange(0,tempData.getCount());
            } else {
                spo2Data = this.mDisplayspo2Data.clearGraph(spo2Data);
            }
            mCompteur = 0;
        } else {
            Toast.makeText(this, "Graph is already cleared", Toast.LENGTH_SHORT);
        }
    }

}
