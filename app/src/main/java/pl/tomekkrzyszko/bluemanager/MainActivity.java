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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements OnDeviceClickListener {

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
    @BindView(R.id.swipeContainer)
    SwipeRefreshLayout swipeRefreshLayout;

    private MainViewModel viewModel;
    DevicesAdapter devicesAdapter;
    private LinearLayoutManager linearLayoutManager;
    private Map<String, BlueDevice> deviceList = new HashMap();
    private BlueDevice connectedDevice = null;
    private String[] states = {"Rozłączony", "Łączenie", "Połączony", "Rozłączanie"};

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
        viewModel.connectedDevice().observe(this, this::processConnectedDevice);
        viewModel.disconnectedDevice().observe(this, this::processDisconnectedDevice);
        viewModel.bluetoothService().observe(this, this::processBluetoothServiceResponse);

        devicesAdapter = new DevicesAdapter(this,deviceList, this);
        linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        deviceRV.setAdapter(devicesAdapter);
        deviceRV.setLayoutManager(linearLayoutManager);

        viewModel.startBluetoothService(this);

        swipeRefreshLayout.setOnRefreshListener(this::refreshDeviceList);
    }

    private void refreshDeviceList(){
        deviceList.clear();
        devicesAdapter.updateDeviceList(deviceList,connectedDevice);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void processNewDevice(BlueDevice blueDevices){
        deviceList.put(blueDevices.getAddress(),blueDevices);
        devicesAdapter.updateDeviceList(deviceList,connectedDevice);
    }

    private void processUpdatedDevice(BlueDevice blueDevice){
        deviceList.remove(blueDevice.getAddress());
        deviceList.put(blueDevice.getAddress(),blueDevice);
        devicesAdapter.updateDeviceList(deviceList,connectedDevice);
    }

    private void processLostDevice(BlueDevice blueDevice){
        deviceList.remove(blueDevice.getAddress());
        devicesAdapter.updateDeviceList(deviceList,connectedDevice);
    }

    private void processConnectedDevice(BlueDevice blueDevice){
        connectedDevice = blueDevice;
        state.setText(states[2]);
        state.setTextColor(getResources().getColor(R.color.green));
        devicesAdapter.updateDeviceList(deviceList,connectedDevice);
    }

    private void processDisconnectedDevice(BlueDevice blueDevice){
        connectedDevice = null;
        state.setText(states[0]);
        state.setTextColor(getResources().getColor(R.color.red));
        refreshDeviceList();
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

    @Override
    public void onDeviceClicked(BlueDevice blueDevice) {
        if(connectedDevice==null) {
            state.setText(states[1]);
            state.setTextColor(getResources().getColor(R.color.blue));
            connectedDevice=blueDevice;
            viewModel.connect(blueDevice);
        }else{
            Timber.d("onDevice clicked: "+blueDevice.getAddress() + " connectedDevice: " + connectedDevice.getAddress());
            if(connectedDevice.getAddress().equals(blueDevice.getAddress())){
                state.setText(states[3]);
                state.setTextColor(getResources().getColor(R.color.blue));
                viewModel.disconnectDevice(connectedDevice);
                connectedDevice = null;
            }
        }
    }
}
