package pl.tomekkrzyszko.bluemanager.dagger;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.BlueManager;
import pl.tomekkrzyszko.bluemanager.BlueManagerApplication;

@Module
public class AppModule {

    @Provides
    Context provideContext(BlueManagerApplication application) {
        return application.getApplicationContext();
    }

    @Provides
    SharedPreferences providesSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    BlueManager provideBlueManager(){
        return BlueManager.getInstance();
    }
}
