package com.cpm.xmlGetterSetter;

import java.util.ArrayList;

public class NonWorkingReasonGetterSetter {
	
	String nonworking_table;
	
	ArrayList<String> reason_cd=new ArrayList<String>();
	ArrayList<String> reason=new ArrayList<String>();
	
	
	public String getNonworking_table() {
		return nonworking_table;
	}
	public void setNonworking_table(String nonworking_table) {
		this.nonworking_table = nonworking_table;
	}
	public ArrayList<String> getReason_cd() {
		return reason_cd;
	}
	public void setReason_cd(String reason_cd) {
		this.reason_cd.add(reason_cd);
	}
	public ArrayList<String> getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason.add(reason);
	}
}
