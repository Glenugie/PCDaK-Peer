package com.rend.simulation;

public class CryptInt {
	private int value;
	
	public CryptInt(int value) {
		//This is some encryption function
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public String toString() {
		return ""+value;
	}
	
	//SIMPLE OPERATIONS
	
	public static CryptInt cryptAdd(CryptInt v1, CryptInt v2) {
		return new CryptInt(v1.getValue() + v2.getValue());
	}
	
	public static CryptInt cryptSub(CryptInt v1, CryptInt v2) {
		return new CryptInt(v1.getValue() - v2.getValue());
	}
	
	public static CryptInt cryptMult(CryptInt v1, CryptInt v2) {
		return new CryptInt(v1.getValue() * v2.getValue());
	}
	
	public static CryptInt cryptDiv(CryptInt v1, CryptInt v2) {
		return new CryptInt(v1.getValue() / v2.getValue());
	}
	
	public static boolean cryptEq(CryptInt v1, CryptInt v2) {
		return (v1.getValue() == v2.getValue());
	}
	
	//ADVANCED OPERATIONS
	
	//Used in encodedComparison, to determine entailment
	//Java auto-rounds down with integer divisions. This is not needed
	/*public static CryptInt cryptFloor(CryptInt v1) {
		return new CryptInt((int) Math.floor(v1.getValue()));
	}*/
	
	//Used to evaluate conditionals
	public static boolean cryptLess(CryptInt v1, CryptInt limit) {
		while (!cryptEq(limit,new CryptInt(0))) {
			limit = cryptSub(limit,new CryptInt(1));
			if (cryptEq(limit, v1)) {
				return true;
			}
		}
		return false;
	}
	
	//FLOAT OPERATIONS
	//Assume these floats are also encrypted in the same way, but allowed to store real numbers
	
	public static CryptFloat cryptDivFloat(CryptInt v1, CryptFloat v2) {
		return new CryptFloat(v1.getValue() / v2.getValue());
	}
	
	public static boolean cryptEqFloat(CryptInt v1, CryptFloat v2) {
		return (v1.getValue() == v2.getValue());
	}
}
