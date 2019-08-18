package pl.tomekkrzyszko.bluemanager;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueScannerServiceConnection;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.exception.BlueManagerExceptions;
import timber.log.Timber;

public class MainViewModel extends ViewModel{

    private final BlueManager blueManager;
    private final MutableLiveData<BlueDevice> newDevice = new MutableLiveData<>();
    private final MutableLiveData<BlueDevice> updateDevice = new MutableLiveData<>();
    private final MutableLiveData<BlueDevice> removeDevice = new MutableLiveData<>();
    private final MutableLiveData<Boolean> bluetoothServiceStarted = new MutableLiveData<>();

    public MainViewModel(BlueManager blueManager) {
        this.blueManager = blueManager;
    }

    LiveData<BlueDevice> addNewDevice() {
        return newDevice;
    }

    LiveData<BlueDevice> updateDevice() {
        return updateDevice;
    }

    LiveData<BlueDevice> removeDevice() {
        return removeDevice;
    }

    LiveData<Boolean> bluetoothService() {
        return bluetoothServiceStarted;
    }

    public void startBluetoothService(Context context) {
        if (blueManager.checkBluetooth(context)) {
            try {
                blueManager.startBlueScanner(blueScannerServiceConnection);
            } catch (BlueManagerExceptions blueManagerExceptions) {
                blueManagerExceptions.printStackTrace();
            }
        } else {
            Timber.d("BLUETOOTH NOT GRANTED");
        }
    }

    private BlueScannerServiceConnection blueScannerServiceConnection = new BlueScannerServiceConnection() {
        @Override
        public void onConnected() {
            Timber.d("BlueScannerServiceConnection: CONNECTED");
            bluetoothServiceStarted.postValue(true);
        }

        @Override
        public void onDisconnected() {
            Timber.d("BlueScannerServiceConnection: DISCONNECTED");
            bluetoothServiceStarted.postValue(false);
        }
    };

    public void startScanning() {
        Timber.d("Start Scanning");
        blueManager.addBlueDeviceScanListener(new BlueDeviceScanListener() {
            @Override
            public void onDeviceFound(BlueDevice blueDevice) {
                Timber.d("Discovered: %s", blueDevice.getAddress());
                newDevice.postValue(blueDevice);
            }

            @Override
            public void onDeviceLost(BlueDevice blueDevice) {
                Timber.d("Lost: %s", blueDevice.getAddress());
                removeDevice.postValue(blueDevice);
            }

            @Override
            public void onDeviceUpdate(BlueDevice blueDevice) {
                Timber.d("Update: %s", blueDevice.getAddress());
                updateDevice.postValue(blueDevice);
            }

            @Override
            public void onDeviceScanError(int errorCode) {
                Timber.e("Error: %s", errorCode);
            }
        });
        blueManager.startScanning(true);
    }

    private void stopScanning(){
        blueManager.stopBlueScanner();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopScanning();
    }
}
