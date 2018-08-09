package pl.tomek_krzyszko.bluemanager.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

import java.io.Serializable;

public class BlueDevice implements Serializable {

    /**
     * bluetooth device
     */
    private BluetoothDevice bluetoothDevice;

    /**
     * bluetooth gatt
     */
    private BluetoothGatt bluetoothGatt;

    /**
     * hardware bluetooth device address
     */
    private String address;

    /**
     * bluetooth device name
     */
    private String name;

    /**
     * time in milliseconds when the device was last scanned
     */
    private long discoveredTimestamp;

    /**
     * estimated distance in meters
     */
    private double distance;

    public BlueDevice() {
    }

    /**
     * @return hardware unique address of the device
     */
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return last point in time when the device was scanned
     */
    public long getDiscoveredTimestamp() {
        return discoveredTimestamp;
    }

    public void setDiscoveredTimestamp(long discoveredTimestamp) {
        this.discoveredTimestamp = discoveredTimestamp;
    }

    /**
     * @return last known distance to the device
     */
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    @Override
    public String toString() {
        return address;
    }
}
