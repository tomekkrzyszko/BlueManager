package pl.tomek_krzyszko.bluemanager.scanner;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.dagger.components.TaskComponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

public class BlueScannerTask implements Runnable {

    @Inject BlueConfig blueConfig;

    @Inject Context context;

    /**
     * Used in API level < 21 for scanning
     * Used in all API levels for managing device's Bluetooth
     */
    @Inject BluetoothAdapter bluetoothAdapter;

    private BlueScanner blueScanner;
    TaskComponent component;

    /**
     * Whether or not the scanner is running on a device with API level < 21
     * Bluetooth Low Energy was introduced in Android 4.3 (API level 18)
     * and changed in Android 5.0 (API level 21).
     * {@link BlueScanner} detects system version and runs appropriate code.
     */
    private boolean isLegacy = false;

    /**
     * Whether or not internal scanner object and its callback has been initialized.
     * Scanning cannot be started without initialization.
     */
    private boolean initialized;

    /**
     * Whether or not {@link BlueScanner}'s scanning worker thread is allowed to run.
     */
    private boolean isRunning = true;

    /**
     * Used in API level >= 21 for scanning
     */
    private BluetoothLeScanner bluetoothLeScanner;

    /**
     * Scan callback used in API level < 21.
     */
    private BluetoothAdapter.LeScanCallback legacyScanCallback;

    /**
     * Scan callback used in API level >= 21.
     */
    private ScanCallback scanCallback;

    public BlueScannerTask(BlueScanner blueScanner) {
        if (component == null) {
            component = BlueManager.getInstance()
                    .getComponent()
                    .module(new TaskModule(blueScanner));
        }
        this.blueScanner = blueScanner;
    }

    /**
     * Initializes scan callback used in Android devices with API level < 21.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initLegacyScanner() {
        isLegacy = true;

        legacyScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] rawScanRecord) {
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        onScan(device, rssi);
                        return null;
                    }

                }.execute();
            }
        };

        initialized = true;
    }

    /**
     * Starts Bluetooth Low Energy scanning on Android devices with API level < 21.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startLegacyScanning() {
        bluetoothAdapter.startLeScan(legacyScanCallback);
    }

    /**
     * Stops Bluetooth Low Energy scanning on Android devices with API level < 21.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopLegacyScanning() {
        if (bluetoothAdapter != null) {
            try { //sometimes throw npe from os.Parcel.eadException() - only try catch is a solution
                bluetoothAdapter.startLeScan(legacyScanCallback);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes scan callback used in Android devices with API level >= 21.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initScanner() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        isLegacy = false;

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, final ScanResult result) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        BluetoothDevice device = result.getDevice();
                        if (result.getScanRecord() != null) {
                            try { //#80 - exception caused by internal sdk function - only try catch is a solution
                                onScan(device, result.getRssi());
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }


                }.execute();
            }

            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
                for (ScanResult result : results) {
                    this.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                blueScanner.onFailure(errorCode);
            }
        };

        initialized = true;
    }

    /**
     * Starts Bluetooth Low Energy scanning on Android devices with API level >= 21.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP | Build.VERSION_CODES.M)
    private void startScanning() {
        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        if (bluetoothLeScanner != null) { //FIX crash #69 on Crashlitics
            bluetoothLeScanner.startScan(null, settings, scanCallback);
        }
    }

    /**
     * Stops Bluetooth Low Energy scanning on Android devices with API level >= 21.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP | Build.VERSION_CODES.M)
    private void stopScanning() {
        if (bluetoothLeScanner != null) {
            try { //sometimes throw npe from os.Parcel.eadException() - only try catch is a solution
                bluetoothLeScanner.stopScan(scanCallback);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Used by both {@link BlueScannerTask#legacyScanCallback} and {@link BlueScannerTask#scanCallback}
     * to process scanned devices. Discovered device processing runs on a worker thread.
     *
     * @param device           Android object associated with detected Bluetooth device
     * @param rssi             Received Signal Strength Indication
     */
    private void onScan(BluetoothDevice device, int rssi) {
        long currentTimestamp = System.currentTimeMillis();
        if (device != null) {
            //if (device.getAddress().contains("02:89")) {
                String address = device.getAddress();
                final BlueDevice blueDevice = new BlueDevice();
                blueDevice.setAddress(device.getAddress());
                blueDevice.setName(device.getName());
                blueDevice.setDiscoveredTimestamp(currentTimestamp);
                blueDevice.setDistance(rssi);
                blueDevice.setBluetoothDevice(device);
                if (!blueScanner.discoveredDevices.containsKey(address)) {
                    blueScanner.onDiscovery(address, blueDevice, device);
                } else {
                    blueScanner.onUpdate(address, blueDevice, device);
                }
           // }
        }
    }

    private void waitError() {
        try {
            Thread.sleep(blueScanner.getSettings().getWaitPeriodAfterErrorMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitActively() {
        try {
            Thread.sleep(blueScanner.getSettings().getScanPeriodMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitPassively() {
        try {
            Thread.sleep(blueScanner.getSettings().getWaitPeriodMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to initialize internal Bluetooth scanner and scan callback.
     */
    private void initialize() {
        if (bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // API changed in 5.0
                initScanner();
            } else {
                initLegacyScanner();
            }
        } else {
           // L.e(BlueScanner.class, "Bluetooth is not enabled");
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                blueScanner.setSettings(BlueScannerSettings.getDefault());
            } else {
                blueScanner.setSettings(BlueScannerSettings.getDefaultLegacy());
            }

            if (bluetoothAdapter.isEnabled()) {
                if (!initialized) {
                    initialize();
                }

                if (!isLegacy) {
                    stopScanning();
                } else {
                    stopLegacyScanning();
                }

                blueScanner.monitorDiscoveredDevices();
                waitPassively();
                synchronized (this) {
                    if (isRunning) {
                        if(bluetoothAdapter.isEnabled()) {
                            if (!isLegacy) {
                                // isRunning flag could change while waiting
                                startScanning();
                            } else {
                                startLegacyScanning();
                            }
                        }
                    }
                }

                waitActively();
            } else {
                waitError();
            }
        }
    }

    public synchronized void stop() {
        isRunning = false;
        if (bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopScanning();
            } else {
                stopLegacyScanning();
            }
        }
    }

    public Set<BluetoothDevice> getBondedDevicesSet(){
        if(bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.getBondedDevices();
        }else{
            return Collections.EMPTY_SET;
        }
    }

    public boolean isBluetoothLowEnergyEnabled(){
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        }else{
            return false;
        }
    }

}