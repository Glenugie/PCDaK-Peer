package com.rend.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class PeerSimulation {
	private final int bandSize = 2; //The size of each band in the numerical encoding setup (could be calculated dynamically)
	private int round = 1;
	
	private int peerID;
	private HashMap<Integer,Neighbour> neighbours;
	private LinkedHashMap<Integer,String> encodingTable;
	
	public HashMap<Integer,Integer> dataWanted;	
	public HashMap<Integer,Integer> records;
	
	private int publicKey;
	private int privateKey;
	private int nKey;
	
	public PeerSimulation() {		
		initPeer();
		printStatus();
		
		while (dataWanted.size() > 0 && round < 10000) {
			HashSet<Integer> toRemove = new HashSet<Integer>();
			for (Integer d : dataWanted.keySet()) {
				for (Integer nID : neighbours.keySet()) {
					Neighbour n = neighbours.get(nID); 
					
					//Send a request for d to n
					HashMap<Integer,Integer> resultSet = requestRecords(d,dataWanted.get(d),nID);
					
					if (resultSet.size() > 0) {
						for (int dID : resultSet.keySet()) {
							System.out.println("Received "+resultSet.get(dID)+" x "+dID+" Records from "+n);
							
							//Add new transaction records to records (and compact)
							if (!records.containsKey(dID)) { 
								records.put(dID,resultSet.get(dID));
							} else {
								records.replace(dID, records.get(dID)+resultSet.get(dID));
							}

							//Subtract the returned records from data wanted. If 0, add to toRemove
							if (dataWanted.containsKey(dID)) {
								dataWanted.replace(dID,dataWanted.get(dID)-resultSet.get(dID));
								if (dataWanted.get(dID) <= 0) { toRemove.add(dID);}
							}
						}
					}
				}
			}
			
			for (Integer tR : toRemove) { dataWanted.remove(tR);}

			round += 1;
			printStatus();
		}
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
		
		records = new HashMap<Integer,Integer>();
		
		generateKeyPair();
	}
	
	private HashMap<Integer,Integer> requestRecords(int recordType, int recordQuantity, int peer) {
		System.out.println("Requesting "+recordType+" x "+recordQuantity+" Records from "+peer);
		//Data Request from A to B
		HashMap<Integer,Integer> resultSet = new HashMap<Integer,Integer>();		
		Neighbour peerB = neighbours.get(peer);
		
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
		
		//B sends relevant policies to A
		if (relPolicies.size() > 0) { //If this is false, peer doesn't have access to data	
			//A determines which transaction records are relevant, sends to B
			HashSet<Integer> uniqueElements = new HashSet<Integer>();
			for (Policy p : relPolicies) {
				uniqueElements.add(p.getDataCondition());
				uniqueElements.add(p.getDataPermit());
			}

			System.out.println("\tFinding Relevant Records...");
			HashMap<Integer,Integer> relRecords = new HashMap<Integer,Integer>();
			for (int r : records.keySet()) {
				boolean found = false;
				for (int e : uniqueElements) {
					if (twinEncodedComparison(r,e)) {
						found = true;
						break;
					}
				}

				if (found) {
					System.out.println("\t\tRecord <"+r+", "+records.get(r)+"> is relevant");
					relRecords.put(r, records.get(r));
				}
			}
			
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
			System.out.println("\tChecking Records against Policies");
			for (Policy p : relPolicies) { //Assume peers hold copies of relPolicies until transaction completion (storage vs processing)
				boolean fired = false;
				if (p.getDataCondition() != -1) {
					//Does the policy fire?						
					int matchingRecords = 0;
					for (int r : relRecords.keySet()) {
						if (twinEncodedComparison(r,p.getDataCondition())) {
							matchingRecords += relRecords.get(r);
						}
					}
					
					if (matchingRecords < p.getDataConditionQuantity()) { fired = true;}
				} else {
					fired = true;
				}
				System.out.println("\t\t"+p+" Fired: "+fired);
					
				//If policy fires, add a temporary transaction record to relRecords, and add data to resultSet
				if (fired) {
					int quantity = p.getDataPermitQuantity(); 
					if (relRecords.containsKey(recordType)) { quantity -= relRecords.get(recordType);}
					if (quantity > recordQuantity || p.getDataPermitQuantity() == -1) { quantity = recordQuantity;}
					
					if (quantity > 0) {
						if (!relRecords.containsKey(recordType)) { 
							relRecords.put(recordType,quantity);
						} else {
							relRecords.replace(recordType, relRecords.get(recordType)+quantity);
						}
						
						if (!resultSet.containsKey(recordType)) { 
							resultSet.put(recordType,quantity);
						} else {
							resultSet.replace(recordType, resultSet.get(recordType)+quantity);
						}
					}
					System.out.println("\t\t\tAdding "+quantity+" records of "+recordType+" to Result Set");
				}
			}
			
			if (resultSet.size() > 0) {
				//Send data to A (encrypted with A's public key)				
				//A decrypts package (using own Private Key)
			}
		}
		
		return resultSet;
	}
	
	/*
	 *  n cannot be encrypted, unless we can calculate zero bands for an encrypted integer?
	 *  Is it possible to calculate zero bands using only +,-,*,/?
	 *  Currently:
	 *  WHILE ((DN % (10^bandSize)) == 0)
	 *		DN /= (10^bandSize)     FINE
	 *		ZB += 1                 FINE
	 *	WHILE ((DN / ((int) (10^bandSize))) == (DN / ((float) (10^bandSize)))) 
	 */
	public boolean twinEncodedComparison(int n, int dn) {
		boolean r1 = encodedComparison(n,dn), r2 = encodedComparison(dn,n);
		return (r1 || r2);
	}
	
	public boolean encodedComparison(int n, int dn) { //n can be encrypted
		boolean result = false;
		
		int zeroBands = 0, tempDN = dn, factor = ((int) Math.pow(10, bandSize));
		//WHILE ((DN % (10^bandSize)) == 0)
		//while ((tempDN % factor) == 0) {
		//WHILE ((DN / ((int) (10^bandSize))) == (DN / ((float) (10^bandSize)))) 
		while ((tempDN / factor) == (tempDN / ((float) factor))) {
			tempDN = tempDN/factor;
			zeroBands += 1;
		}

		int nCalc = (int) Math.floor(n/Math.pow(10, (bandSize*zeroBands)));
		int dnCalc = (int) Math.floor(dn/Math.pow(10, (bandSize*zeroBands)));
		//Both nCalc and dnCalc should be encrypted at this point (if not already)
		result = (nCalc == dnCalc);
		
		return result;
	}
	
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
		
		/*System.out.println("");
		System.out.print("L \\ R|\t"); for (int k : encodingTable.keySet()) { System.out.print(k+"|\t");} System.out.println("");
		for (int k : encodingTable.keySet()) { 
			System.out.print(k+"|\t");
			for (int k2 : encodingTable.keySet()) { 
				String result = " "; if (twinEncodedComparison(k,k2)) { result = "t";}
				System.out.print("  "+result+"  |\t");
			}
			System.out.println("");
		}*/
	}
	
	private void printStatus() {
		System.out.println("\n\n=====ROUND "+round+"=====");
		if (dataWanted.size() > 0) {
			System.out.println("Data Wanted:");
			for (int d : dataWanted.keySet()) {
				System.out.println("\t"+d+" ("+dataWanted.get(d)+")");
			}
		} else {
			System.out.println("All Data Possessed");
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
