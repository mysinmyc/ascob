package ascob.impl.backend;

import org.springframework.beans.factory.BeanNameAware;

import ascob.backend.ExecutionBackend;

public abstract class ExecutionBackendBase implements BeanNameAware, ExecutionBackend{

	String id;
	
	@Override
	public void setBeanName(String name) {
		this.id=name;
	}
	
	@Override
	public String getId() {
		return id;
	}

}
