package pl.tomekkrzyszko.bluemanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomek_krzyszko.bluemanager.callback.BlueDeviceScanListener;
import pl.tomek_krzyszko.bluemanager.callback.BlueScannerServiceConnection;
import pl.tomek_krzyszko.bluemanager.device.BlueDevice;
import pl.tomek_krzyszko.bluemanager.exception.BlueManagerExceptions;
import pl.tomekkrzyszko.bluemanager.dagger.module.MainModule;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.state) TextView state;

    @Inject BlueManager blueManager;

    private String[] states = {"Disconnected","Connecting","Connected"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inject();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        startBluetoothService();
    }

    private void inject(){
        BlueManagerApplication.get(this).getAppComponent()
                .module(new MainModule())
                .inject(this);
    }

    @Override
    protected void onDestroy() {
        blueManager.stopBlueScanner();
        super.onDestroy();
    }

    private void startBluetoothService(){
        if(blueManager.checkBluetooth(this)) {
            try {
                blueManager.startBlueScanner(blueScannerServiceConnection);
            } catch (BlueManagerExceptions blueManagerExceptions) {
                blueManagerExceptions.printStackTrace();
            }
        }else{
            Log.d("GRANT","BLUETOOTH NOT GRANTED");
        }
    }

    private BlueScannerServiceConnection blueScannerServiceConnection = new BlueScannerServiceConnection() {
        @Override
        public void onConnected() {
            Log.d("SERVICE","onConnected");
            startScanning();
        }

        @Override
        public void onDisconnected() {
            Log.d("SERVICE","onDisconnected");
        }
    };

    private void startScanning(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }else{
            Log.d("MainActivity","StartScanning");
           blueManager.addBlueDeviceScanListener(new BlueDeviceScanListener() {
               @Override
               public void onDeviceFound(BlueDevice blueDevice) {
                   Log.d("SCANNER","Discoverd: "+blueDevice.getAddress());
               }

               @Override
               public void onDeviceLost(BlueDevice blueDevice) {
                   Log.d("SCANNER","Lost: "+ blueDevice.getAddress());
               }

               @Override
               public void onDeviceUpdate(BlueDevice blueDevice) {
                   Log.d("SCANNER","Update: "+ blueDevice.getAddress());
               }

               @Override
               public void onDeviceScanError(int errorCode) {
                   Log.d("SCANNER","Error: "+ errorCode);
               }
           });
           blueManager.startScanning(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.d("GRANT","LOCATION NOT GRANTED");
                }else{
                    startScanning();
                }
            }
        }
    }
}
