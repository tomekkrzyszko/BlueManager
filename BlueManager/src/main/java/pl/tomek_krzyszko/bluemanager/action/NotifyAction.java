package pl.tomek_krzyszko.bluemanager.action;

import java.util.UUID;

public class NotifyAction extends BlueAction {

    public NotifyAction(UUID service, UUID characteristic) {
        super(service, characteristic);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.NOTIFY;
    }
}