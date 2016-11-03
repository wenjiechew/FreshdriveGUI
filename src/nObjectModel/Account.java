package nObjectModel;

public class Account {
	private String _id;
	private String username;
	private String email;
	private String password;
	private String _token;

	public String get_token() {
		return _token;
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public String get_id() {
		return _id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	
	public void set_id(String _id) {
		this._id = _id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	

}
