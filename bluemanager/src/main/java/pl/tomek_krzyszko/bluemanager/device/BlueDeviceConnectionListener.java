package pl.tomek_krzyszko.bluemanager.device;

import android.bluetooth.BluetoothGatt;

public interface BlueDeviceConnectionListener {

    /**
     * Callback initiated when connection to the device is established and it's services are discovered.
     *
     * @param blueDeviceController {@link BlueDeviceController} object managing connection to the device.
     */
    void onDeviceReady(BlueDeviceController blueDeviceController);

    /**
     * Callback initiated when connection to the device is closed.
     * {@link BluetoothGatt#disconnect()} and {@link BluetoothGatt#close()} have been called.
     *
     * @param blueDeviceController {@link BlueDeviceController} object managing connection to the device.
     */
    void onDeviceClosed(BlueDeviceController blueDeviceController);
}
