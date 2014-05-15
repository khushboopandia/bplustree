package edu.khush.db.bplus.tree;

import java.util.ArrayList;

public class Node {
	protected boolean isLeafNode;
	protected Node parentNode;
	protected ArrayList<Integer> keys;

	public boolean isOverflowed() {
		return keys.size() > 2 * BPlusTree.D;
	}

	public boolean isUnderflowed() {
		return keys.size() < BPlusTree.D;
	}

}
