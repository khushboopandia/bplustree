package edu.khush.db.bplus.tree;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;



/**
 * BPlusTree Class Assumptions: 1. No duplicate keys inserted 2. Order D:
 * D<=number of keys in a node <=2*D 3. All keys are non-negative
 */
@SuppressWarnings("static-access")
public class BPlusTree {

	public Node root;
	public static final int D = 2;

	/**
	 *  Search the value for a specific key
	 * 
	 * @param key
	 * @return value
	 */
	public String search(int key) {

		//We start at the root node
		Node current = root;

		
		//If root node is leaf node instance search in it directly
		//else call recursiveSearch to get the leaf node in
		//the key may exist
		if (root instanceof IndexNode)
			current = recursiveSearch(key, (IndexNode) current);
		if (current == null)
			return "Search key not found";
		
		else {

			//Linearly search the leaf node
			//to find the index of the key 
			int index = 0;
			for (Integer nodeKey : current.keys) {

				if (key == nodeKey.intValue())
					break;
				else {
					index++;
				}
			}

			
			if (index > current.keys.size() - 1)
				return "Search key not found";
			else {
				//Return the value corresponding to
				//the index of the key being searched
				return ((LeafNode) current).values.get(index);
			}
		}

	}
	
	
	/**
	 *  Search the leaf node
	 *  for a specific key
	 * 
	 * @param key,current
	 * @return value
	 */
	public Node recursiveSearch(int key, IndexNode current) {

		LeafNode leafNodeOfKey = null;
		int index = 0;
		for (Integer nodeKey : current.keys) {

			if (key < nodeKey.intValue()) {

				System.out.println(nodeKey.intValue());
				break;
			} else {
				index++;
			}
		}

		if (current.children.get(index) instanceof IndexNode)
			leafNodeOfKey = (LeafNode) recursiveSearch(key,
					(IndexNode) current.children.get(index));
		else {

			leafNodeOfKey = (LeafNode) current.children.get(index);

		}

		return leafNodeOfKey;

	}
	
	

	/**
	 * Insert a key/value pair into the BPlusTree
	 * 
	 * @param key
	 * @param value
	 */
	public void insert(int key, String value) {

		// Check if the root node is null
		if (root == null) {
			System.out.println("Root is null");
			LeafNode l = new LeafNode(key, value);
			root = l;

		}
		//insert in the b+ tree
		else {

			insertInBPlusTree(root, key, value);
		}

	}

	
	/**
	 * Split a leaf node and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param leaf
	 * @return the key/node pair as an Entry
	 */
	public Entry<Integer, Node> splitLeafNode(LeafNode leaf) {

		int splitIndex = this.D;
		Integer splittingKey = leaf.keys.get(splitIndex);
		ArrayList<Integer> rightNodeKeys = new ArrayList<Integer>();
		ArrayList<String> rightNodeValues = new ArrayList<String>();

		for (int i = splitIndex; i <= 2 * this.D; i++) {

			rightNodeKeys.add(leaf.keys.get(i));
			rightNodeValues.add(leaf.values.get(i));

		}

		while (leaf.keys.size() != this.D) {
			leaf.keys.remove(leaf.keys.size() - 1);
			leaf.values.remove(leaf.values.size() - 1);
		}

		LeafNode rightNode = new LeafNode(rightNodeKeys, rightNodeValues);
		rightNode.parentNode = leaf.parentNode;
		rightNode.previousLeaf = leaf;
		rightNode.nextLeaf = leaf.nextLeaf;
		if (leaf.nextLeaf != null)
			leaf.nextLeaf.previousLeaf = rightNode;
		leaf.nextLeaf = rightNode;

		return new AbstractMap.SimpleEntry<Integer, Node>(splittingKey,
				rightNode);
	}
	
	

	/**
	 *  split an indexNode and return the new right node and the splitting
	 * key as an Entry<slitingKey, RightNode>
	 * 
	 * @param index
	 * @return new key/node pair as an Entry
	 */
	public Entry<Integer, Node> splitIndexNode(IndexNode index) {

		int splitIndexForKey = this.D;
		int splitIndexForChildren = this.D;

		Integer splittingKey = index.keys.get(splitIndexForKey);

		ArrayList<Integer> rightNodeKeys = new ArrayList<Integer>();
		ArrayList<Node> rightNodeChildren = new ArrayList<Node>();

		for (int i = splitIndexForKey + 1; i <= 2 * this.D; i++) {
			rightNodeKeys.add(index.keys.get(i));

		}

		for (int j = splitIndexForChildren + 1; j <= 2 * this.D + 1; j++) {
			rightNodeChildren.add(index.children.get(j));

		}

		while (index.keys.size() != this.D) {
			index.keys.remove(index.keys.size() - 1);
			index.children.remove(index.children.size() - 1);
		}

		IndexNode rightIndexNode = new IndexNode(rightNodeKeys,
				rightNodeChildren);
		rightIndexNode.parentNode = index.parentNode;

		return new AbstractMap.SimpleEntry<Integer, Node>(splittingKey,
				rightIndexNode);

	}
	
	
	/**
	 * Insert a given key,value in the b+ tree
	 * recursively.
	 */
	private void insertInBPlusTree(Node current, int key, String value) {

		//If the current node is an index node
		//we need to recurse further to get the leaf node
		//in which the key should be inserted
		
		if (current instanceof IndexNode) {

			IndexNode node = (IndexNode) current;

			int index = 0;
			for (Integer nodeKey : node.keys) {

				if (key < nodeKey.intValue())
					break;

				else {
					index++;
				}
			}
			insertInBPlusTree(node.children.get(index), key, value);

		}

		//If the current node is a leaf node
		//insert the key,value in it
		else {

			LeafNode leaf = (LeafNode) current;
			
			
			if (leaf.keys.size() < 2 * this.D) {

				leaf.insertSorted(key, value);
			} 
			//If the leaf node in which the key is inserted 
			//is overflown, handle the overflow condition
			//by splitting the node and pushing the splitkey
			//to parent index nodes
			else {
				leaf.insertSorted(key, value);
				Entry<Integer, Node> rightNodeEntry = splitLeafNode(leaf);
				trickleUp(rightNodeEntry);

			}

		}

	}

	
	/**
	 * Recursively trickle up the split key when an overflown leaf
	 * or index node is split.
	 */
	private void trickleUp(Entry<Integer, Node> rightNodeEntry) {

		IndexNode parent = (IndexNode) rightNodeEntry.getValue().parentNode;

		//If the parent of the split node is null
		//it implies that the root node was split.
		//Therefore, create a new index node and set it to be the root
		
		if (parent == null) {
			IndexNode newRootNode = new IndexNode(rightNodeEntry.getKey(),
					root, rightNodeEntry.getValue());
			root = newRootNode;
			return;
		}		
		
		Integer newKey = rightNodeEntry.getKey();

		//Get the index in the parent node
		//where the split key needs to be inserted
		int index = 0;
		for (Integer parentKey : parent.keys) {

			if (newKey.intValue() > parentKey.intValue())
				index++;
			else
				break;
		}

		//If the parent index node overflows after inserting split key
		//split the parent index node
		//and call itself to trickle up the new split key
		if (parent.keys.size() < 2 * this.D) {

			parent.insertSorted(rightNodeEntry, index);

		} else {

			parent.insertSorted(rightNodeEntry, index);
			Entry<Integer, Node> pushUpEntry = splitIndexNode(parent);
			trickleUp(pushUpEntry);

		}
	}
	
	

	/**
	 * Delete a key/value pair from this B+Tree
	 * 
	 * @param key
	 */
	public void delete(int key)  {

		Node current = root;

		//Get the leaf node in which the key
		//to be deleted should be 
		if (root instanceof IndexNode)
			current = recursiveSearch(key, (IndexNode) current);
		LeafNode leafNodeForKeyDeletion = (LeafNode) current;

		// Find index of the entry to get deleted
		int index = 0;
		for (Integer nodeKey : leafNodeForKeyDeletion.keys) {
			if (key == nodeKey.intValue())
				break;
			else {
				index++;
			}
		}

		// Insert check where the key to be deleted is absent
		if (index >= leafNodeForKeyDeletion.keys.size()) {
			System.out.println("The entry for deletion is absent");
			return;
		}

		// Delete the entry
		leafNodeForKeyDeletion.keys.remove(index);
		leafNodeForKeyDeletion.values.remove(index);

		// Check for leaf underflow
		if (root instanceof IndexNode
				&& leafNodeForKeyDeletion.keys.size() < this.D) {
			handleLeafNodeUnderflow(leafNodeForKeyDeletion.previousLeaf,
					leafNodeForKeyDeletion.nextLeaf,
					(IndexNode) leafNodeForKeyDeletion.parentNode);

			//After handling the leaf node underflow, check for
			//index node underflow  until all nodes upto root node have been
			//checked and handled for underflow
			IndexNode currentIndexNode = (IndexNode) leafNodeForKeyDeletion.parentNode;
			while (currentIndexNode != root
					&& currentIndexNode.keys.size() < this.D) {

				currentIndexNode = handleIndexNodeUnderflow((IndexNode) currentIndexNode);

			}

			//If the root index node has no keys
			//Set its only child node
			//as the new root
			if (root.keys.size() == 0) {
				root = ((IndexNode) root).children.get(0);
			}
		}
	}
	
	
	
	/**
	* Take the indexNode which has underflown
	* and handle it by merge or redistribution with 
	* left and right siblings
	* 
	*/
	
	private IndexNode handleIndexNodeUnderflow(IndexNode indexNode) {

		IndexNode current = indexNode;
		IndexNode parent = (IndexNode) current.parentNode;
		IndexNode leftSibling = null;
		IndexNode rightSibling = null;

		int indexOfCurrent = 0;
		int indexOfLeft = -1;
		int indexOfRight = -1;
		
		//Use the parent node to get the left and right
		//siblings of an index node
		//as they are do not have direct references to their siblings
		for (Node child : parent.children) {

			if (child == current) {
				indexOfLeft = indexOfCurrent - 1;
				indexOfRight = indexOfCurrent + 1;
			} else
				indexOfCurrent++;
		}

		if (indexOfLeft >= 0)
			leftSibling = (IndexNode) parent.children.get(indexOfLeft);
		if (indexOfRight < parent.children.size()) {
			rightSibling = (IndexNode) parent.children.get(indexOfRight);
		}

		
		//Check if can be merged with left sibling
		if (leftSibling != null
				&& leftSibling.keys.size() + current.keys.size() < 2 * this.D) {
			parent = mergeIndexNodes(leftSibling, current, parent);
		} 
		//Check if can be merged with right sibling
		else if (rightSibling != null
				&& rightSibling.keys.size() + current.keys.size() < 2 * this.D) {
			parent = mergeIndexNodes(current, rightSibling, parent);
		} 
		//Check if can be redistributed with left sibling
		else if (leftSibling != null
				&& leftSibling.keys.size() + current.keys.size() >= 2 * this.D) {
			parent = redistributeIndexNodes(leftSibling, current, parent, true);
		} 
		//Check if can be redistributed with right sibling
		else if (rightSibling != null
				&& rightSibling.keys.size() + current.keys.size() >= 2 * this.D) {
			parent = redistributeIndexNodes(current, rightSibling, parent,
					false);
		}

		return parent;

	}
	
	

	/**
	* Redistribute the index node from
	* left node to right node or 
	* from right to left.
	*
	*/	
	private IndexNode redistributeIndexNodes(IndexNode left, IndexNode right,
			IndexNode parent, boolean leftRedistribution) {

		int parentSplitKeyIndex = -1;
		Integer newValueInParent = -1;
		double leftKeysSize=left.keys.size();
		double rightKeysSize=right.keys.size();
		System.out.println(leftKeysSize);
		System.out.println(rightKeysSize);
		int totalsize=(int)Math.ceil((leftKeysSize+rightKeysSize)/2d);
		System.out.println("Total size: "+totalsize);
		

		//Redistributing from left sibling to underflown index node
		if (leftRedistribution) {

			Integer parentSplitKey = -1;
			
			//Find the parent index node split key
			for (int i = 0; i < parent.children.size(); i++) {
				if (parent.children.get(i) == left) {
					parentSplitKeyIndex = i;
				}
			}

			//Move the parent splitkey to the right node
			parentSplitKey = parent.keys.get(parentSplitKeyIndex);
			right.keys.add(0, parentSplitKey);

			int j = left.keys.size() - 1;
			//Move keys from left node to right node
			//till right node doesnt satisfies minm occupancy
			while (right.keys.size() != totalsize) {
				right.keys.add(0, left.keys.get(j));
				left.keys.remove(j);
				j = left.keys.size() - 1;
			}

			j = left.children.size() - 1;
			//Move children from left node to right node
			//till right node doesnt satisfies minm occupancy
			while (right.children.size() != totalsize + 1) {
				right.children.add(0, left.children.get(j));
				left.children.get(j).parentNode = right;
				left.children.remove(j);
				j = left.children.size() - 1;

			}

			//Replace the splitkey with the new split key value
			//in the parent node
			newValueInParent = left.keys.get(left.keys.size() - 1);
			parent.keys.remove(parentSplitKeyIndex);
			parent.keys.add(parentSplitKeyIndex, newValueInParent);
			left.keys.remove(left.keys.size() - 1);

		} 
		
		//Redistributing from right sibling to underflown index node
		else {

			//Find the parent index node split key
			Integer parentSplitKey = -1;
			for (int i = 0; i < parent.children.size(); i++) {
				if (parent.children.get(i) == right) {
					parentSplitKeyIndex = i - 1;
				}
			}

			//Move the parent splitkey to the left node
			parentSplitKey = parent.keys.get(parentSplitKeyIndex);
			left.keys.add(parentSplitKey);

			
			//Move keys from right node to left node
			//till left node doesnt satisfy minm occupancy
			int j = 0;
			while (left.keys.size() != this.D) {
				left.keys.add(right.keys.get(j));
				right.keys.remove(0);
			}

			
			//Move children from right node to left node
			//till left node doesnt satisfies minm occupancy
			j = 0;
			while (left.children.size() != this.D+1) {
				left.children.add(right.children.get(j));
				right.children.get(j).parentNode = left;
				right.children.remove(0);
			}

			//Replace the splitkey with the new split key value
			//in the parent node
			newValueInParent = right.keys.get(0);
			parent.keys.remove(parentSplitKeyIndex);
			parent.keys.add(parentSplitKeyIndex, newValueInParent);
			right.keys.remove(0);

		}

		return parent;

	}
	
	
	/**
	* Merge the index node left and right nodes	
	*/	
	private IndexNode mergeIndexNodes(IndexNode left, IndexNode right,
			IndexNode parent) {

		int parentSplitKeyIndex = -1;
		Integer parentSplitKey = null;
		int i = 1;
		//Find the parent index node split key
		for (i = 1; i < parent.children.size(); i++) {
			if (parent.children.get(i) == right) {
				parentSplitKeyIndex = i - 1;
				break;
			}
		}

		//Move the parent splitkey to the left node
		//and remove it to from the parent
		parentSplitKey = parent.keys.get(parentSplitKeyIndex);
		parent.keys.remove(parentSplitKeyIndex);
		
		//Also remove the child refernece from the parent
		//as the right node (child reference)
		//will be discarded after the merge
		parent.children.remove(i);

		// Put the parent split key
		//in the left node
		left.keys.add(parentSplitKey);

		// Move right node keys to left node
		for (i = 0; i < right.keys.size(); i++) {
			left.keys.add(right.keys.get(i));
		}

		// Move right node children to left node
		for (i = 0; i < right.children.size(); i++) {
			left.children.add(right.children.get(i));
			right.children.get(i).parentNode = left;
		}

		//Left node is the new merged node
		//Right node is discarded
		right = null;
		return parent;

	}
	
	
	/**
	* Take the underflown leaf node's siblings 
	* and handle the underflow by merge or redistribution with 
	* left and right siblings
	*/
	public void handleLeafNodeUnderflow(LeafNode left, LeafNode right,
			IndexNode parent) {

		LeafNode currentNode;
		if (left != null)
			currentNode = left.nextLeaf;
		else
			currentNode = right.previousLeaf;

		LeafNode leftSibling = null;
		LeafNode rightSibling = null;

		if (left != null && left.parentNode.equals(currentNode.parentNode)) {
			leftSibling = left;
		}

		if (right != null && right.parentNode.equals(currentNode.parentNode)) {
			rightSibling = right;
		}


		//Check if can be merged with left sibling
		if (leftSibling != null
				&& currentNode.keys.size() + leftSibling.keys.size() < 2 * D) {
			mergeLeafNodes(leftSibling, currentNode, parent);

		}
		//Check if can be merged with right sibling
		else if (rightSibling != null
				&& currentNode.keys.size() + right.keys.size() < 2 * D) {
			mergeLeafNodes(currentNode, rightSibling, parent);

		}
		//Check if can be redistributed with left sibling
		else if (leftSibling != null
				&& currentNode.keys.size() + leftSibling.keys.size() >= 2 * D) {
			redistributeLeafNodes(leftSibling, currentNode, parent, true);
			// return -1;
		}
		//Check if can be merged with right sibling
		else if (rightSibling != null
				&& currentNode.keys.size() + rightSibling.keys.size() >= 2 * D) {
			redistributeLeafNodes(currentNode, rightSibling, parent, false);

		}
	}
	
	
	
	/**
	* Merge the leaf nodes left and right nodes	
	*/	
	private int mergeLeafNodes(LeafNode left, LeafNode right, IndexNode parent) {

		int parentSplitKeyIndex = -1;

		//Move all keys and values from right node to the left node 
		for (int i = 0; i < right.keys.size(); i++) {
			left.keys.add(right.keys.get(i));
			left.values.add(right.values.get(i));
		}

		//Find the parent index node split key
		int i = 1;
		for (i = 0; i < parent.children.size(); i++) {
			if (parent.children.get(i) == right) {
				parentSplitKeyIndex = i - 1;
				break;
			}
		}
		
		//Remove the split key and corresponding
		//child reference (right node) from the parent index node
		parent.keys.remove(parentSplitKeyIndex);
		parent.children.remove(i);

		
		left.nextLeaf = right.nextLeaf;
		if (right.nextLeaf != null)
			right.nextLeaf.previousLeaf = left;

		//The left node is the new merged node
		//The right node can be discarded
		right = null;
		return parentSplitKeyIndex;

	}

	private void redistributeLeafNodes(LeafNode left, LeafNode right,
			IndexNode parent, boolean leftRedistribution) {

		int parentSplitKeyIndex = -1;
		Integer newSplitKeyInParent = -1;
		int totalsize=(left.keys.size()+right.keys.size())/2;
		//Redistributing from left sibling to underflown leaf node
		if (leftRedistribution) {
			
			
			int j = left.keys.size() - 1;
			
			//Move keys and values from the left node 
			//to the right node 
			while (left.keys.size() != totalsize) {
				right.keys.add(0, left.keys.get(j));
				right.values.add(0, left.values.get(j));
				newSplitKeyInParent = left.keys.get(j);
				left.keys.remove(j);
				left.values.remove(j);

				j = left.keys.size() - 1;

			}

			//Find the parent index node split key
			for (int i = 0; i < parent.children.size(); i++) {
				if (parent.children.get(i) == left) {
					parentSplitKeyIndex = i;
				}

			}
			
			//Replace the split key in the parent node
			//with the new split key after the redistribution
			parent.keys.remove(parentSplitKeyIndex);
			parent.keys.add(parentSplitKeyIndex, newSplitKeyInParent);

		} 
		
		//Redistributing from right sibling to underflown index node
		else {
			
			//Move keys and values from the left node 
			//to the right node
			int j = 0;
			while (left.keys.size() != totalsize) {
				left.keys.add(right.keys.get(j));
				left.values.add(right.values.get(j));
				right.keys.remove(0);
				right.values.remove(0);
			}
			
			
			newSplitKeyInParent = right.keys.get(0);
			//Find the parent index node split key
			for (int i = 0; i < parent.children.size(); i++) {
				if (parent.children.get(i) == right) {
					parentSplitKeyIndex = i - 1;
				}

			}
			
			//Replace the split key in the parent node
			//with the new split key after the redistribution
			parent.keys.remove(parentSplitKeyIndex);
			parent.keys.add(parentSplitKeyIndex, newSplitKeyInParent);

		}

	}

}
