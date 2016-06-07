package com.rend.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;

public class PeerSimulation {
	private final int bandSize = 2; //The size of each band in the numerical encoding setup (could be calculated dynamically)
	private final int roundLimit = 100;
	private int round = 1;
	
	private int peerID;
	private HashMap<Integer,Neighbour> neighbours;
	private LinkedHashMap<Integer,String> encodingTable;
	
	public HashMap<Integer,Integer> dataWanted;	
	public HashMap<CryptInt,CryptInt> records;
	
	private int publicKey;
	private int privateKey;
	private int nKey;

	//Metrics
	private int messagesSent = 0;
	private ArrayList<Double> messageSizes = new ArrayList<Double>();
	
	public PeerSimulation() {	
    	long totalStartTime = System.currentTimeMillis();	
		initPeer();
		printStatus();
		
		while (dataWanted.size() > 0 && round < roundLimit) {
	    	long roundStartTime = System.currentTimeMillis();	
			HashSet<Integer> toRemove = new HashSet<Integer>();
			for (Integer d : dataWanted.keySet()) {
				for (Integer nID : neighbours.keySet()) {
					Neighbour n = neighbours.get(nID); 
					
					//Send a request for d to n
					DataResultPacket resultPacket = requestRecords(d,dataWanted.get(d),nID);
					HashMap<CryptInt,CryptInt> resultSet = resultPacket.getRecords();
					
					if (resultSet.size() > 0) {
						for (CryptInt dID : resultSet.keySet()) {
							System.out.println("Received "+resultSet.get(dID)+" x "+dID+" Records from "+n);
							
							//Add new transaction records to records (and compact)
							if (!records.containsKey(dID)) { 
								records.put(dID, resultSet.get(dID));
							} else {
								records.replace(dID, CryptInt.cryptAdd(records.get(dID),resultSet.get(dID)));
							}

							//Determine the appropriate key, if it exists
							int key = -1;
							for (int k : dataWanted.keySet()) {
								if (CryptInt.cryptEq(new CryptInt(k), dID)) {
									key = k;
									break;
								}
							}

							//Subtract the returned records from data wanted. If 0, add to toRemove
							if (key != -1) {
								int resultQuantity = 0;
								for (int r : resultPacket.getData()) {
									if (r == key) {
										resultQuantity += 1;
									}
								}
								dataWanted.replace(key, dataWanted.get(key)-resultQuantity);
								if (dataWanted.get(key) <= 0) { toRemove.add(key);}
							}
						}
					}
					if (toRemove.contains(d)) { 
						System.out.println("Enough "+d+" received, skipping remaining neighbours");
						break;
					}
				}
				System.out.println("");
			}
			
			for (Integer tR : toRemove) { dataWanted.remove(tR);}

			System.out.println("Round Time: "+(System.currentTimeMillis()-roundStartTime)+" ms");
			round += 1;
			printStatus();
		}
		
		if (round >= roundLimit && dataWanted.size() > 0) {
			System.out.println("ROUND LIMIT EXCEEDED, NOT ALL WANTED DATA RECEIVED");
		}
		
		System.out.println("\n\n===============Simulation Statistics===============");
		System.out.println("Messages Sent: "+messagesSent);
		double min = -1, avg = 0.0, max = -1;
		for (double mS : messageSizes) {
			if (min == -1 || mS < min) { min = mS;}
			if (max == -1 || mS > max) { max = mS;}
			avg += mS;
		}
		avg /= messagesSent;
		System.out.println("Min Message Size: "+min);
		System.out.println("Avg Message Size: "+avg);
		System.out.println("Max Message Size: "+max);
		System.out.println("Simulation Time: "+(System.currentTimeMillis()-totalStartTime)+" ms");
	}
	
	private void initPeer() {
		peerID = 1;
		
		//Create Neighbours
		neighbours = new HashMap<Integer,Neighbour>();
		Neighbour newNeighbour = null;
		
		newNeighbour = new Neighbour(2); 
			newNeighbour.addRecord(10301, 20); 
			newNeighbour.addPolicy(3, 10301, 5, -1, -1);
		neighbours.put(newNeighbour.getID(),newNeighbour);
		
		newNeighbour = new Neighbour(3);
			newNeighbour.addRecord(20000, 50); 
			newNeighbour.addPolicy(2, 20000, -1, -1, -1);
		neighbours.put(newNeighbour.getID(),newNeighbour);
		
		newNeighbour = new Neighbour(4); 
			newNeighbour.addRecord(20000, 15); 
			newNeighbour.addPolicy(-1, 20000, -1, 10300, 1);
		neighbours.put(newNeighbour.getID(),newNeighbour);
		
		newNeighbour = new Neighbour(5); 
			newNeighbour.addRecord(30000, 5);
			newNeighbour.addPolicy(1, 30000, 5, 30000, 5);
		neighbours.put(newNeighbour.getID(),newNeighbour);
		
		//Populate Encoding Table
		encodingTable = new LinkedHashMap<Integer,String>();
		encodingTable.put(10000,"Prescriptions");
		encodingTable.put(10100,"Name");
		encodingTable.put(10200,"Drugs");
		encodingTable.put(10300,"Patient Notes");
		encodingTable.put(10301,"Other Medications");
		encodingTable.put(10302,"Other Conditions");
		encodingTable.put(10400,"Renewal Date");
		encodingTable.put(20000,"DrugX");
		encodingTable.put(20100,"Trial Number");
		encodingTable.put(20200,"Patient Notes");
		encodingTable.put(20201,"Other Medications");
		encodingTable.put(20202,"Other Conditions");
		encodingTable.put(20300,"Recorded Side-effects");
		encodingTable.put(20400,"Treatment Effectiveness");
		encodingTable.put(30000,"Vehicles");
		encodingTable.put(30100,"Motorcycles");
		encodingTable.put(30101,"Owner");
		encodingTable.put(30102,"Brand");
		encodingTable.put(30103,"Horsepower");
		encodingTable.put(30200,"Cars");
		encodingTable.put(30201,"Owner");
		encodingTable.put(30202,"Brand");
		encodingTable.put(30203,"Horsepower");
		printComparisons();
		
		
		//Populate Data Wanted
		dataWanted = new HashMap<Integer,Integer>();
		dataWanted.put(20100, 10);
		dataWanted.put(20201, 10);
		dataWanted.put(20202, 10);
		dataWanted.put(20300, 10);
		dataWanted.put(20400, 10);
		
		records = new HashMap<CryptInt,CryptInt>();
		
		generateKeyPair();
	}
	
	private DataResultPacket requestRecords(int recordType, int recordQuantity, int peer) {
		System.out.println("Requesting "+recordType+" x "+recordQuantity+" Records from "+peer);
		DataResultPacket result = new DataResultPacket();
		HashMap<CryptInt,CryptInt> resultRecords = new HashMap<CryptInt,CryptInt>();
		LinkedList<Integer> resultData = new LinkedList<Integer>();

		//Data Request from A to B
		Neighbour peerB = neighbours.get(peer);
		messagesSent += 1;
		messageSizes.add(1.0); //Message Contains a single number
		
		//B Checks if A's identity prevents sending data (ID policies)
		ArrayList<Policy> relPolicies = new ArrayList<Policy>();
		System.out.println("\tFinding Relevant Policies...");
		for (Policy p : peerB.getPolicies()) {
			if (p.getPeerID() == peerID || p.getPeerID() == -1) {
				if (p.getDataPermit() == -1 || twinEncodedComparison(p.getDataPermit(),recordType)) {
					relPolicies.add(p);
					System.out.println("\t\t"+p);
				}
			}
		}
		if (relPolicies.size() == 0) { System.out.println("\t\tNONE FOUND");}
		
		//B sends relevant policies to A
		messagesSent += 1;
		messageSizes.add(((double) relPolicies.size()*5)+1); //Message contains a number of policies (5 numbers per policy, +1 is the array overhead)
		
		if (relPolicies.size() > 0) { //If this is false, peer doesn't have access to data	
			//A determines which transaction records are relevant, sends to B
			HashSet<Integer> uniqueElements = new HashSet<Integer>();
			for (Policy p : relPolicies) {
				uniqueElements.add(p.getDataCondition());
				uniqueElements.add(p.getDataPermit());
			}

			System.out.println("\tFinding Relevant Records...");
			HashMap<CryptInt,CryptInt> relRecords = new HashMap<CryptInt,CryptInt>();
			for (CryptInt r : records.keySet()) {
				boolean found = false;
				for (int e : uniqueElements) {
					if (twinEncodedComparison(r,new CryptInt(e))) {
						found = true;
						break;
					}
				}

				if (found) {
					System.out.println("\t\tRecord <"+r+", "+records.get(r)+"> is relevant");
					relRecords.put(r, records.get(r));
				}
			}

			if (relRecords.size() == 0) { System.out.println("\t\tNONE FOUND");}
			messagesSent += 1;
			messageSizes.add(((double) relRecords.size()*4)+1); //Message contains a number of records (2 encrypted numbers per record), +1 is the array overhead)
			/*
			 * Multiple policies could fire (referring to subparts of the requested data)
			 * So need to fire them sequentially (Pseudocode to follow):
			 * For each policy
			 * 	Does policy refer to any of the data requested
			 * 	If so, does the policy fire
			 * 	If so, add a temporary transaction record (for the data that this policy allows) to relRecords, as well as the full data the resultSet
			 * 		Then fire the next policy in the list
			 * 		Sequential policies cannot affect data which has already been allowed, only add more
			 */
			//B checks if records violate policies			
			System.out.println("\tChecking Records against Policies...");
			for (Policy p : relPolicies) { //Assume peers hold copies of relPolicies until transaction completion (storage vs processing)
				boolean fired = false;
				if (p.getDataCondition() != -1) {
					//Does the policy fire?						
					CryptInt matchingRecords = new CryptInt(0);
					for (CryptInt r : relRecords.keySet()) {
						if (twinEncodedComparison(r,new CryptInt(p.getDataCondition()))) {
							CryptInt.cryptAdd(matchingRecords, relRecords.get(r));
						}
					}
					
					if (CryptInt.cryptLess(matchingRecords, new CryptInt(p.getDataConditionQuantity()))) { fired = true;}
					//System.out.println("\t\t\t"+matchingRecords.getValue()+" < "+p.getDataConditionQuantity()+" = "+fired);
				} else {
					fired = true;
				}
				System.out.println("\t\t"+p+", PERMIT "+fired);
					
				//If policy fires, add a temporary transaction record to relRecords, and add data to resultSet
				if (fired) {
					int quantity = p.getDataPermitQuantity(); 
					//if (relRecords.containsKey(recordType)) { quantity -= relRecords.get(recordType);}
					if (quantity > recordQuantity || p.getDataPermitQuantity() == -1) { quantity = recordQuantity;}
					
					if (quantity > 0) {
						if (!relRecords.containsKey(recordType)) { 
							relRecords.put(new CryptInt(recordType),new CryptInt(quantity));
						} else {
							relRecords.replace(new CryptInt(recordType), CryptInt.cryptAdd(relRecords.get(recordType),new CryptInt(quantity)));
						}
						
						if (!resultRecords.containsKey(recordType)) { 
							resultRecords.put(new CryptInt(recordType),new CryptInt(quantity));
						} else {
							resultRecords.replace(new CryptInt(recordType), CryptInt.cryptAdd(resultRecords.get(recordType),new CryptInt(quantity)));
						}
						
						for (int i = 0; i < quantity; i += 1) {
							resultData.add(recordType);
						}
					}
					System.out.println("\t\t\tAdding "+quantity+" records of "+recordType+" to Result Set");
				}
			}
			
			if (resultRecords.size() > 0) {
				result.setRecords(resultRecords);
				result.setData(resultData);
				
				//Send data to A (encrypted with A's public key: publicKey)				
				//A decrypts package (using own A's private key: privateKey)
				messagesSent += 1;
				messageSizes.add((double) (resultRecords.size()*4)+resultData.size()+2); //Two arrays, one of records (two encrypted numbers) and one of data (one number), +2 for array overheads
			}
		}
		
		return result;
	}
	
	/*
	 *  n cannot be encrypted, unless we can calculate zero bands for an encrypted integer.
	 *  Is it possible to calculate zero bands using only +,-,*,/?
	 *  Currently:
	 *  WHILE ((DN % (10^bandSize)) == 0)
	 *		DN /= (10^bandSize)     FINE
	 *		ZB += 1                 FINE
	 *
	 *	New:
	 *	WHILE ((DN / ((int) (10^bandSize))) == (DN / ((float) (10^bandSize)))) 
	 */
	
	//For two encrypted integers (if only one, encrypt other)
	public boolean twinEncodedComparison(CryptInt n, CryptInt dn) {
		boolean r1 = encodedComparison(n,dn), r2 = encodedComparison(dn,n);
		return (r1 || r2);
	}	
	public boolean encodedComparison(CryptInt n, CryptInt dn) { //n can be encrypted
		boolean result = false;
		
		int zeroBands = 0, factor = ((int) Math.pow(10, bandSize));
		CryptInt tempDN = dn;
		
		/* 
		OLD Band Size Loop (Pseudocode and Code)
		WHILE ((DN % (10^bandSize)) == 0)
		while ((tempDN % factor) == 0) {
		*/
		
		//WHILE ((DN / ((int) (10^bandSize))) == (DN / ((float) (10^bandSize)))) 
		while (CryptInt.cryptEqFloat( CryptInt.cryptDiv(tempDN,new CryptInt(factor)), CryptInt.cryptDivFloat(tempDN,new CryptFloat(factor)))) {
			tempDN = CryptInt.cryptDiv(tempDN,new CryptInt(factor));
			zeroBands += 1;
		}

		CryptInt divFactor = new CryptInt((int) Math.pow(10, (bandSize*zeroBands)));
		CryptInt nCalc = CryptInt.cryptDiv(n, divFactor);
		CryptInt dnCalc = CryptInt.cryptDiv(dn, divFactor);
		
		//Both nCalc and dnCalc must be encrypted by this point (current implementation forces encryption for the entire calculation)
		result = CryptInt.cryptEq(nCalc, dnCalc);
		
		return result;
	}

	//For two unencrypted integers (these operations will likely be the same as above in final implementation. Current pseudo-encryption requires these wrappers
	public boolean twinEncodedComparison(int n, int dn) { return twinEncodedComparison(new CryptInt(n), new CryptInt(dn));}	
	public boolean encodedComparison(int n, int dn) { return encodedComparison(new CryptInt(n), new CryptInt(dn));}
	
	private void printComparisons() {
		System.out.print("L \\ R|\t"); for (int k : encodingTable.keySet()) { System.out.print(k+"|\t");} System.out.println("");
		for (int k : encodingTable.keySet()) { 
			System.out.print(k+"|\t");
			for (int k2 : encodingTable.keySet()) { 
				String result = " "; if (encodedComparison(k,k2)) { result = "t";}
				String result2 = " "; if (twinEncodedComparison(k,k2)) { result2 = "t";}
				System.out.print(result+"/"+result2+"  |\t");
			}
			System.out.println("");
		}
		System.out.println("Single Comparison (L -> R) / Twin Comparison (L -> R || R -> L)");
	}
	
	private void printStatus() {
		System.out.println("\n===============ROUND "+round+"===============");
		if (dataWanted.size() > 0) {
			System.out.println("Data Wanted:");
			for (int d : dataWanted.keySet()) {
				System.out.println("\t"+d+" ("+dataWanted.get(d)+")");
			}
		} else {
			System.out.println("ALL DATA RECEIVED. ENDING SIMULATION.");
		}
		System.out.println("");
	}
	
	//Generates a (fixed) public and private RSA keypair for Peer 1
	private void generateKeyPair() {
		int p = 17, q = 29;
		int n = p*q;
		int totient = (p-1)*(q-1);
		int e = 3;
		int d = 299; //299 = (1+2x448)/3
				
		nKey = n;
		publicKey = e;
		privateKey = d;
	}
}
