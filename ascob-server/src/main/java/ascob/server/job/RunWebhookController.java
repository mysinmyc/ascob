package ascob.server.job;


import ascob.server.backend.ExecutionBackendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks/runs")
public class RunWebhookController {

        @Autowired
        JobService jobService;

        @RequestMapping(method= RequestMethod.POST, path = "/{webhookId}")
        public void updateRun(@PathVariable("webhookId") String webhookId, @RequestBody UpdateRunWebhookRequest updateRunWebhookRequest) throws ExecutionBackendException {
                if (updateRunWebhookRequest.getIdentificationKeys() != null && ! updateRunWebhookRequest.getIdentificationKeys().isEmpty()) {
                        jobService.updateRunBackendIdenficationKeysByWebhookId(webhookId,updateRunWebhookRequest.getIdentificationKeys());
                }
                if (updateRunWebhookRequest.getStatus() !=null) {
                        jobService.updateRunStatusByWebhookId(webhookId, updateRunWebhookRequest.getStatus());
                }
        }
}
