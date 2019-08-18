package pl.tomekkrzyszko.bluemanager.dagger;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import pl.tomekkrzyszko.bluemanager.MainActivity;
import pl.tomekkrzyszko.bluemanager.MainModule;

/**
 * Binds all sub-components within the app.
 */
@Module
public abstract class BuildersModule {

    @ContributesAndroidInjector(modules = MainModule.class)
    abstract MainActivity bindMainActivity();

}