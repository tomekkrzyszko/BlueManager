package pl.tomek_krzyszko.bluemanager.device;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import pl.tomek_krzyszko.bluemanager.action.BlueAction;
import pl.tomek_krzyszko.bluemanager.action.BlueDeviceActionListener;
import pl.tomek_krzyszko.bluemanager.action.NotifyAction;
import pl.tomek_krzyszko.bluemanager.action.ReadAction;
import pl.tomek_krzyszko.bluemanager.action.WriteAction;

public class BlueDeviceController {

    private Context context;
    private final BlueDevice blueDevice;
    private BluetoothGatt bluetoothGatt;

    private BlueAction currentAction;
    private BlueDeviceActionListener blueDeviceActionListener;

    /**
     * Time in milliseconds after which service discovery process is considered to have failed
     */
    private static final long SERVICE_DISCOVERY_TIMEOUT_MILLIS = 15000;
    private Timer timer = new Timer();

    public BlueDeviceController(Context context, BlueDevice blueDevice) {
        this.blueDevice = blueDevice;
        this.context = context;
    }

    public String getAddress() {
        return blueDevice.getAddress();
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public boolean performAction(BlueAction blueAction, BlueDeviceActionListener blueDeviceActionListener) {
        if (bluetoothGatt != null && currentAction == null) {
            for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {
                if (bluetoothGattService.getUuid().equals(blueAction.getService())) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(blueAction.getCharacteristic());
                    if (bluetoothGattCharacteristic != null) {
                        boolean isSuccessful = false;
                        this.blueDeviceActionListener = blueDeviceActionListener;
                        if (blueAction instanceof WriteAction) {
                            bluetoothGattCharacteristic.setValue(((WriteAction) blueAction).getValue());
                            isSuccessful = bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                            currentAction = blueAction;
                        } else if (blueAction instanceof ReadAction) {
                            currentAction = blueAction;
                            isSuccessful = bluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
                        }else if( blueAction instanceof NotifyAction){
                                for (BluetoothGattDescriptor descriptor : bluetoothGattCharacteristic.getDescriptors()) {
                                    UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                                    if (descriptor.getUuid().equals(uuid)) {
                                        currentAction = blueAction;
                                        bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);
                                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                        isSuccessful = bluetoothGatt.writeDescriptor(descriptor);
                                    }
                                }
                        }
                        if (!isSuccessful) {
                            currentAction = null;
                            this.blueDeviceActionListener = null;
                        } else {

                        }
                        return isSuccessful;
                    }
                }
            }
        }
        return false;
    }

    /**
     * http://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt) {
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

    public void open(final BlueDeviceConnectionListener blueDeviceConnectionListener) {
        bluetoothGatt = blueDevice.getBluetoothDevice().connectGatt(context, false, new BluetoothGattCallback() {

            private TimerTask timerTask;
            private boolean taskCanceled = false;
            private boolean disconnected = false;

            private void onDisconnect(final BluetoothGatt gatt) {
                gatt.close();

                if (currentAction != null) {
                    BlueAction completedAction = currentAction;
                    // cleared before callback to be able to perform another action right after current one
                    currentAction = null;
                    if (blueDeviceActionListener != null) {
                        blueDeviceActionListener.onFailure(BlueDeviceController.this, completedAction);
                    }
                }
                if (blueDeviceConnectionListener != null) {
                    blueDeviceConnectionListener.onDeviceClosed(BlueDeviceController.this);
                }

                disconnected = true;
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
                if (blueDeviceConnectionListener != null) {
                    if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.discoverServices();
                        timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                taskCanceled = true;
                                gatt.disconnect();
                                onDisconnect(gatt);
                            }
                        };

                        timer.schedule(timerTask, SERVICE_DISCOVERY_TIMEOUT_MILLIS);
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
                            blueDeviceConnectionListener.onDeviceReady(BlueDeviceController.this);
                        }
                    } else {
                        gatt.disconnect();
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (currentAction != null && currentAction instanceof NotifyAction) {
                    if (status == 0 ) {
                        BlueAction completedAction = currentAction;
                        // cleared before callback to be able to perform another action right after current one
                        currentAction = null;

                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onSuccess(BlueDeviceController.this, completedAction,descriptor.getValue());
                        }

                    } else {
                        BlueAction completedAction = currentAction;
                        // cleared before callback to be able to perform another action right after current one
                        currentAction = null;
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onFailure(BlueDeviceController.this, completedAction);
                        }
                    }
                }
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                Intent i = new Intent("care.shape.bluetooth.device.NOTIFY");
//                if(characteristic.getUuid().equals(BlueActionFactory.getValue1Action().getCharacteristic())){
//                    i.putExtra("response1",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getValue2Action().getCharacteristic())){
//                    i.putExtra("response2",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getValue3Action().getCharacteristic())){
//                    i.putExtra("response3",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getValue4Action().getCharacteristic())){
//                    i.putExtra("response4",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getValue5Action().getCharacteristic())){
//                    i.putExtra("response5",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getValue6Action().getCharacteristic())){
//                    i.putExtra("response6",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getAcelerometerXAction().getCharacteristic())){
//                    i.putExtra("responseX",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getAcelerometerYAction().getCharacteristic())){
//                    i.putExtra("responseY",data);
//                }else if(characteristic.getUuid().equals(BlueActionFactory.getAcelerometerZAction().getCharacteristic())){
//                    i.putExtra("responseZ",data);
//                }
               // if(currentAction==null) {
                context.sendBroadcast(i);
                //}
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (currentAction != null && currentAction instanceof ReadAction) {

                    BlueAction completedAction = currentAction;
                    // cleared before callback to be able to perform another action right after current one
                    currentAction = null;

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onSuccess(BlueDeviceController.this, completedAction, characteristic.getValue());
                        }
                    } else {
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onFailure(BlueDeviceController.this, completedAction);
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (currentAction != null && currentAction instanceof WriteAction) {
                    if (status == BluetoothGatt.GATT_SUCCESS && Arrays.equals(characteristic.getValue(), ((WriteAction) currentAction).getValue())) {
                            BlueAction completedAction = currentAction;
                            // cleared before callback to be able to perform another action right after current one
                            currentAction = null;
                        if (blueDeviceActionListener != null) {
                                blueDeviceActionListener.onSuccess(BlueDeviceController.this, completedAction, ((WriteAction) completedAction).getValue());
                            }

                    } else {
                        BlueAction completedAction = currentAction;
                        // cleared before callback to be able to perform another action right after current one
                        currentAction = null;
                        if (blueDeviceActionListener != null) {
                            blueDeviceActionListener.onFailure(BlueDeviceController.this, completedAction);
                        }
                    }
                }
            }
        });
    }

    public void close() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt = null;
        }
    }
}
