package com.rend.simulation;

public class Policy {
	private int peerID; //Who the policy targets (-1 for all)
	private int dataPermit; //What data this policy refers to (-1 for all data)
	private int dataPermitQuantity; //How much of the above can be accessed (-1 for unlimited)
	private int dataCondition; //The data to be checked for as a condition of the above (-1 for empty)
	private int dataConditionQuantity; //The amount of the dataCondition which has to be exceeded to prevent access (-1 for empty)
	
	public Policy(int peerID, int dataPermit, int dataPermitQuantity, int dataCondition, int dataConditionQuantity) {
		this.setPeerID(peerID);
		this.setDataPermit(dataPermit);
		this.setDataPermitQuantity(dataPermitQuantity);
		this.setDataCondition(dataCondition);
		this.setDataConditionQuantity(dataConditionQuantity);
	}
	
	public int getPeerID() {
		return peerID;
	}
	
	public void setPeerID(int peerID) {
		this.peerID = peerID;
	}

	public int getDataPermit() {
		return dataPermit;
	}

	public void setDataPermit(int dataPermit) {
		this.dataPermit = dataPermit;
	}

	public int getDataPermitQuantity() {
		return dataPermitQuantity;
	}

	public void setDataPermitQuantity(int dataPermitQuantity) {
		this.dataPermitQuantity = dataPermitQuantity;
	}

	public int getDataCondition() {
		return dataCondition;
	}

	public void setDataCondition(int dataCondition) {
		this.dataCondition = dataCondition;
	}

	public int getDataConditionQuantity() {
		return dataConditionQuantity;
	}

	public void setDataConditionQuantity(int dataConditionQuantity) {
		this.dataConditionQuantity = dataConditionQuantity;
	}
	
	public String toString() {
		return "<"+getPeerID()+", <"+getDataPermit()+", "+getDataPermitQuantity()+">, <"+getDataCondition()+", "+getDataConditionQuantity()+">>";
	}
}
