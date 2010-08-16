package org.scale7.networking.clustering;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClusterNodesHash<T> {
	
	private final HashCalculator hasher;
	private final int replicationFactor;
	private volatile SortedMap<Long, T> ring;
	private volatile HashMap<String, T> nodeMap;
	
	/**
	 * Create a cluster nodes hash. This maps objects to cluster nodes using consistent hashing. 
	 * @param replicationFactor The number of "virtual nodes" used to represent a node on the consistent hashing ring
	 */	
	public ClusterNodesHash(int replicationFactor) {
		this(new HashCalculator(), replicationFactor, new ArrayList<T>());
	}
	
	/**
	 * Create a cluster nodes hash. This maps objects to cluster nodes using consistent hashing. 
	 * @param replicationFactor The number of "virtual nodes" used to represent a node on the consistent hashing ring
	 * @param clusterNodes The cluster nodes to add to the consistent hashing ring
	 */	
	public ClusterNodesHash(int replicationFactor, Collection<T> clusterNodes) {
		this(new HashCalculator(), replicationFactor, clusterNodes);
	}	
	
	/**
	 * Create a cluster nodes hash. This maps objects to cluster nodes using consistent hashing. 
	 * @param hasher A HashCalculator object encapsulating the hash algorithm to use when calculating the hash for an id
	 * @param replicationFactor The number of "virtual nodes" used to represent a node on the consistent hashing ring
	 * @param clusterNodes The cluster nodes to add to the consistent hashing ring
	 */
	public ClusterNodesHash(HashCalculator hasher, int replicationFactor, Collection<T> clusterNodes) {
		this.hasher = hasher;
		this.replicationFactor = replicationFactor;
		setMembers(clusterNodes);
	}		
	
	/**
	 * Set the nodes that comprise the cluster. This is performed as a single operation. 
	 * @param clusterNodes The nodes that comprise the cluster
	 * @return A set containing the nodes objects that were previously in the cluster, but which have been removed
	 */
	public Set<T> setMembers(Collection<T> clusterNodes) {
		SortedMap<Long, T> newRing = new TreeMap<Long, T>();
		HashMap<String, T> newNodeMap = new HashMap<String, T>();

		for (T node : clusterNodes) {
			addNode(newRing, newNodeMap, node, replicationFactor);
		}	

		HashSet<T> removedNodes;
		if (ring == null) {
			removedNodes = new HashSet<T>();
		} else {
			removedNodes = new HashSet<T>(ring.values());
			removedNodes.removeAll(newRing.values());
		}
		
		ring = newRing;
		nodeMap = newNodeMap;
		
		return removedNodes;
	}
	
	/**
	 * Get the node in the cluster by its id. The id of a node is returned by its <code>toString()</code> method.
	 * @param nodeId The id of the node in the cluster
	 * @return The cluster node with the specified id. <code>null</code> is returned if the node is not present 
	 */
	public T getNodeById(String nodeId) {
		AbstractMap<String, T> currNodeMap = nodeMap;
		return currNodeMap.get(nodeId);
	}
		
	/**
	 * Get the node in the cluster that is responsible for object with the provided id. 
	 * @param objectId The id of the object for which the responsible cluster node is being sought
	 * @return The cluster node responsible for the specified object
	 */
	public T getNodeByResponsibility(String objectId) {
		SortedMap<Long, T> currRing = ring;
		
		if (currRing.isEmpty()) {
			return null;
		}
		long hash = hasher.hash(objectId);
		if (!currRing.containsKey(hash)) {
			SortedMap<Long, T> tailMap = currRing.tailMap(hash);
			hash = tailMap.isEmpty() ? currRing.firstKey() : tailMap.firstKey();
		}
		return currRing.get(hash);
	}

	private void addNode(SortedMap<Long, T> circle, AbstractMap<String, T> nodeMap, T node, int replicationFactor) {
		for (int i = 0; i < replicationFactor; i++) {
			circle.put(hasher.hash(node.toString() + i), node);
		}
		nodeMap.put(node.toString(), node);
	}
	
	public static class HashCalculator {
		MessageDigest md;
		
		public HashCalculator() {
			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				assert false;
			}
		}
		
		public HashCalculator(String hashName) throws NoSuchAlgorithmException {
			md = MessageDigest.getInstance(hashName);
		}
		
		long hash(String key) {
			md.reset();
			md.update(key.getBytes());
			byte[] digest = md.digest();
			
	        long h = 0;
	        for (int i = 0; i < 4; i++) {
	            h <<= 8;
	            h |= ((int) digest[i]) & 0xFF;
	        }
	        return h;			
		}
	};
}
