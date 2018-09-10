package pl.tomek_krzyszko.bluemanager.callback;

import pl.tomek_krzyszko.bluemanager.action.BlueAction;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

public interface BlueDeviceActionListener {

    /**
     * Callback initiated when a {@link BlueAction} was performed successfully.
     *
     * @param blueDevice {@link BlueDevice} object managing connection to the device.
     * @param blueAction {@link BlueAction} action that was performed
     * @param value binary value of characteristic or stream
     */
    void onActionSuccess(BlueDevice blueDevice, BlueAction blueAction, byte[] value);

    /**
     * Callback initiated when a {@link BlueAction} failed.
     *
     * @param blueDevice {@link BlueDevice} object managing connection to the device.
     * @param blueAction {@link BlueAction} action that was performed
     */
    void onActionFailure(BlueDevice blueDevice, BlueAction blueAction);

}

