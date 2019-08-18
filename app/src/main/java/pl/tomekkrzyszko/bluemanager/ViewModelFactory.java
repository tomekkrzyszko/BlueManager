package pl.tomekkrzyszko.bluemanager;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import pl.tomek_krzyszko.bluemanager.BlueManager;

public class ViewModelFactory implements ViewModelProvider.Factory {

    private final BlueManager blueManager;

    public ViewModelFactory(BlueManager blueManager) {
        this.blueManager = blueManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {
            return (T) new MainViewModel(blueManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
