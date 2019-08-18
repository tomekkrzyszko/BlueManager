package pl.tomek_krzyszko.bluemanager.dagger;

import android.content.Context;

import pl.tomek_krzyszko.bluemanager.BlueConfig;
import pl.tomek_krzyszko.bluemanager.dagger.components.BlueManagerComponent;
import pl.tomek_krzyszko.bluemanager.dagger.components.DaggerBlueManagerComponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.BlueManagerModule;

public class ApplicationScope {
    private static BlueManagerComponent component;

    public static BlueManagerComponent getComponent(Context context, BlueConfig blueConfig) {
        if (component == null) {
            component = DaggerBlueManagerComponent.builder()
                    .blueManagerModule(new BlueManagerModule(context, blueConfig))
                    .build();
        }
        return component;
    }

    public static void setComponent(BlueManagerComponent component) {
        ApplicationScope.component = component;
    }

    public static void clearComponent() {
        component = null;
    }
}
