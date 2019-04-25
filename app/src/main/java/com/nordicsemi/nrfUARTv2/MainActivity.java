
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/* BEE 484 Water Monitoring System
   Depending on sensor value suggests maintenance
 */

package com.nordicsemi.nrfUARTv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    // kiem tra lai cac so
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    private static final int CREATE_REQUEST_CODE = 40;
    private static final int OPEN_REQUEST_CODE = 41;
    private static final int SAVE_REQUEST_CODE = 42;

    //private static EditText textView;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;   // The BluetoothAdapter is required for any and all Bluetooth activity
    private Button btnConnectDisconnect,btnSaveData,btnViewSaveData;
    int lengthTxtValue = 14;
    double[] ECG_dataIn;
    double[] outPut;
    byte[] txValue;
    int[] a = new int[lengthTxtValue];
    double[] receivedData = new double[lengthTxtValue/2];
    double [] firstDataBuffer;

    private LineGraphSeries<DataPoint> series_fetal;
    private LineGraphSeries<DataPoint> series_maternal;
    private int lastX1 = 0;
    private int lastX2 = 0;
    boolean isRunning = false;
    boolean isConnect = false;
    boolean isSaving  = false;

    ArrayList<Double> data1Save = new ArrayList<Double>();
    ArrayList<Double> data2Save = new ArrayList<Double>();
    ArrayList<Double> data3Save = new ArrayList<Double>();
    ArrayList<Double> data4Save = new ArrayList<Double>();
    ArrayList<Double> data5Save = new ArrayList<Double>();
    ArrayList<Double> data6Save = new ArrayList<Double>();
    ArrayList<Double> data7Save = new ArrayList<Double>();
    SaveData saver = new SaveData();
    IIR_Filter filter = new IIR_Filter();

    double[] filter_input1 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    double[] filter_output1 = {0, 0, 0, 0, 0, 0, 0, 0};
    double[] filter_input2 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
    double[] filter_output2 = {0, 0, 0, 0, 0, 0, 0, 0};

    @Override
    // create
    public void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView (R.layout.main);

        initGraphFetal();
        initGraphMaternal();
        btnSaveData = findViewById(R.id.btn_saveData);
        btnViewSaveData = findViewById(R.id.btn_viewSaveData);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // lay gia tri default ban dau la null


        // vơi gia tri ban dau la null, bluetooth khong hoat dong
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnConnectDisconnect=  findViewById(R.id.btn_connect);
        service_init();

        // Handle Save data function
        btnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data1Save.size() == 0 && data2Save.size() == 0) { Toast.makeText(getApplicationContext(), "No data available yet.", Toast.LENGTH_SHORT).show();}

                else {
                    isSaving = true;
                    isRunning = false;
                    mService.disconnect();
                    AlertDialog saved = new AlertDialog.Builder (MainActivity.this).setTitle (R.string.popup_title2).setMessage (R.string.popup_message2).setNeutralButton (R.string.popup_overwrite, null).setNegativeButton (R.string.popup_new, new DialogInterface.OnClickListener () {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            saver.save (data1Save, "-RAW ECG.txt");
                            saver.save (data2Save, "-AccX.txt");
                            saver.save (data3Save, "-AccY.txt");
                            saver.save (data4Save, "-AccZ.txt");
                            saver.save (data5Save, "-GyoX.txt");
                            saver.save (data6Save, "-GyoY.txt");
                            saver.save (data7Save, "-GyoZ.txt");
                            Toast.makeText (getApplicationContext (), "Saved", Toast.LENGTH_SHORT).show ();
                            isSaving = false;
                            isRunning = true;
                        }
                    })

                            .setPositiveButton (R.string.popup_cancel, null).show ();
                }
            }
        });

        // Handle Viewing of saved data function
        btnViewSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
            			Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class); // chuyen qua device list activity
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
            			// Start Thread for displaying data
                       //new FilterThread(handler).start();


        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();

                            //resetData();
        				}
        			}
                }
            }
        });

    }
    private void resetData(){
        isRunning = false;
        data1Save.clear();
        data2Save.clear();
        data3Save.clear();
        data4Save.clear();
        data5Save.clear();
        data6Save.clear();
        data7Save.clear();
    }

    private void initGraphMaternal(){
        // we get graph view instance
        GraphView graph =  findViewById(R.id.graph);
        // data
        series_maternal = new LineGraphSeries<DataPoint>();
            graph.addSeries(series_maternal);
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
            viewport.setYAxisBoundsManual(true);
            viewport.setMinY(-2);
            viewport.setMaxY(2);
            viewport.setMinX(-1000);
            viewport.setMaxX(1000);
            viewport.setScrollable(false);
        //graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
            graph.getGridLabelRenderer().setNumHorizontalLabels(40);
            graph.getGridLabelRenderer().setNumVerticalLabels(20);
            graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
            graph.setTitle("Maternal ECG");
        // viewport
    }

    private void initGraphFetal(){
        // we get graph view instance
        GraphView graph2 = findViewById(R.id.graph2);
        // data
        series_fetal = new LineGraphSeries();
        graph2.addSeries(series_fetal);
        // customize a little bit viewport
        Viewport viewport2 = graph2.getViewport();
        viewport2.setYAxisBoundsManual(true);
        viewport2.setMinY(-2);
        viewport2.setMaxY(2);
        viewport2.setMinX(-1000);
        viewport2.setMaxX(1000);
        viewport2.setScrollable(false);
        //graph2.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph2.getGridLabelRenderer().setNumHorizontalLabels(40);
        graph2.getGridLabelRenderer().setNumVerticalLabels(20);
        graph2.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph2.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph2.setTitle("Fetal ECG");
        // viewport.
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        		mService = ((UartService.LocalBinder) rawBinder).getService();
        		Log.d(TAG, "onServiceConnected mService= " + mService);
        		if (!mService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }

        }

        public void onServiceDisconnected(ComponentName classname) {
       ////     mService.disconnect(mDevice);
        		mService = null;
        }
    };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_CONNECT_MSG");
                             btnConnectDisconnect.setText("Disconnect");
                             if(!isSaving) {Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();}
                            //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - ready");
                             //Toast.makeText(getApplicationContext(), "["+currentDateTimeString+"] Connected to: "+ mDevice.getName(),Toast.LENGTH_LONG).show();
                             mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }

          //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             if(!isSaving) {Toast.makeText(getApplicationContext(), "Disconnected", Toast.LENGTH_LONG).show();}
                         //   ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                            // Toast.makeText(getApplicationContext(), "["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName(),Toast.LENGTH_LONG).show();
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                             //resetData();
                             isRunning = false;

                     }
                 });
            }


          //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
            }
          //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
               // firstDataBuffer = new double[3000];
                for (int i = 0; i < txValue.length; i++) {
                    a[i] = 0xFF & txValue[i];
                    // Log.d(Utils.MTAG, "value a=" + a[i]);
                }
                for (int i = 0; i < txValue.length / 2; i++) {
                    if (i == 0)
                    {
                        receivedData[i] = (256 * a[2 * i] + a[(2 * i) + 1]);
                    }
                    else
                        receivedData[i] = (short)(a[2 * i] << 8 | a[(2 * i) + 1]);

                    data1Save.add(receivedData[0]);     // fill save array for ECG signal raw data
                    data2Save.add(receivedData[1]);     // fill save array for AccX signal raw data
                    data3Save.add(receivedData[2]);     // fill save array for AccY signal raw data
                    data4Save.add(receivedData[3]);     // fill save array for AccZ signal raw data
                    data5Save.add(receivedData[4]);     // fill save array for GyoX signal raw data
                    data6Save.add(receivedData[5]);     // fill save array for GyoY signal raw data
                    data7Save.add(receivedData[6]);     // fill save array for GyoZ signal raw data
                    // IIR Bandpass filter for 1st input
                    filter_input1 = filter.update_input_filter_array(filter_input1, receivedData[0]);
                    double filtered_point1 = filter.filter(filter_input1, filter_output1);
                    filter_output1 = filter.update_output_filter_array(filter_output1, filtered_point1);

                    // IIR Bandpass filter for 2nd input
                    filter_input2 = filter.update_input_filter_array(filter_input2, receivedData[1]);
                    double filtered_point2 = filter.filter(filter_input2, filter_output2);
                    filter_output2 = filter.update_output_filter_array(filter_output2, filtered_point2);

                    // plot the filtered data points or raw data
                    series_maternal.appendData(new DataPoint(lastX1++, receivedData[0] / 700), true, 1200);
                    series_fetal.appendData(new DataPoint(lastX2++, receivedData[3] / 700), true, 1200);

                }
             }
           //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
                if(!isSaving) {
                    showMessage("Device doesn't support UART. Disconnecting");
                    mService.disconnect();
                }
            }
        }
    };

    private class CalHR extends Thread{
        private Handler mHandler;
        public CalHR(Handler handler){
            this.mHandler = handler;
        }
        @Override
        public void run() {
            super.run();
        }
    }

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                  //  ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                    mService.connect(deviceAddress);
                    resetData();
                    isRunning = true;

                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;

            //case CREATE_REQUEST_CODE:

            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.popup_title1)
                    .setMessage(R.string.popup_message)
                    .setNegativeButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.popup_no, null)
                    .show();
        }
    }
}