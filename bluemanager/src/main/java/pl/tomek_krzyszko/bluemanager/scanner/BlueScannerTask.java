package pl.tomek_krzyszko.bluemanager.scanner;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.inject.Inject;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import timber.log.Timber;

public class BlueScannerTask implements Runnable {

    @Inject BlueConfig blueConfig;
    @Inject Context context;
    @Inject BluetoothAdapter bluetoothAdapter;
    @Inject BluetoothLeScanner bluetoothLeScanner;
    @Inject Timer timer;
    private BlueScanner blueScanner;

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
     * Scan callback used in API level < 21.
     */
    private BluetoothAdapter.LeScanCallback legacyScanCallback;

    /**
     * Scan callback used in API level >= 21.
     */
    private ScanCallback scanCallback;

    /**
     * Scanning time passed from {@link BlueScanner} for scanner
     */
    private Long scanningTime = null;

    /**
     * Bluetooth device MAC address passed from {@link BlueScanner} for scanner
     */
    private String address = null;

    /**
     * Whether or not the scanner is running for bluetooth low energy devices or not
     */
    private boolean isLowEnergy;

    /**
     * Array of service UUID which scanner should looking for in devices
     * Scan method will return only this {@link BluetoothDevice}s which will have service {@link UUID} from that array
     */
    private UUID[] uuids;

    /**
     * Settings for {@link BluetoothLeScanner} scanner
     */
    private ScanSettings scanSettings;

    /**
     * List of filtering conditions for {@link BluetoothLeScanner} scanner
     */
    private List<ScanFilter> scanFilters;


    public BlueScannerTask(BlueScanner blueScanner) {
        BlueManager.getInstance()
                    .getComponent()
                    .module(new TaskModule(blueScanner))
                    .inject(this);
        this.blueScanner = blueScanner;
    }

    public void setScanningTime(Long scanningTime) {
        this.scanningTime = scanningTime;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLowEnergy(boolean lowEnergy) {
        isLowEnergy = lowEnergy;
    }

    public void setUuids(UUID[] uuids) {
        this.uuids = uuids;
    }

    public void setScanSettings(ScanSettings scanSettings) {
        this.scanSettings = scanSettings;
    }

    public void setScanFilters(List<ScanFilter> scanFilters) {
        this.scanFilters = scanFilters;
    }

    /**
     * Initializes scan callback used in Android devices with API level < 18.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initClassicScanner() {
        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        initialized = true;
    }

    /**
     * Starts Bluetooth Classic scanning on Android devices with API level < 18.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startClassicScanning() {
        bluetoothAdapter.startDiscovery();
    }


    /**
     * Stops Bluetooth Classic scanning on Android devices with API level < 18.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void stopClassicScanning() {
        bluetoothAdapter.cancelDiscovery();
        context.unregisterReceiver(mReceiver);
    }

    /**
     * Initializes scan callback used in Android devices with API level < 21.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initLegacyScanner() {
        isLegacy = true;

        legacyScanCallback = (device, rssi, rawScanRecord) -> new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                onScan(device, rssi);
                return null;
            }

        }.execute();

        initialized = true;
    }

    /**
     * Starts Bluetooth Low Energy scanning on Android devices with API level < 21.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void startLegacyScanning() {
        bluetoothAdapter.startLeScan(uuids,legacyScanCallback);
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
        if (bluetoothLeScanner != null) { //FIX crash #69 on Crashlitics
            if(scanSettings==null){
                scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build();
            }
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
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

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onScan(device,0);
            }
        }
    };


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
                String macAddress = device.getAddress();
                final BlueDevice blueDevice = new BlueDevice();
                blueDevice.setAddress(device.getAddress());
                blueDevice.setName(device.getName());
                blueDevice.setDiscoveredTimestamp(currentTimestamp);
                blueDevice.setBluetoothDevice(device);
                if (!blueScanner.discoveredDevices.containsKey(macAddress)) {
                    blueScanner.onDiscovery(macAddress, blueDevice);
                } else {
                    blueScanner.onUpdate(macAddress, blueDevice);
                }
                if(address!=null && !address.isEmpty() && macAddress.equals(address)){
                    stop();
                }
        }
    }

    private void waitError() {
        try {
            Thread.sleep(blueConfig.getWaitPeriodAfterErrorMillis());
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
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                initLegacyScanner();
            }else{
                initClassicScanner();
            }
        } else {
           Timber.e("Bluetooth is not enabled");
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (bluetoothAdapter.isEnabled()) {
                if (!initialized) {
                    initialize();
                }
                if(isLowEnergy) {
                    if (!isLegacy) {
                        stopScanning();
                    } else {
                        stopLegacyScanning();
                    }
                }else{
                    stopClassicScanning();
                }
                blueScanner.checkBlueDevices();
                synchronized (this) {
                    if (isRunning) {
                        if(bluetoothAdapter.isEnabled()) {
                            //Start scanning
                            if(scanningTime!=null && scanningTime >0){
                                startScaningTimer();
                            }
                            if(isLowEnergy) {
                                if (!isLegacy) {
                                    startScanning();
                                } else {
                                    startLegacyScanning();
                                }
                            }else{
                                startClassicScanning();
                            }
                        }
                    }
                }
            } else {
                waitError();
            }
        }
    }

    private void startScaningTimer(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stop();
            }
        },scanningTime);
    }

    public synchronized void stop() {
        isRunning = false;
        if (bluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopScanning();
            }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                stopLegacyScanning();
            }else{
                stopClassicScanning();
            }
        }
    }



    public Set<BluetoothDevice> getBondedDevicesSet(){
        if(bluetoothAdapter.isEnabled()) {
            return bluetoothAdapter.getBondedDevices();
        }else{
            return  new HashSet<>();
        }
    }

}