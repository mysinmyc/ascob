package ascob.security;

import java.util.Arrays;
import java.util.Collection;

public enum Permission {
    job_submit("job", true),
    job_run_manual_start("job",true),
    job_run_upload_files("job",true),
    job_run_stop("job", true),
    job_run_read("job", false),
    job_run_output("job",false),
    job_run_refresh("job",false),
    job_run_search("job",false),
    webhook_update_status("webhook", true),
    webhook_identification_keys("webhook", true),
    webhook_get_files("webhook", false),
    security_token_write("security", true);


    String group;
    boolean write;
    Permission(String group, boolean write) {
        this.group = group;
        this.write = write;
    }

    public String getGroup (){
        return group;
    }

    public boolean isWrite() {
        return write;
    }

    public static Collection<Permission> byGroup(String group) {
        return byGroup(group,false);
    }
    public static Collection<Permission> byGroup(String group, boolean readOnly) {
        return Arrays.stream(values()).filter( p-> p.getGroup().equals(group) && ( ! (readOnly && p.isWrite()) )).toList();
    }
}
