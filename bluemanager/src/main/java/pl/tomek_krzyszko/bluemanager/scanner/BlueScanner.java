package pl.tomek_krzyszko.bluemanager.scanner;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.inject.Inject;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.device.BlueDeviceConnectionListener;
import pl.tomek_krzyszko.bluemanager.device.BlueDeviceController;
import timber.log.Timber;

/**
 * Class responsible for scanning for nearby devices.
 * Found devices are saved to internal collection.
 * If the device is not scanned for certain amount of time it is removed from the collection.
 * {@link BlueScanner} runs scanning and parsing tasks on its own threads.
 */
public class BlueScanner extends Service {

    @Inject
    BlueConfig blueConfig;

    /**
     * Task run on a worker thread. The task consists of cyclic bluetooth scanning.
     */
    @Inject BlueScannerTask blueScannerTask;

    @Inject BlueScannerSettings settings;

    public static final String ACTION_TERMA = "action_terma";

    public static final String EXTRA_TYPE = "extra_type";

    public static final int EXTRA_TYPE_DISCOVERED = 0;
    public static final int EXTRA_TYPE_UPDATED = 1;
    public static final int EXTRA_TYPE_LOST = 2;
    public static final int EXTRA_TYPE_ERROR = 3;

    public static final String EXTRA_ERROR_CODE = "extra_error_code";

    public static final String EXTRA_VALUE = "extra_value";


    @Override
    public void onCreate() {
        injectDependencies();
        super.onCreate();
    }

    private void injectDependencies() {
        BlueManager.getInstance()
                    .getComponent()
                    .module(new ScannerModule(this))
                    .inject(this);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stop();
        disconnectAll();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        stop();
        disconnectAll();
        super.onDestroy();
    }

    public void setSettings(BlueScannerSettings settings) {
        this.settings = settings;
    }

    public BlueScannerSettings getSettings() {
        return settings;
    }

    /**
     * Used to provide {@link BlueScanner} reference to objects that are bound to this service.
     */
    private IBinder binder = new LocalBinder();

    private Set<BlueDeviceScanListener> blueDeviceScanListeners = new HashSet<>();

    /**
     * Adds {@link BlueDeviceScanListener} to the scanner.
     *
     * @param blueDeviceScanListener listener to add
     * @throws IllegalArgumentException when provided listener is null
     */
    public void addBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if (blueDeviceScanListener != null) {
            blueDeviceScanListeners.add(blueDeviceScanListener);
        } else {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    /**
     * Removes {@link BlueDeviceScanListener} from the scanner.
     *
     * @param blueDeviceScanListener listener to remove
     * @return {@code true} if object was removed from the scanner, {@code false} if the listener was not registered in the scanner
     * @throws IllegalArgumentException when provided listener is null
     */
    public boolean removeBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if (blueDeviceScanListener != null) {
            return blueDeviceScanListeners.remove(blueDeviceScanListener);
        } else {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    /**
     * Removes all {@link BlueDeviceScanListener}s registered in the scanner.
     */
    public void removeAllBlueDeviceScanListener() {
        blueDeviceScanListeners.clear();
    }

    /**
     * Holds information about iBeacon devices currently connected to.
     * Connected devices do not broadcast Advertisement Data.
     * They can be used to perform actions.
     * Mapping between hardware address and {@link BlueDevice} object.
     */
    private Map<String, BlueDeviceController> connectedDevices = new HashMap<>();

    /**
     * Mapping between hardware address and {@link BluetoothDevice}.
     * {@link BluetoothDevice} is an Android entity used to connect to the device.
     */
    private Map<String, BluetoothDevice> bluetoothDevices = new HashMap<>();

    /**
     * Mapping between hardware address and {@link BlueDevice}.
     * {@link BlueDevice} is a model class representing devices discovered in a nearby location.
     */
    Map<String, BlueDevice> discoveredDevices = new HashMap<>();

    /**
     * @return {@link List} containing all {@link BlueDevice}s
     * currently considered to be nearby Android device.
     */
    public synchronized List<BlueDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }

    /**
     * @param address hardware address of the device
     * @return {@link BlueDevice} object if it is currently considered to be nearby Android device
     * - it is present in {@link BlueScanner#discoveredDevices} collection.
     * {@code null} if the device is not considered to be discovered.
     */
    public synchronized BlueDevice getDiscoveredDevice(String address) {
        return discoveredDevices.get(address);
    }

    private Handler handler = new Handler();

    /**
     * {@link Binder} class associated with {@link BlueScanner} instance.
     */
    public class LocalBinder extends Binder {

        /**
         * @return {@link BlueScanner} instance that can be used to interact with devices after successful binding.
         */
        public BlueScanner getService() {
            return BlueScanner.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Code to run when a new device is discovered that is not present in {@link BlueScanner#discoveredDevices} collection
     *
     * @param address          hardware address of the bluetooth device
     * @param blueDevice {@link BlueDevice} object
     * @param device           Android object associated with detected Bluetooth device
     */
    void onDiscovery(String address, final BlueDevice blueDevice, BluetoothDevice device) {
        synchronized (this) {
            discoveredDevices.put(address, blueDevice);
            bluetoothDevices.put(address, device);
        }
        Set<BlueDeviceScanListener> blueDeviceScanListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException #99

        for (final BlueDeviceScanListener blueDeviceScanListener : blueDeviceScanListenersCopy) {
            // run callback on main thread to easily update UI
            handler.post(new Runnable() {
                @Override
                public void run() {
                    blueDeviceScanListener.onDeviceDiscovered(blueDevice);
                }
            });
        }

        if (settings.isSendBroadcasts()) {
            Intent intent = new Intent(ACTION_TERMA);
            intent.putExtra(EXTRA_TYPE, EXTRA_TYPE_DISCOVERED);
            intent.putExtra(EXTRA_VALUE, blueDevice);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * Code to run when a device present in {@link BlueScanner#discoveredDevices} collection has been updated
     *
     * @param address          hardware address of the bluetooth device
     * @param blueDevice {@link BlueDevice} object
     * @param device           Android object associated with detected Bluetooth device
     */
    void onUpdate(String address, final BlueDevice blueDevice, BluetoothDevice device) {
        synchronized (this) {
            discoveredDevices.remove(address);
            bluetoothDevices.remove(address);
            discoveredDevices.put(address, blueDevice);
            bluetoothDevices.put(address, device);

        }
        // L.d(BScanner.this, String.format("Updated %s -> %s -> ", discoveredDevice.getAddress(), discoveredDevice.getName()));

        Set<BlueDeviceScanListener> blueDeviceScanListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException

        for (final BlueDeviceScanListener deviceListener : blueDeviceScanListenersCopy) {
            // run callback on main thread to easily update UI
            handler.post(new Runnable() {
                @Override
                public void run() {
                    deviceListener.onDeviceUpdate(blueDevice);
                }
            });
        }

        if (settings.isSendBroadcasts()) {
            Intent intent = new Intent(ACTION_TERMA);
            intent.putExtra(EXTRA_TYPE, EXTRA_TYPE_UPDATED);
            intent.putExtra(EXTRA_VALUE, blueDevice);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    void onFailure(int errorCode) {

        Set<BlueDeviceScanListener> blueDeviceListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException

        for (final BlueDeviceScanListener blueDeviceScanListener : blueDeviceListenersCopy) {
            // ScanCallback's methods are called on main thread already
            blueDeviceScanListener.onDeviceScanError(errorCode);
        }

        if (settings.isSendBroadcasts()) {
            Intent intent = new Intent(ACTION_TERMA);
            intent.putExtra(EXTRA_TYPE, EXTRA_TYPE_ERROR);
            intent.putExtra(EXTRA_ERROR_CODE, errorCode);
            LocalBroadcastManager.getInstance(BlueScanner.this).sendBroadcast(intent);
        }
    }

    /**
     * Used to remove all {@link BlueDevice}s in {@link BlueScanner#discoveredDevices}
     * that have not been detected fore some time and are considered lost.
     * Information about lost devices is passed to {@link BlueScanner#blueDeviceScanListeners}.
     */
    synchronized void monitorDiscoveredDevices() {

        Iterator<Map.Entry<String, BlueDevice>> iterator = discoveredDevices.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, BlueDevice> entry = iterator.next();
            long currentTimestamp = System.currentTimeMillis();
            long discoveredTimestamp = entry.getValue().getDiscoveredTimestamp();


            if (currentTimestamp - discoveredTimestamp > settings.getDiscoveryTimeoutMillis() && !connectedDevices.containsKey(entry.getValue().getAddress())) {
                // if device was not detected for some time and it is not connected right now, remove device from discovered devices
                // when the device is connected it does not send advertising data
                iterator.remove();
                bluetoothDevices.remove(entry.getKey());
                final BlueDevice blueDevice = entry.getValue();
                Set<BlueDeviceScanListener> wooletListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException

                for (final BlueDeviceScanListener wooletListener : wooletListenersCopy) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            wooletListener.onDeviceLost(blueDevice);
                        }
                    });
                }

                if (settings.isSendBroadcasts()) {
                    Intent intent = new Intent(ACTION_TERMA);
                    intent.putExtra(EXTRA_TYPE, EXTRA_TYPE_LOST);
                    intent.putExtra(EXTRA_VALUE, blueDevice);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
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
    public synchronized boolean connectToDevice(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener) {
        if (blueDevice != null) {
            String address = blueDevice.getAddress();
            if (address != null && !connectedDevices.containsKey(address) && bluetoothDevices.containsKey(address)) {
                stop();
                final BlueDeviceController blueDeviceController = new BlueDeviceController(BlueScanner.this, blueDevice);
                connectedDevices.put(address, blueDeviceController);
                blueDeviceController.open(new BlueDeviceConnectionListener() {
                    @Override
                    public void onDeviceReady(final BlueDeviceController blueDeviceController) {
                        if (blueDeviceConnectionListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    blueDeviceConnectionListener.onDeviceReady(blueDeviceController);
                                }
                            });
                        }
                    }

                    @Override
                    public void onDeviceClosed(final BlueDeviceController blueDeviceController) {
                        if (blueDeviceConnectionListener != null) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    blueDeviceConnectionListener.onDeviceClosed(blueDeviceController);
                                }
                            });
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Disconnects from given discovered device.
     *
     * @param blueDevice {@link BlueDevice} to disconnect from
     * @return true if disconnected successfully, false if discovered device does not exist or was not connected
     */
    public synchronized boolean disconnectFromDevice(BlueDevice blueDevice) {
        if (blueDevice != null) {
            String address = blueDevice.getAddress();
            if (address != null && connectedDevices.containsKey(address)) {
                synchronized (this) {
                    if (discoveredDevices.containsKey(address)) {
                        // to prevent deleting device from discovered devices right after disconnecting from it
                        discoveredDevices.get(address).setDiscoveredTimestamp(System.currentTimeMillis());
                    }
                }
                BlueDeviceController blueDeviceController = connectedDevices.get(address);
                connectedDevices.remove(address);
                blueDeviceController.close();

                if (connectedDevices.size() == 0) {
                    start();
                }

                return true;
            }
        }
        return false;
    }

    /**
     * Disconnects all active iBeacon connections.
     */
    private void disconnectAll() {
        for (BlueDeviceController blueDeviceController : connectedDevices.values()) {
            blueDeviceController.close();
        }
        connectedDevices.clear();
    }

    public synchronized void start() {
        new Thread(blueScannerTask).start();
    }

    public synchronized void stop() {
        if (blueScannerTask != null) {
            blueScannerTask.stop();
        } else {
            Timber.i("BlueScanner not started yet.");
        }
    }



    public Set<BluetoothDevice> getBondedDevicesSet(){
        if(blueScannerTask!=null){
            return blueScannerTask.getBondedDevicesSet();
        }else{
            return Collections.EMPTY_SET;
        }
    }

    public void pairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
           // L.e(BlueScanner.this, e.getMessage());
        }
    }
}
