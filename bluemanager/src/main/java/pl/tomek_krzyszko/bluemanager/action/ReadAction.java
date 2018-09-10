package pl.tomek_krzyszko.bluemanager.action;

import java.util.UUID;

public class ReadAction extends BlueAction {

    public ReadAction(UUID service, UUID characteristic) {
        super(service, characteristic);
    }

    public ReadAction() {
        super();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.READ;
    }
}