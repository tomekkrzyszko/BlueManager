package pl.tomekkrzyszko.bluemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    @Inject ViewModelFactory viewModelFactory;

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.state)
    TextView state;
    @BindView(R.id.deviceList)
    RecyclerView deviceRV;

    private MainViewModel viewModel;
    DevicesAdapter devicesAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Map<String, BlueDevice> deviceList = new HashMap();
    private String[] states = {"Rozłączony", "Łączenie", "Połączony"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel.class);
        viewModel.addNewDevice().observe(this, this::processNewDevice);
        viewModel.updateDevice().observe(this, this::processUpdatedDevice);
        viewModel.removeDevice().observe(this, this::processLostDevice);
        viewModel.bluetoothService().observe(this, this::processBluetoothServiceResponse);

        devicesAdapter = new DevicesAdapter(this,deviceList);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        deviceRV.setAdapter(devicesAdapter);
        deviceRV.setLayoutManager(linearLayoutManager);

        viewModel.startBluetoothService(this);
    }

    private void processNewDevice(BlueDevice blueDevices){
        deviceList.put(blueDevices.getAddress(),blueDevices);
        devicesAdapter.updateDeviceList(deviceList);
    }

    private void processUpdatedDevice(BlueDevice blueDevices){
        deviceList.remove(blueDevices.getAddress());
        deviceList.put(blueDevices.getAddress(),blueDevices);
        devicesAdapter.updateDeviceList(deviceList);
    }

    private void processLostDevice(BlueDevice blueDevices){
        deviceList.remove(blueDevices.getAddress());
        devicesAdapter.updateDeviceList(deviceList);
    }
    private void processBluetoothServiceResponse(Boolean isStarted){
        if(isStarted){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }else{
                viewModel.startScanning();
            }
        }else{
            Timber.d("Bluetooth Service disconnected");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Timber.d("LOCATION NOT GRANTED");
                } else {
                    viewModel.startScanning();
                }
            }
        }
    }
}
