package pl.tomekkrzyszko.bluemanager;

import dagger.Module;
import dagger.Provides;
import pl.tomek_krzyszko.bluemanager.BlueManager;

@Module
public class MainModule {

    @Provides
    public ViewModelFactory provideViewModelFactory(BlueManager blueManager) {
        return new ViewModelFactory(blueManager);
    }
}
