package org.ranran.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2161159638135962073L;

	public String name;
	
	public Date born;

	public User(String name){
	
		this.name = name;
		
		this.born = new Date();
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBorn() {
		return born;
	}

	public void setBorn(Date born) {
		this.born = born;
	}
	
	public String toString(){
		return "username:" + this.getName()+"; born:" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.ssssss").format(this.born); 
	}
	
}
