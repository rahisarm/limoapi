package com.gateway.limoapi.model;

public class ConfigModel {

	private String field_nme,value;

	public String getField_nme() {
		return field_nme;
	}

	public void setField_nme(String field_nme) {
		this.field_nme = field_nme;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public ConfigModel() {
		
	}
	public ConfigModel(String field_nme, String value) {
		this.field_nme = field_nme;
		this.value = value;
	}
	
	
}
