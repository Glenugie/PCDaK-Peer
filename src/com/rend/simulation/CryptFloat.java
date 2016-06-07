package com.rend.simulation;

public class CryptFloat {
	private float value;
	
	public CryptFloat(float value) {
		//This is some encryption function
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	public String toString() {
		return ""+value;
	}
}
