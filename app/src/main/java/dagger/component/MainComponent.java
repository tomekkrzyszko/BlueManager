package dagger.component;

import dagger.Subcomponent;
import dagger.module.MainModule;
import dagger.scope.PerActivity;
import pl.tomekkrzyszko.bluemanager.MainActivity;


@PerActivity
@Subcomponent(
        modules = {MainModule.class})
public interface MainComponent {
    void inject(MainActivity splashActivity);
}
