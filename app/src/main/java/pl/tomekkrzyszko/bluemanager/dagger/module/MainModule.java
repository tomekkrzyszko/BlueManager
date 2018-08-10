package pl.tomekkrzyszko.bluemanager.dagger.module;

import dagger.Module;
import dagger.Provides;
import pl.tomekkrzyszko.bluemanager.dagger.scope.PerActivity;
import pl.tomekkrzyszko.bluemanager.MainPresenter;

@Module
public class MainModule {

    @PerActivity
    @Provides
    public MainPresenter provideMainPresenter() {
        return new MainPresenter();
    }
}
