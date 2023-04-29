package ascob.server.job;

import ascob.api.RunStatus;

import java.util.Map;

public class UpdateRunWebhookRequest {

    Map<String,String> identificationKeys;

    RunStatus status;

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Map<String, String> getIdentificationKeys() {
        return identificationKeys;
    }

    public void setIdentificationKeys(Map<String, String> identificationKeys) {
        this.identificationKeys = identificationKeys;
    }


}
