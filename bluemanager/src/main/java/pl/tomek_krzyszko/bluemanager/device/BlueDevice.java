package pl.tomek_krzyszko.bluemanager.device;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import pl.tomek_krzyszko.bluemanager.action.BlueAction;

/**
 * Main data model class wich represent Bluetooth Device in the library
 */
public class BlueDevice implements Serializable {

    /**
     * bluetooth device
     */
    private BluetoothDevice bluetoothDevice;

    /**
     * blue action
     */
    private BlueAction currentAction;

    /**
     * bluetooth server socket
     * for BLE devices this is null
     */
    private BluetoothSocket bluetoothSocket;

    /**
     * bluetooth input stream from bluetooth socket
     * oould be null when using BLE version
     */
    private InputStream inputStream;

    /**
     * bluetooth output stream from bluetooth socket
     * oould be null when using BLE version
     */
    private OutputStream outputStream;

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
    private long discoveredTimestamp; // wytłumaczyć

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

    @Nullable
    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    @Nullable
    public InputStream getInputStream() throws IOException {
        if(bluetoothSocket==null){
            return null;
        }
        inputStream = bluetoothSocket.getInputStream();
        return inputStream;
    }

    @Nullable
    public OutputStream getOutputStream() throws IOException {
        if(bluetoothSocket==null){
            return null;
        }
        outputStream = bluetoothSocket.getOutputStream();
        return outputStream;
    }

    public BlueAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(BlueAction currentAction) {
        this.currentAction = currentAction;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean isBLEDevice() {
        if(bluetoothDevice!=null){
            int deviceType = bluetoothDevice.getType();
            if(deviceType == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                return false;
            } else if(deviceType == BluetoothDevice.DEVICE_TYPE_LE) {
                return true;
            } else if(deviceType == BluetoothDevice.DEVICE_TYPE_DUAL) {
                return true;
            } else if(deviceType == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                return false;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        return address;
    }
}
