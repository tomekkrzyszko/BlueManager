package pl.tomek_krzyszko.bluemanager.callback;


import pl.tomek_krzyszko.bluemanager.device.BlueDevice;

/**
 * Used to receive various callbacks from device scanning.
 */
public interface BlueDeviceScanListener {

    /**
     * Called when bluetooth device is scanned for the first time (or it has been lost earlier)
     *
     * @param blueDevice cumulative data package with discovered device
     */
    void onDeviceFound(BlueDevice blueDevice);

    /**
     * Called when bluetooth device was discovered earlier and it couldn't be found anymore
     *
     * @param blueDevice cumulative data package with discovered device
     */
    void onDeviceLost(BlueDevice blueDevice);

    /**
     * Called when bluetooth device was discovered earlier and it's data was updated
     *
     * @param blueDevice cumulative data package with discovered device
     */
    void onDeviceUpdate(BlueDevice blueDevice);

    /**
     * Called when bluetooth scanning failed
     *
     * @param errorCode one of {@link android.bluetooth.le.ScanCallback}'s SCAN_FAILED_* values
     */
    void onDeviceScanError(int errorCode);
}