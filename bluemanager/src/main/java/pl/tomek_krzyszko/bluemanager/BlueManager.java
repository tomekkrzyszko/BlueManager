package pl.tomek_krzyszko.bluemanager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pl.tomek_krzyszko.bluemanager.action.BlueAction;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceActionListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceConnectionListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueScannerServiceConnection;
import pl.tomek_krzyszko.bluemanager.dagger.ApplicationScope;
import pl.tomek_krzyszko.bluemanager.dagger.components.BlueManagerComponent;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.exception.BlueManagerExceptions;
import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;
import timber.log.Timber;

/**
 * Main manager class used to control scanning and managing scan results.
 */
public class BlueManager{
    private static BlueManager instance = null;
    private BlueManagerComponent component;
    private BlueScanner blueScanner;
    private Context mContext;
    private boolean connected = false;
    private BlueScannerServiceConnection blueScannerServiceConnection;
    private Intent blueScannerIntent;

    /**
     * Method which returns {@link BlueManager} instance.
     */
    public static BlueManager getInstance() {
        if (instance == null) {
            instance = new BlueManager();
        }
        return instance;
    }

    /**
     * Public constructor of the class.
     */
    public BlueManager() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    /**
     * Method to receive component {@link BlueManagerComponent} object.
     * @return {@link BlueManagerComponent} used to inject all needed dependencies.
     */
    public BlueManagerComponent getComponent() {
        return component;
    }

    /**
     *
     * @param context {@link Context} used to initialize app context in library
     * @param blueConfig {@link BlueConfig} used to change default BlueManager configuration. If null, default config will be used.
     */
    public void initilize(Context context, @Nullable BlueConfig blueConfig){
        this.mContext = context;
        this.component = ApplicationScope.getComponent(mContext,blueConfig);
    }

    /**
     * @return {@link Intent} associated with {@link BlueScanner} service.
     */
    private Intent getServiceIntent() {
        if(blueScannerIntent == null){
            blueScannerIntent = new Intent(mContext, BlueScanner.class);
        }
        return blueScannerIntent;
    }

    /**
     *  Set {@link BlueScannerServiceConnection} instance.
     */
    private void setBlueScannerServiceConnection(BlueScannerServiceConnection blueScannerServiceConnection) {
        this.blueScannerServiceConnection = blueScannerServiceConnection;
    }

    /**
     * Starts {@link BlueScanner} service. If the service is started already, it does nothing.
     */
    public void startBlueScanner(BlueScannerServiceConnection blueScannerServiceConnection) throws BlueManagerExceptions {
        if(mContext!=null) {
            mContext.startService(getServiceIntent());
            connect(blueScannerServiceConnection);
        }else{
            throw new BlueManagerExceptions("First initilize BlueManager.");
        }
    }

    /**
     * Stops {@link BlueScanner} service.
     */
    public void stopBlueScanner() {
        disconnect();
        mContext.stopService(getServiceIntent());
    }

    /**
     * Connects {@link BlueManager} to {@link BlueScanner} service.
     * If the service hasn't been started before, it is started now.
     *
     * @param blueScannerServiceConnection {@link BlueScannerServiceConnection} used to notify about service connection status.
     * @return whether or not you have successfully bound the service
     */
    private boolean connect(BlueScannerServiceConnection blueScannerServiceConnection) {
        if (!connected) {
            setBlueScannerServiceConnection(blueScannerServiceConnection);
            connected = mContext.bindService(getServiceIntent(), serviceConnection, Context.BIND_ADJUST_WITH_ACTIVITY);
            return connected;
        }
        return true;
    }

    /**
     * Disconnects {@link BlueManager} from {@link BlueScanner} service.
     */
    private void disconnect() {
        if (connected) {
            connected = false;
            mContext.unbindService(serviceConnection);
        }
    }

    /**
     * Service connection object which returns information about {@link BlueScanner} service connection status.
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (blueScannerServiceConnection != null) {
                blueScanner = ((BlueScanner.LocalBinder) iBinder).getService();
                blueScannerServiceConnection.onConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            if (blueScannerServiceConnection != null) {
                blueScanner = null;
                blueScannerServiceConnection.onDisconnected();
            }
        }
    };

    /**
     * Check if location service is enabled. If not method will start proper intent.
     * @param context {@link Context} used to manage system services.
     * @return whether or not you have location service on.
     */
    public boolean checkLocationIsOn(Context context){
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled=false;
        try {
            if (lm != null) {
                gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            }else{
                return false;
            }
        } catch(Exception ex) {
            return false;
        }
        if(gpsEnabled){
            return true;
        }else{
            Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(myIntent);
            return false;
        }
    }

    /**
     * Check if bluetooth is enabled. If not method will start proper intent.
     * @param context {@link Context} used to manage system services.
     * @return whether or not you have bluetooth enabled.
     */
    public boolean checkBluetooth(Context context){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enable
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
                return false;
            }else{
                return true;
            }
        }
    }

    /**
     * Check if bluetooth low energy is enabled.
     * @return whether or not you have bluetooth low energy enabled on the device.
     */
    public boolean isBluetoothLowEnergyEnabled(){
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * Method used to force device pairing process.
     * @param device {@link BluetoothDevice} which you want to pair your phone.
     */
    public void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
             Timber.e(e.getMessage());
        }
    }

    /**
     * Start bluetooth scanning process with default parameters.
     * Remember to stop scanning when it will be not needed.
     */
    public void startScanning(boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.startScan(null,null,null,null,null,lowEnergy);
        }
    }

    /**
     * Start bluetooth scanning process.
     * @param address {@link String} in format XX:XX:XX:XX:XX:XX
     * If address is not null, scanning process will be working until given device will be found.
     * @param scanSettings settings with scanning type
     * @param scanFilters for scanner which set conditions for scanning
     * @param lowEnergy whether or not you will be using bluetooth low energy scanner
     */
    public void startScanning(String address, ScanSettings scanSettings, List<ScanFilter> scanFilters, boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.startScan(address,null,null,scanSettings,scanFilters,lowEnergy);
        }
    }

    /**
     * Start bluetooth scanning process.
     * @param address {@link String} in format XX:XX:XX:XX:XX:XX
     * If address is not null, scanning process will be working until given device will be found.
     * @param uuids array for scanner. Scan method will return only this {@link BlueDevice}s which will have service {@link UUID} from that array
     * @param lowEnergy whether or not you will be using bluetooth low energy scanner
     */
    public void startScanning(String address, UUID[] uuids, boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.startScan(address,null,uuids,null,null,lowEnergy);
        }
    }

    /**
     * Start bluetooth scanning process for specific amount of time
     * @param time in milliseconds
     * @param scanSettings settings with scanning type
     * @param scanFilters for scanner which set conditions for scanning
     * @param lowEnergy whether or not you will be using bluetooth low energy scanner
     */
    public void startScanning(long time, ScanSettings scanSettings, List<ScanFilter> scanFilters, boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.startScan(null,time,null,scanSettings,scanFilters,lowEnergy);
        }
    }

    /**
     * Start bluetooth scanning process for specific amount of time
     * @param time in milliseconds
     * @param uuids array for scanner. Scan method will return only this {@link BlueDevice}s which will have service {@link UUID} from that array
     * @param lowEnergy whether or not you will be using bluetooth low energy scanner
     */
    public void startScanning(long time, UUID[] uuids, boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.startScan(null,time,uuids,null,null,lowEnergy);
        }
    }

    /**
     * Stop bluetooth scanning process.
     */
    public void stopScanning(boolean lowEnergy){
        if (blueScanner != null) {
            blueScanner.stopScan(lowEnergy);
        }
    }

    /**
     * Used to force remove all {@link BlueDevice}s in {@link BlueScanner#discoveredDevices}
     * that have not been detected fore some time and are considered lost.
     * Information about lost devices is passed to {@link BlueScanner#blueDeviceScanListeners}.
     */
    public void forceCheckNearbyBlueDevices(){
        if(blueScanner!=null){
            blueScanner.checkBlueDevices();
        }
    }

    /**
     * Connects to the given discovered device.
     * There is a limit for simultaneously connected Bluetooth devices which seems to vary on different devices.
     * It was 4 in the beginning, in 2013 it was increased to 7.
     * <i>https://code.google.com/p/android/issues/detail?id=68538</i>
     *
     * @param blueDevice {@link BlueDevice} to connect to
     * @param blueDeviceConnectionListener   {@link BlueDeviceConnectionListener} that receives various callbacks
     * @return true if connection was initiated successfully, false if discovered device does not exist or is already connected
     */
    public boolean connectDevice(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener){
        if (blueScanner != null) {
           return blueScanner.connectToDevice(blueDevice,blueDeviceConnectionListener);
        }else{
            return false;
        }
    }

    /**
     * Disconnects from given discovered device.
     * @param blueDevice {@link BlueDevice} to disconnect from. If null it disconnects all active {@link BlueDevice} connections.
     * @return true if disconnected successfully, false if discovered device does not exist or was not connected
     */
    public synchronized boolean disconnectFromDevice(BlueDevice blueDevice) {
        if(blueScanner!=null){
            if(blueDevice!=null){
                return  blueScanner.disconnectFromDevice(blueDevice);
            }else{
                return blueScanner.disconnectAll();
            }
        }else{
            return false;
        }
    }

    /**
     * Class responsible for performing {@link BlueAction} on given {@link BlueDevice}
     * @param blueDevice on which action will be performed
     * @param blueAction represents {@link BlueAction} which will be performed
     * @param blueDeviceActionListener represents {@link BlueDeviceActionListener} class which will be returning callbacks
     * @return true if method successfully starts proper bluetooth process, false if device or action are wrong
     */
    public boolean performActionOnDevice(BlueDevice blueDevice, BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener){
        if(blueScanner!=null){
            return blueScanner.performAction(blueDevice,blueAction,blueDeviceActionListener);
        }else{
            return false;
        }
    }

    /**
     * Adds {@link BlueDeviceScanListener} to the scanner.
     * @param blueDeviceScanListener listener to add
     * @throws IllegalArgumentException when provided listener is null
     */
    public void addBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if (blueScanner != null) {
            blueScanner.addBlueDeviceScanListener(blueDeviceScanListener);
        }
    }

    /**
     * Removes {@link BlueDeviceScanListener} from the scanner.
     *
     * @param blueDeviceScanListener listener to remove. If null it removes all {@link BlueDeviceScanListener}s registered in the scanner.
     * @return {@code true} if object was removed from the scanner, {@code false} if the listener was not registered in the scanner
     */
    public boolean removeBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if(blueScanner!=null) {
            if (blueDeviceScanListener != null) {
                return blueScanner.removeBlueDeviceScanListener(blueDeviceScanListener);
            } else {
                blueScanner.removeAllBlueDeviceScanListener();
                return true;
            }
        }else{
            return false;
        }
    }

    /**
     * @param address hardware address of the device. If null it returns all nearby devices.
     * @return {@link List} containing all {@link BlueDevice}s currently considered to be nearby Android device.
     * - it is present in {@link BlueScanner#discoveredDevices} collection.
     * Return empty list if the devices are not considered to be discovered.
     */
    public synchronized List<BlueDevice> getDiscoveredDevices(String address) {
        if(blueScanner!=null){
            if(address!=null && !address.isEmpty()){
                LinkedList<BlueDevice> oneElementList = new LinkedList<>();
                oneElementList.add(blueScanner.getDiscoveredDevice(address));
                return oneElementList;
            }else{
                return blueScanner.getDiscoveredDevices();
            }
        }else{
            return new LinkedList<>();
        }
    }

    /**
     * Method helps clearing device cache memory.
     * http://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android
     */
    public void refreshDeviceCache(BlueDevice blueDevice){
        if(blueScanner!=null){
            blueScanner.refreshDeviceCache(blueDevice);
        }
    }

    /**
     * Return paired {@link BluetoothDevice} set.
     */
    public Set<BluetoothDevice> getPairedDeivces(){
        if(blueScanner!=null){
            return blueScanner.getBondedDevicesSet();
        }else{
            return new HashSet<>();
        }
    }

    /**
     *  Make local device discoverable to other devices for given time
     *  @param seconds
     */
    public void setDeviceDiscoverable(int seconds){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, seconds);
        mContext.startActivity(discoverableIntent);
    }
}
