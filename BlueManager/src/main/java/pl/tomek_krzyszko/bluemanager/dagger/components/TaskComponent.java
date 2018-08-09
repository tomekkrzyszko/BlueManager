package pl.tomek_krzyszko.bluemanager.dagger.components;

import dagger.Subcomponent;
import pl.tomek_krzyszko.bluemanager.dagger.modules.TaskModule;
import pl.tomek_krzyszko.bluemanager.dagger.scopes.ApplicationScope;

@ApplicationScope
@Subcomponent(modules = {TaskModule.class})
public interface TaskComponent {

}
