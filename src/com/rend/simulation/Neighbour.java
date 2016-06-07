package com.rend.simulation;

import java.util.HashMap;
import java.util.LinkedList;

public class Neighbour {
	private int peerID;
	private HashMap<Integer,Integer> records;
	private LinkedList<Policy> policies;
	
	public Neighbour(int peerID) {
		this.peerID = peerID;
		records = new HashMap<Integer,Integer>();
		policies = new LinkedList<Policy>();
	}
	
	public void addRecord(int record, int quantity) {
		if (!records.containsKey(record)) { records.put(record, 0);}
		records.replace(record, records.get(record)+quantity);
	}
	
	public void addPolicy(int peerID, int dataPermit, int dataPermitQuantity, int dataCondition, int dataConditionQuantity) {
		policies.add(new Policy(peerID, dataPermit, dataPermitQuantity, dataCondition, dataConditionQuantity));
	}
	
	public int getID() {
		return peerID;
	}
	
	public LinkedList<Policy> getPolicies() {
		return policies;
	}
	
	public String toString() {
		return "<Neighbour "+peerID+">";
	}
}
