package main;

import java.io.Serializable;

public class Message implements Serializable { 
	/**
	 * 
	 */
private static final long serialVersionUID = 1L;
	
	public static enum Status { 
		INCOMING, OUTGOING;
	}
	
	private String recipient; //Phone number
	private String message; 
	private Status status;
	
	public Message(String recipient, String message, Status status) { 
		this.recipient = recipient;
		this.message = message;
		this.status = status;
	}
	
	public void setStatus(Status s) { status = s; }
	public void setRecipient(String s) { recipient = s; } 
	public void setMessage(String s) { message = s; }
	
	public Status getStatus() { return status; }
	public String getRecipient() { return recipient; } 
	public String getMessage() { return message; }
}
