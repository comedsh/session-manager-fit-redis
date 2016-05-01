package org.ranran.tomcat.redissession;

public class SessionManagerException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5042830067692345346L;

	public SessionManagerException(String message){
		
		super(message);
		
	}	
	
	public SessionManagerException(String message, Throwable e){
		
		super(message, e);
		
	}
	
}
