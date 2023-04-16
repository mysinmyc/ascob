package ascob.server.job;

import ascob.api.JobSpec;
import ascob.server.util.SerializationConverterBase;

public class JobSpecConverter extends SerializationConverterBase<JobSpec>{

	@Override
	protected Class<JobSpec> getObjecType() {
		return JobSpec.class;
	}

}
