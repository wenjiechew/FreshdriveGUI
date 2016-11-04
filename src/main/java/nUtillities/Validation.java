package nUtillities;

import java.util.regex.*;

public class Validation {	
	private final static String PWD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
	
	private Matcher matched;
	private Pattern pattern;
	
	public Validation(){
		this.pattern = Pattern.compile(PWD_PATTERN);
	}
	
	public boolean isGoodPassword(String password){
		matched = pattern.matcher(password);
		return matched.matches();
	}

}
