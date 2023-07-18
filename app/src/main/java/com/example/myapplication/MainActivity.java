package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";

    private static final String serviceUUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";
    private static final String characteristicUUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";
    private static final String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";


    BluetoothDevice device;
    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int RUNTIME_PERMISSION_REQUEST_CODE = 2;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;

    private int progress = 0;
    Button buttonIncrement;
    Button buttonDecrement;
    Button buttonToCharts;
    Button scanButton;

    ProgressBar progressBar;
    TextView textView;
    ScanSettings scanSettings;
    ScanFilter scanFilter;

    boolean connected = false;
    private List<ScanResult> scanResults = new ArrayList<>();

    UUID suuid;
    UUID cuuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting adapters
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();


        // setting filter
        suuid = UUID.fromString(serviceUUID);
        cuuid = UUID.fromString(characteristicUUID);
        ParcelUuid pu = new ParcelUuid(suuid);
        scanFilter = (new ScanFilter.Builder()).setServiceUuid(pu).build();
        scanSettings = (new ScanSettings.Builder()).setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();


        // BUTTON INIT
        buttonDecrement = (Button) findViewById(R.id.button_decr);
        buttonToCharts = (Button) findViewById(R.id.button_tocharts);
        buttonIncrement = (Button) findViewById(R.id.button_incr);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        textView = (TextView) findViewById(R.id.text_view_progress);
        scanButton = (Button) findViewById(R.id.scan_button);


        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!connected) {
                    scanButton.setText("Connecting");
                    startBleScan();
                }

            }
        });

        buttonToCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });
        // when clicked on buttonIncrement progress is increased by 10%
        buttonIncrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if progress is less than or equal
                // to 90% then only it can be increased
                if (progress <= 90) {
                    progress += 10;
                    updateProgressBar();
                }
            }
        });

        // when clicked on buttonIncrement progress is decreased by 10%
        buttonDecrement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If progress is greater than
                // 10% then only it can be decreased
                if (progress >= 10) {
                    progress -= 10;
                    updateProgressBar();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager)MainActivity.this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if(!lm.isLocationEnabled()){
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        if (!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }


    }


    @SuppressLint("MissingPermission")
    public void promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST_CODE:
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth();
                }
                break;
        }
    }

    ///////// PERFORMING SCAN
    @SuppressLint("MissingPermission")
    private void startBleScan() {
        if (!hasRequiredRuntimePermissions(this.getApplicationContext())) {
            requestRelevantRuntimePermissions();
        } else {
            scanResults.clear();
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();

            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);

            while (scanSettings == null);


            bleScanner.startScan(scanFilters, scanSettings, scanCallback);





        }

    }

    @SuppressLint("MissingPermission")
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int indexQuery = -1;
            for (int i = 0; i < scanResults.size(); i++) {
                if (scanResults.get(i).getDevice().getAddress().equals(result.getDevice().getAddress())) {
                    indexQuery = i;
                    break;
                }
            }

            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults.set(indexQuery, result);
            } else {
                BluetoothDevice device = result.getDevice();
                String name = device.getName() != null ? device.getName() : "Unnamed";
                String address = device.getAddress();
                Log.i("ScanCallback", "Found BLE device! Name: " + name + ", address: " + address);
                bleScanner.stopScan(scanCallback);



                device.connectGatt(MainActivity.this.getApplicationContext(), false, gattCallback);
                scanResults.add(result);
            }
        }





        @Override
        public void onScanFailed(int errorCode) {
            Log.e("ScanCallback", "onScanFailed: code " + errorCode);
        }


    };

    // GATT OPERATIONS

    @SuppressLint("MissingPermission")
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String deviceAddress = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to " + deviceAddress);


                    // MUST TO DO IN MAIN THREAD

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            gatt.discoverServices();

                            scanButton.setText("Connected");
                            connected = true;

                        }
                    });


                    // TODO: Store a reference to BluetoothGatt
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from " + deviceAddress);
                    gatt.close();
                }
            } else {
                Log.w("BluetoothGattCallback", "Error " + status + " encountered for " + deviceAddress + "! Disconnecting...");
                gatt.close();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            String incomingMessage = new String(characteristic.getValue(), StandardCharsets.UTF_8);
            Log.i("BluetoothGattCallback", "Characteristic " + uuid.toString() + " changed | value: " + incomingMessage);

            // SERVER REQUESTED DATE TIME
            if(incomingMessage.equals("-")){
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = new Date();
                String timeDate = format.format(date);


                byte[] value = timeDate.getBytes();
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
            }
            // DATE TIME AND VALUE CAME
            else{
                
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(suuid);
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(cuuid);

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                if (descriptor != null) {
                    // Enable local notifications
                    gatt.setCharacteristicNotification(characteristic, true);
                    // Enable remote notifications
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                } else {
                    throw new RuntimeException("CCC descriptor not found!");
                }
            }
        }


    };

    private void requestRelevantRuntimePermissions() {
        if (hasRequiredRuntimePermissions(this.getApplicationContext())) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            requestLocationPermission();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissions();
        }
    }

    private void requestLocationPermission() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Location permission required")
                        .setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                                "location access in order to scan for BLE devices.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        RUNTIME_PERMISSION_REQUEST_CODE
                                );
                            }
                        })
                        .show();
            }
        });
    }

    private void requestBluetoothPermissions() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Bluetooth permissions required")
                        .setMessage("Starting from Android 12, the system requires apps to be granted " +
                                "Bluetooth access in order to scan for and connect to BLE devices.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{
                                                Manifest.permission.BLUETOOTH_SCAN,
                                                Manifest.permission.BLUETOOTH_CONNECT
                                        },
                                        RUNTIME_PERMISSION_REQUEST_CODE
                                );
                            }
                        })
                        .show();
            }
        });
    }

    // updateProgressBar() method sets
    // the progress of ProgressBar in text
    private void updateProgressBar() {
        progressBar.setProgress(progress);
        textView.setText(String.valueOf(progress));
    }

    public static boolean hasPermission(Context context, String permissionType) {
        return ContextCompat.checkSelfPermission(context, permissionType) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasRequiredRuntimePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(context, Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }



    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RUNTIME_PERMISSION_REQUEST_CODE: {
                boolean containsPermanentDenial = false;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED &&
                            !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        containsPermanentDenial = true;
                        break;
                    }
                }

                boolean containsDenial = false;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        containsDenial = true;
                        break;
                    }
                }

                boolean allGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false;
                        break;
                    }
                }

                if (containsPermanentDenial) {
                    // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
                    // Note: The user will need to navigate to App Settings and manually grant
                    // permissions that were permanently denied
                } else if (containsDenial) {
                    requestRelevantRuntimePermissions();
                } else if (allGranted && hasRequiredRuntimePermissions(this.getApplicationContext())) {
                    startBleScan();
                } else {
                    // Unexpected scenario encountered when handling permissions
                    recreate();
                }
                break;
            }
        }
    }






}
