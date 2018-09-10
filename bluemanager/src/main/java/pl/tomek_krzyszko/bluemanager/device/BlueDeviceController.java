package pl.tomek_krzyszko.bluemanager.device;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.inject.Inject;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.action.BlueAction;
import pl.tomek_krzyszko.bluemanager.action.NotifyAction;
import pl.tomek_krzyszko.bluemanager.action.ReadAction;
import pl.tomek_krzyszko.bluemanager.action.WriteAction;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceActionListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceConnectionListener;
import pl.tomek_krzyszko.bluemanager.dagger.modules.DeviceModule;
import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BlueDeviceController {

    @Inject BlueConfig blueConfig;
    @Inject Timer timer;

    private Context context;
    private BlueDeviceActionListener blueDeviceActionListener;

    public BlueDeviceController(Context context) {
        BlueManager.getInstance()
                .getComponent()
                .module(new DeviceModule())
                .inject(this);
        this.context = context;
    }

    public boolean performAction(BlueDevice blueDevice, BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && blueDevice.isBLEDevice()){
           return performActionOnBluetoothLowEnergyDevice(blueDevice,blueAction,blueDeviceActionListener);
        }else{
           return performActionOnBluetoothDevice(blueDevice,blueAction,blueDeviceActionListener);
        }
    }

    public boolean performActionOnBluetoothLowEnergyDevice(BlueDevice blueDevice, BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener){
        BluetoothGatt bluetoothGatt = blueDevice.getBluetoothGatt();
        if (bluetoothGatt != null && blueDevice.getCurrentAction() == null) {
            for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {
                if (bluetoothGattService.getUuid().equals(blueAction.getService())) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(blueAction.getCharacteristic());
                    if (bluetoothGattCharacteristic != null) {
                        boolean isSuccessful = false;
                        this.blueDeviceActionListener = blueDeviceActionListener;
                        if (blueAction instanceof WriteAction) {
                            bluetoothGattCharacteristic.setValue(((WriteAction) blueAction).getValue());
                            isSuccessful = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                            blueDevice.setCurrentAction(blueAction);
                        } else if (blueAction instanceof ReadAction) {
                            blueDevice.setCurrentAction(blueAction);
                            isSuccessful = bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                        }else if( blueAction instanceof NotifyAction){
                            for (BluetoothGattDescriptor descriptor : bluetoothGattCharacteristic.getDescriptors()) {
                                UUID uuid = blueConfig.getNotificationsEnablingDescriptorUUID();
                                if (descriptor.getUuid().equals(uuid)) {
                                    blueDevice.setCurrentAction(blueAction);
                                    bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    isSuccessful = bluetoothGatt.writeDescriptor(descriptor);
                                }
                            }
                        }
                        if (!isSuccessful) {
                            blueDevice.setCurrentAction(blueAction);
                            this.blueDeviceActionListener = null;
                        }
                        return isSuccessful;
                    }
                }
            }
        }
        return false;
    }

    public boolean performActionOnBluetoothDevice(BlueDevice blueDevice, BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener){
        if(blueDevice!=null && blueDevice.getBluetoothDevice()!=null && blueDevice.getBluetoothSocket()!=null && blueAction!=null){
            if (blueAction instanceof WriteAction) {
                blueDevice.setCurrentAction(blueAction);
                try {
                    blueDevice.getOutputStream().write(((WriteAction) blueAction).getValue());
                    // Share the sent message with the UI activity.
                    blueDeviceActionListener.onActionSuccess(blueDevice,blueAction,((WriteAction) blueAction).getValue());
                } catch (IOException e) {
                    Timber.e("Error occurred when sending data");
                }
                return true;
            }else if( blueAction instanceof NotifyAction){
                blueDevice.setCurrentAction(blueAction);
                new Thread(() -> {
                    byte[] mmBuffer = new byte[blueConfig.getBufferSize()];
                    int numBytes; // bytes returned from read()
                    // Keep listening to the InputStream until an exception occurs.
                    while (true) {
                        try {
                            // Read from the InputStream.
                            numBytes = blueDevice.getInputStream().read(mmBuffer);
                            blueDeviceActionListener.onActionSuccess(blueDevice,blueAction,mmBuffer);
                        } catch (IOException e) {
                            blueDeviceActionListener.onActionFailure(blueDevice,blueAction);
                            Timber.e("Input stream was disconnected");
                            break;
                        }
                    }

                }).start();
                return true;
            }else{
                blueDeviceActionListener.onActionFailure(blueDevice,blueAction);
                return false;
            }
        }else{
            blueDeviceActionListener.onActionFailure(blueDevice,blueAction);
            return false;
        }
    }

    public boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            Method localMethod = gatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(gatt);
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return false;
    }

    public void open(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && blueDevice.isBLEDevice()){
            connectBluetoothLowEnergyDevice(blueDevice,blueDeviceConnectionListener);
        }else{
            connectBluetoothDevice(blueDevice,blueDeviceConnectionListener);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void connectBluetoothLowEnergyDevice(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener){
        blueDevice.getBluetoothDevice().connectGatt(context, false, new BluetoothGattCallback() {

            private TimerTask timerTask;
            private boolean taskCanceled = false;
            private boolean disconnected = false;

            private void onDisconnect(final BluetoothGatt gatt) {
                gatt.close();

                if (blueDevice.getCurrentAction() != null) {
                    BlueAction completedAction = blueDevice.getCurrentAction();
                    // cleared before callback to be able to perform another action right after current one
                    blueDevice.setCurrentAction(null);
                    if (blueDeviceActionListener != null) {
                        blueDeviceActionListener.onActionFailure(blueDevice, completedAction);
                    }
                }
                if (blueDeviceConnectionListener != null) {
                    blueDeviceConnectionListener.onDeviceClosed(blueDevice);
                }

                disconnected = true;
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                if (blueDeviceConnectionListener != null) {
                    if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                        blueDevice.setBluetoothGatt(gatt);
                        gatt.discoverServices();
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                taskCanceled = true;
                                gatt.disconnect();
                                onDisconnect(gatt);
                            }
                        };

                        timer.schedule(timerTask, blueConfig.getServiceDiscoveryTimeoutMillis());
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (!disconnected) {
                            onDisconnect(gatt);
                        }
                    } else {
                        if (!disconnected) {
                            onDisconnect(gatt);
                        }
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (!taskCanceled) {
                    if (timerTask != null) {
                        timerTask.cancel();
                        timerTask = null;
                    }
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (blueDeviceConnectionListener != null) {
                            blueDeviceConnectionListener.onDeviceReady(blueDevice);
                        }
                    } else {
                        gatt.disconnect();
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (blueDevice.getCurrentAction() != null && blueDevice.getCurrentAction() instanceof NotifyAction) {
                    if (status == 0 ) {
                        BlueAction completedAction = blueDevice.getCurrentAction();
                        // cleared before callback to be able to perform another action right after current one

                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionSuccess(blueDevice, completedAction,descriptor.getValue());
                        }

                    } else {
                        BlueAction completedAction = blueDevice.getCurrentAction();
                        // cleared before callback to be able to perform another action right after current one
                        blueDevice.setCurrentAction(null);
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionFailure(blueDevice, completedAction);
                        }
                    }
                }
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                if (blueDevice.getCurrentAction() != null && blueDevice.getCurrentAction() instanceof NotifyAction) {
                    BlueAction completedAction = blueDevice.getCurrentAction();
                    // cleared before callback to be able to perform another action right after current one
                    blueDevice.setCurrentAction(null);
                    if (blueDeviceActionListener != null) {
                        blueDeviceActionListener.onActionSuccess(blueDevice, completedAction, characteristic.getValue());
                    }
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (blueDevice.getCurrentAction() != null && blueDevice.getCurrentAction() instanceof ReadAction) {

                    BlueAction completedAction = blueDevice.getCurrentAction();
                    // cleared before callback to be able to perform another action right after current one
                    blueDevice.setCurrentAction(null);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionSuccess(blueDevice, completedAction, characteristic.getValue());
                        }
                    } else {
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionFailure(blueDevice, completedAction);
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (blueDevice.getCurrentAction() != null && blueDevice.getCurrentAction() instanceof WriteAction) {
                    if (status == BluetoothGatt.GATT_SUCCESS && Arrays.equals(characteristic.getValue(), ((WriteAction) blueDevice.getCurrentAction()).getValue())) {
                        BlueAction completedAction = blueDevice.getCurrentAction();
                        // cleared before callback to be able to perform another action right after current one
                        blueDevice.setCurrentAction(null);
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionSuccess(blueDevice, completedAction, ((WriteAction) completedAction).getValue());
                        }

                    } else {
                        BlueAction completedAction = blueDevice.getCurrentAction();
                        // cleared before callback to be able to perform another action right after current one
                        blueDevice.setCurrentAction(null);
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onActionFailure(blueDevice, completedAction);
                        }
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void connectBluetoothDevice(BlueDevice blueDevice, final BlueDeviceConnectionListener blueDeviceConnectionListener){
        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = blueDevice.getBluetoothDevice().createRfcommSocketToServiceRecord(blueConfig.getBluetoothClassicServerUUID());
        } catch (IOException e) {
            Timber.e("Socket's create() method failed");
            blueDeviceConnectionListener.onDeviceClosed(blueDevice);
        }
        if(tmp!=null) {
            blueDevice.setBluetoothSocket(tmp);
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                blueDevice.getBluetoothSocket().connect();
                blueDeviceConnectionListener.onDeviceReady(blueDevice);
            } catch (IOException connectException) {
                // Unable to connect; close the socket.
                blueDeviceConnectionListener.onDeviceClosed(blueDevice);
                close(blueDevice);
            }
        }
    }

    public void close(BlueDevice blueDevice) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && blueDevice.isBLEDevice()){
            BluetoothGatt bluetoothGatt = blueDevice.getBluetoothGatt();
            if (bluetoothGatt != null) {
                bluetoothGatt.disconnect();
                blueDevice.setBluetoothGatt(null);
            }
        }else{
            try {
                blueDevice.getBluetoothSocket().close();
                blueDevice.setBluetoothSocket(null);
            } catch (IOException e) {
                Timber.e("Could not close the client socket");
            }
        }
    }
}
