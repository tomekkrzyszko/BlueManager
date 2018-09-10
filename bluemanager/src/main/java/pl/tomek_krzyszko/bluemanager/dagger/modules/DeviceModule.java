package pl.tomek_krzyszko.bluemanager.dagger.modules;

import java.util.Timer;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.InstanceScope;

@Module
public class DeviceModule {

    public DeviceModule(){}

    @InstanceScope
    @Provides
    public Timer provideTimer(){
        return new Timer();
    }
}
