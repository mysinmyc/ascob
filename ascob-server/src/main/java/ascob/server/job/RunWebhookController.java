package ascob.server.job;


import ascob.job.UpdateRunWebhookRequest;
import ascob.security.Permission;
import ascob.server.ApiInfo;
import ascob.server.backend.ExecutionBackendException;
import ascob.server.security.NotAuthorizedException;
import ascob.server.security.SecurityAssertionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/webhooks/runs")
@SecurityRequirements({@SecurityRequirement(name = ApiInfo.API_TOKEN_SCHEMA_NAME)})
public class RunWebhookController {

        @Autowired
        SecurityAssertionService securityAssertionService;
        @Autowired
        JobService jobService;

        @Operation(description = "Update run info by using webhook. Some backend implementation are not aware abound identification keys or job status at submission. In this case the underling job must use this webhook to update run identifications keys and status")
        @RequestMapping(method= RequestMethod.POST, path = "/{webhookId}")
        public void updateRun(@Parameter(description = "Webhook id") @PathVariable("webhookId") String webhookId, @RequestBody UpdateRunWebhookRequest updateRunWebhookRequest, Authentication authentication) throws ExecutionBackendException, NotAuthorizedException, RunNotFoundException {
                if (updateRunWebhookRequest.getIdentificationKeys() != null && ! updateRunWebhookRequest.getIdentificationKeys().isEmpty()) {
                        securityAssertionService.assertAuthorized(authentication, Permission.webhook_identification_keys);
                        jobService.updateRunBackendIdenficationKeysByWebhookId(webhookId,updateRunWebhookRequest.getIdentificationKeys());
                }
                if (updateRunWebhookRequest.getStatus() !=null) {
                        securityAssertionService.assertAuthorized(authentication, Permission.webhook_update_status);
                        jobService.updateRunStatusByWebhookId(webhookId, updateRunWebhookRequest.getStatus());
                }
        }

        @Operation(description = "Allow jobs to retrieve files attached to execution")
        @RequestMapping(method=RequestMethod.GET, path = "/{webhookId}/files/{fileId}")
        public void getFile(@Parameter(description = "Webhook id") @PathVariable("webhookId") String webhookId,@Parameter(description = "File identifier") @PathVariable("fileId") String fileId, HttpServletResponse response, Authentication authentication) throws ExecutionBackendException, IOException, NotAuthorizedException, RunNotFoundException {
                securityAssertionService.assertAuthorized(authentication, Permission.webhook_get_files);
                jobService.downloadFileIntoByWebhookId(webhookId, fileId, response.getOutputStream());
        }
}
