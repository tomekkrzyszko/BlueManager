package pl.tomek_krzyszko.bluemanager;

import pl.tomek_krzyszko.bluemanager.scanner.BlueScanner;

/**
 * Used to notify about service being connected and disconnected
 */
interface BlueScannerServiceConnection {
    /**
     * Provides successfully connected {@link BlueScanner}
     *
     * @param blueScanner service connected to
     */
    void onConnected(BlueScanner blueScanner);

    /**
     * Notifies about service being disconnected.
     */
    void onDisconnected();
}
