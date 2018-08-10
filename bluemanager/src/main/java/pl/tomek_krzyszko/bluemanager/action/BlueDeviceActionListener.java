package pl.tomek_krzyszko.bluemanager.action;

import pl.tomek_krzyszko.bluemanager.device.BlueDeviceController;

public interface BlueDeviceActionListener {

    /**
     * Callback initiated when a {@link BlueAction} was performed successfully.
     *
     * @param blueDeviceController              {@link BlueDeviceController} object managing connection to the beacon.
     * @param blueAction {@link BlueAction} characteristic action that was performed
     * @param value                binary value of characteristic
     */
    void onSuccess(BlueDeviceController blueDeviceController, BlueAction blueAction, byte[] value);

    /**
     * Callback initiated when a {@link BlueAction} failed.
     *
     * @param blueDeviceController              {@link BlueDeviceController} object managing connection to the beacon.
     * @param blueAction {@link BlueAction} characteristic action that was performed
     */
    void onFailure(BlueDeviceController blueDeviceController, BlueAction blueAction);

}

