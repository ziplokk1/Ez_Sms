package main;

import java.io.Serializable;

public class Contact implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7258441517605774691L;
	
	private String name = null;
	private String number = null;
	private String message = null;
	
	public String getName() {
		return name;
	}
	public String getNumber() { 
		return number;
	} 
	
	public String getMessage() { 
		return message;
	}
	
	public void setMessage(String s) { message = s; }
	public void setName(String s) { name = s; } 
	public void setNumber(String s) { number = s; }
}
