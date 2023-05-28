package ascob.server.security;

import java.util.List;

public class WhoAmIResponse {

	List<String> authorities;


	public List<String> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}
}
