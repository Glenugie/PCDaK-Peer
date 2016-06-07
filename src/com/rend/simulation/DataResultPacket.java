package com.rend.simulation;

import java.util.HashMap;
import java.util.LinkedList;

public class DataResultPacket {
	private HashMap<CryptInt,CryptInt> records;	
	private LinkedList<Integer> data;
	
	public DataResultPacket() {
		records = new HashMap<CryptInt,CryptInt>();
		data = new LinkedList<Integer>();
		
		setRecords(new HashMap<CryptInt,CryptInt>());
		setData(new LinkedList<Integer>());
	}

	public HashMap<CryptInt,CryptInt> getRecords() {
		return records;
	}

	public void setRecords(HashMap<CryptInt,CryptInt> records) {
		this.records.clear();
		this.records.putAll(records);
	}

	public LinkedList<Integer> getData() {
		return data;
	}

	public void setData(LinkedList<Integer> data) {
		this.data.clear();
		this.data.addAll(data);
	}
}
