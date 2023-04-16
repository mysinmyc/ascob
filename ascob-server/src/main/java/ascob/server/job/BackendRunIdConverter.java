package ascob.server.job;

import ascob.backend.BackendRunId;
import ascob.server.util.SerializationConverterBase;

public class BackendRunIdConverter extends SerializationConverterBase<BackendRunId> {

	@Override
	protected Class<BackendRunId> getObjecType() {
		return BackendRunId.class;
	}

}
