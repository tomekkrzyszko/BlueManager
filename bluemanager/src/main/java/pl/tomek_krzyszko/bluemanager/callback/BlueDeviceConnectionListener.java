package pl.tomek_krzyszko.bluemanager.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;

import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

public interface BlueDeviceConnectionListener {

    /**
     * Callback initiated when connection to the device is established and it's services are discovered
     *  it's {@link BluetoothSocket} is connected
     * @param blueDevice {@link BlueDevice} object
     */
    void onDeviceReady(BlueDevice blueDevice);

    /**
     * Callback initiated when connection to the device is closed.
     * {@link BluetoothGatt#disconnect()} and {@link BluetoothGatt#close()}
     * or {@link BluetoothSocket#close()}  have been called.
     * @param blueDevice {@link BlueDevice} object
     */
    void onDeviceClosed(BlueDevice blueDevice);
}
