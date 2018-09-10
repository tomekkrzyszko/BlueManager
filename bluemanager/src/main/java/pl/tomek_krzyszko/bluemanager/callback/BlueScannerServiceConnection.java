package pl.tomek_krzyszko.bluemanager.callback;

import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;

/**
 * Used to notify about {@link BlueScanner} service being connected and disconnected
 */
public interface BlueScannerServiceConnection {
    /**
     * Notifies about service being connected.
     */
    void onConnected();

    /**
     * Notifies about service being disconnected.
     */
    void onDisconnected();
}
