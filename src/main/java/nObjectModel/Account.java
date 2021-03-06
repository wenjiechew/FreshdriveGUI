/**
 * The account model to instantiate a new account object 
 * whenever the user is going to be logged in 
 *
 *      
 */

package nObjectModel;

public class Account {
	private static Account instance = null;
	private String _id;
	private String username;
	private String email;
	private String password;
	private String _token;

	/**
	 * The singleton object method to ensure that any 1 point of time only 1
	 * instance of account object is exists in the system
	 * 
	 * @return Account the singleton Account object
	 */
	public static Account getAccount() {
		if (instance == null) {
			instance = new Account();
		}
		return instance;
	}

	/**
	 * When log out, reset all variables before user logs in again, possibly to
	 * another account.
	 */
	public void clearInstance() {
		instance.set_id(null);
		instance.set_token(null);
		instance.setEmail(null);
		instance.setPassword(null);
		instance.setUsername(null);
	}

	public String get_token() {
		return new String(_token);
	}

	public void set_token(String _token) {
		this._token = _token;
	}

	public String get_id() {
		return new String(_id);
	}

	public String getUsername() {
		if (username != null) {
			return new String(username);
		} else
			return username;

	}

	public String getPassword() {
		return new String(password);
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
		return new String(email);
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
