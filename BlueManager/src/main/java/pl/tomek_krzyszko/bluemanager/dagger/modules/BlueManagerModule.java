package pl.tomek_krzyszko.bluemanager.dagger.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.BlueConfig;

@Module
public class BlueManagerModule {
    private final Context context;
    private final BlueConfig blueConfig;

    public BlueManagerModule(Context context, BlueConfig blueConfig) {
        this.context = context;
        this.blueConfig = blueConfig;
    }

    @Singleton
    @Provides
    public Context provideApplicationContext() {
        return context;
    }

    @Singleton
    @Provides
    public BlueConfig provideBlueConfig(){
        if(blueConfig != null) {
            return blueConfig;
        }else{
            return new BlueConfig();
        }
    }
}
