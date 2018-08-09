package dagger.module;

import dagger.Module;
import dagger.Provides;
import dagger.scope.PerActivity;
import pl.tomekkrzyszko.bluemanager.MainPresenter;

@Module
public class MainModule {

    @PerActivity
    @Provides
    public MainPresenter provideMainPresenter() {
        return new MainPresenter();
    }
}
