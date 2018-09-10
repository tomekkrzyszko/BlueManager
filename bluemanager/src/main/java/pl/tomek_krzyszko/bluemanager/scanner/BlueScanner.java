package pl.tomek_krzyszko.bluemanager.scanner;


import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
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
import pl.tomek_krzyszko.bluemanager.action.BlueAction;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceActionListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceConnectionListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.dagger.modules.ScannerModule;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.device.BlueDeviceController;
import timber.log.Timber;

/**
 * Class responsible for scanning for nearby devices.
 * Found devices are saved to internal collection.
 * If the device is not scanned for certain amount of time it is removed from the collection.
 * {@link BlueScanner} runs scanning and parsing tasks on its own threads.
 */
public class BlueScanner extends Service {

    @Inject BlueConfig blueConfig;
    @Inject BlueScannerTask blueScannerTask;
    @Inject Set<BlueDeviceScanListener> blueDeviceScanListeners;
    @Inject Map<String, BlueDevice> discoveredDevices;
    @Inject Handler handler;
    @Inject BlueDeviceController blueDeviceController;

    /**
     * Holds information about devices currently connected to.
     * Connected devices do not broadcast Advertisement Data.
     * They can be used to perform actions.
     * Mapping between hardware address and {@link BlueDevice} object.
     */
    Map<String, BlueDevice> connectedDevices = new HashMap<>();

    /**
     * Whether or not the scanner is running for bluetooth low energy devices or not
     */
    private boolean isLowEnergy;

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
        stopScan(isLowEnergy);
        disconnectAll();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        stopScan(isLowEnergy);
        disconnectAll();
        super.onDestroy();
    }

    /**
     * Used to provide {@link BlueScanner} reference to objects that are bound to this service.
     */
    private IBinder binder = new LocalBinder();

    public void addBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if (blueDeviceScanListener != null) {
            blueDeviceScanListeners.add(blueDeviceScanListener);
        } else {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    public boolean removeBlueDeviceScanListener(BlueDeviceScanListener blueDeviceScanListener) {
        if (blueDeviceScanListener != null) {
            return blueDeviceScanListeners.remove(blueDeviceScanListener);
        } else {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    public void removeAllBlueDeviceScanListener() {
        blueDeviceScanListeners.clear();
    }

    public synchronized List<BlueDevice> getDiscoveredDevices() {
        return new ArrayList<>(discoveredDevices.values());
    }

    public synchronized BlueDevice getDiscoveredDevice(String address) {
        return discoveredDevices.get(address);
    }

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
     */
    void onDiscovery(String address, final BlueDevice blueDevice) {
        synchronized (this) {
            discoveredDevices.put(address, blueDevice);
        }
        Set<BlueDeviceScanListener> blueDeviceScanListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException #99
        for (final BlueDeviceScanListener blueDeviceScanListener : blueDeviceScanListenersCopy) {
            // run callback on main thread to easily update UI
            handler.post(() -> blueDeviceScanListener.onDeviceFound(blueDevice));
        }
        if (blueConfig.getShouldSendBroadcast()) {
            Intent intent = new Intent(BlueConfig.BLUE_BROADCAST_ACTION);
            intent.putExtra(BlueConfig.BLUE_SCAN_TYPE, BlueConfig.BLUE_SCAN_DISCOVERED);
            intent.putExtra(BlueConfig.BLUE_DEVICE_VALUE, blueDevice);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * Code to run when a device present in {@link BlueScanner#discoveredDevices} collection has been updated
     *
     * @param address          hardware address of the bluetooth device
     * @param blueDevice {@link BlueDevice} object
     */
    void onUpdate(String address, final BlueDevice blueDevice) {
        synchronized (this) {
            discoveredDevices.remove(address);
            discoveredDevices.put(address, blueDevice);
        }
        Set<BlueDeviceScanListener> blueDeviceScanListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException
        for (final BlueDeviceScanListener deviceListener : blueDeviceScanListenersCopy) {
            // run callback on main thread to easily update UI
            handler.post(() -> deviceListener.onDeviceUpdate(blueDevice));
        }
        if (blueConfig.getShouldSendBroadcast()) {
            Intent intent = new Intent(BlueConfig.BLUE_BROADCAST_ACTION);
            intent.putExtra(BlueConfig.BLUE_SCAN_TYPE, BlueConfig.BLUE_SCAN_UPDATED);
            intent.putExtra(BlueConfig.BLUE_DEVICE_VALUE, blueDevice);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    void onFailure(int errorCode) {
        Set<BlueDeviceScanListener> blueDeviceListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException
        for (final BlueDeviceScanListener blueDeviceScanListener : blueDeviceListenersCopy) {
            // ScanCallback's methods are called on main thread already
            blueDeviceScanListener.onDeviceScanError(errorCode);
        }
        if (blueConfig.getShouldSendBroadcast()) {
            Intent intent = new Intent(BlueConfig.BLUE_BROADCAST_ACTION);
            intent.putExtra(BlueConfig.BLUE_SCAN_TYPE, BlueConfig.BLUE_SCAN_ERROR);
            intent.putExtra(BlueConfig.BLUE_SCAN_ERROR_CODE, errorCode);
            LocalBroadcastManager.getInstance(BlueScanner.this).sendBroadcast(intent);
        }
    }

    public synchronized void checkBlueDevices() {
        Iterator<Map.Entry<String, BlueDevice>> iterator = discoveredDevices.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, BlueDevice> entry = iterator.next();
            long currentTimestamp = System.currentTimeMillis();
            long discoveredTimestamp = entry.getValue().getDiscoveredTimestamp();
            if (currentTimestamp - discoveredTimestamp > blueConfig.getDiscoveryTimeoutMillis() && !connectedDevices.containsKey(entry.getValue().getAddress())) {
                // if device was not detected for some time and it is not connected right now, remove device from discovered devices
                // when the device is connected it does not send advertising data
                iterator.remove();
                final BlueDevice blueDevice = entry.getValue();
                Set<BlueDeviceScanListener> wooletListenersCopy = new CopyOnWriteArraySet<>(blueDeviceScanListeners); //Prevent set from ConcurrentModificationException
                for (final BlueDeviceScanListener wooletListener : wooletListenersCopy) {
                    handler.post(() -> wooletListener.onDeviceLost(blueDevice));
                }
                if (blueConfig.getShouldSendBroadcast()) {
                    Intent intent = new Intent(BlueConfig.BLUE_BROADCAST_ACTION);
                    intent.putExtra(BlueConfig.BLUE_SCAN_TYPE, BlueConfig.BLUE_SCAN_LOST);
                    intent.putExtra(BlueConfig.BLUE_DEVICE_VALUE, blueDevice);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                }
            }
        }
    }

    public synchronized boolean connectToDevice(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener) {
        if (blueDevice != null) {
            String address = blueDevice.getAddress();
            if (address != null && !connectedDevices.containsKey(address)) {
                connectedDevices.put(address, blueDevice);
                blueDeviceController.open(blueDevice, new BlueDeviceConnectionListener() {
                    @Override
                    public void onDeviceReady(final BlueDevice blueDevice) {
                        if (blueDeviceConnectionListener != null) {
                            handler.post(() -> blueDeviceConnectionListener.onDeviceReady(blueDevice));
                        }
                    }

                    @Override
                    public void onDeviceClosed(final BlueDevice blueDevice) {
                        if (blueDeviceConnectionListener != null) {
                            handler.post(() -> blueDeviceConnectionListener.onDeviceClosed(blueDevice));
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

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
                connectedDevices.remove(address);
                blueDeviceController.close(blueDevice);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean disconnectAll() {
        for (BlueDevice blueDevice : connectedDevices.values()) {
            blueDeviceController.close(blueDevice);
        }
        connectedDevices.clear();
        return true;
    }

    public synchronized void startScan(String address,boolean lowEnergy) {
        if(address!=null && !address.isEmpty()){
            blueScannerTask.setAddress(address);
        }
        isLowEnergy = lowEnergy;
        blueScannerTask.setLowEnergy(lowEnergy);
        new Thread(blueScannerTask).start();
    }

    public synchronized void startScan(long time,boolean lowEnergy) {
        blueScannerTask.setScanningTime(time);
        blueScannerTask.setLowEnergy(lowEnergy);
        isLowEnergy = lowEnergy;
        new Thread(blueScannerTask).start();
    }

    public synchronized void stopScan(boolean lowEnergy) {
        if (blueScannerTask != null) {
            blueScannerTask.setLowEnergy(lowEnergy);
            isLowEnergy = lowEnergy;
            blueScannerTask.stop();
        } else {
            Timber.i("BlueScanner not started yet.");
        }
    }

    public boolean refreshDeviceCache(BlueDevice blueDevice) {
      return blueDeviceController.refreshDeviceCache(blueDevice.getBluetoothGatt());
    }

    public Set<BluetoothDevice> getBondedDevicesSet(){
        if(blueScannerTask!=null){
            return blueScannerTask.getBondedDevicesSet();
        }else{
            return new HashSet<>();
        }
    }

    public boolean performAction(BlueDevice blueDevice, BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener){
        return blueDeviceController.performAction(blueDevice,blueAction,blueDeviceActionListener);
    }
}
