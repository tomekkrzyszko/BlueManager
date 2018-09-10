package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Subcomponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.DeviceModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;
import pl.tomek_krzyszko.bluemanager.device.BlueDeviceController;

@InstanceScope
@Subcomponent(modules = {DeviceModule.class})
public interface DeviceComponent {
    void inject(BlueDeviceController blueDeviceController);
}
