package pl.tomek_krzyszko.bluemanager.dagger.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.ApplicationScope;

@Module
public class BlueManagerModule {
    private final Context context;
    private final BlueConfig blueConfig;

    public BlueManagerModule(Context context, BlueConfig blueConfig) {
        this.context = context;
        this.blueConfig = blueConfig;
    }

    @ApplicationScope
    @Provides
    public Context provideApplicationContext() {
        return context;
    }

    @ApplicationScope
    @Provides
    public BlueConfig provideBlueConfig(){
        if(blueConfig != null) {
            return blueConfig;
        }else{
            return BlueConfig.builder().build();
        }
    }
}
