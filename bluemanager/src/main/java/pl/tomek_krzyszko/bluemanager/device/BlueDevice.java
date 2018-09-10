package pl.tomek_krzyszko.bluemanager.device;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import pl.tomek_krzyszko.bluemanager.action.BlueAction;

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
     */
    private BluetoothSocket bluetoothSocket;

    /**
     * bluetooth input server
     */
    private InputStream inputStream;

    /**
     * bluetooth output server
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
    private long discoveredTimestamp;

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

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket bluetoothSocket) {
        this.bluetoothSocket = bluetoothSocket;
    }

    public InputStream getInputStream() throws IOException {
        inputStream = bluetoothSocket.getInputStream();
        return inputStream;
    }

    public OutputStream getOutputStream() throws IOException {
        outputStream = bluetoothSocket.getOutputStream();
        return outputStream;
    }

    public BlueAction getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(BlueAction currentAction) {
        this.currentAction = currentAction;
    }

    /**
     * check if device support bluetooth low energy
     * use bluetooth device type
     */
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
