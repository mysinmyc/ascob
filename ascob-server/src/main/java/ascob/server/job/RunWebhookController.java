package ascob.server.job;


import ascob.job.UpdateRunWebhookRequest;
import ascob.security.Permission;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/webhooks/runs")
public class RunWebhookController {

        @Autowired
        SecurityAssertionService securityAssertionService;
        @Autowired
        JobService jobService;

        @RequestMapping(method= RequestMethod.POST, path = "/{webhookId}")
        public void updateRun(@PathVariable("webhookId") String webhookId, @RequestBody UpdateRunWebhookRequest updateRunWebhookRequest, Authentication authentication) throws ExecutionBackendException, NotAuthorizedException {
                if (updateRunWebhookRequest.getIdentificationKeys() != null && ! updateRunWebhookRequest.getIdentificationKeys().isEmpty()) {
                        securityAssertionService.assertAuthorized(authentication, Permission.webhook_identification_keys);
                        jobService.updateRunBackendIdenficationKeysByWebhookId(webhookId,updateRunWebhookRequest.getIdentificationKeys());
                }
                if (updateRunWebhookRequest.getStatus() !=null) {
                        securityAssertionService.assertAuthorized(authentication, Permission.webhook_update_status);
                        jobService.updateRunStatusByWebhookId(webhookId, updateRunWebhookRequest.getStatus());
                }
        }

        @RequestMapping(method=RequestMethod.GET, path = "/{webhookId}/files/{fileId}")
        public void getFile(@PathVariable("webhookId") String webhookId, @PathVariable("fileId") String fileId, HttpServletResponse response, Authentication authentication) throws ExecutionBackendException, IOException, NotAuthorizedException {
                securityAssertionService.assertAuthorized(authentication, Permission.webhook_get_files);
                jobService.downloadFileIntoByWebhookId(webhookId, fileId, response.getOutputStream());
        }
}
