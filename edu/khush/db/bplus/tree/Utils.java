package edu.khush.db.bplus.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class contains methods assisting coding and testing
 * 
 */
public class Utils {

	/**
	 * Bulk Insert test data
	 * 
	 * @param b
	 * @param tests
	 */
	public static void bulkInsert(BPlusTree b, int[] tests) {
		for (int i = 0; i < tests.length; i++) {
			b.insert(tests[i], String.valueOf(tests[i]));
		}

	}

	/**
	 * print the current tree to console
	 * 
	 * @param root
	 */
	public static void printTree(BPlusTree tree) {
		/* Temporary queue. */
		LinkedBlockingQueue<Node> queue;

		/* Create a queue to hold node pointers. */
		queue = new LinkedBlockingQueue<Node>();
		String result = "";

		int nodesInCurrentLevel = 1;
		int nodesInNextLevel = 0;
		ArrayList<Integer> childrenPerIndex = new ArrayList<Integer>();
		queue.add(tree.root);
		while (!queue.isEmpty()) {
			Node target = queue.poll();
			nodesInCurrentLevel--;
			if (target.isLeafNode) {
				LeafNode leaf = (LeafNode) target;
				result += "[";
				for (int i = 0; i < leaf.keys.size(); i++) {
					result += "(" + leaf.keys.get(i) + " , "
							+ leaf.values.get(i) + ");";
				}
				childrenPerIndex.set(0, childrenPerIndex.get(0) - 1);
				if (childrenPerIndex.get(0) == 0) {
					result += "] $ ";
					childrenPerIndex.remove(0);
				} else {
					result += "] # ";
				}
			} else {
				IndexNode index = ((IndexNode) target);
				result += "@ ";
				for (int i = 0; i < index.keys.size(); i++) {
					result += "" + index.keys.get(i) + "/";
				}
				result += "@   ";
				//System.out.println(index.isLeafNode);
				//System.out.println(index.children.size());
				queue.addAll(index.children);
				if (index.children.get(0).isLeafNode) {
					childrenPerIndex.add(index.children.size());
				}
				nodesInNextLevel += index.children.size();
			}

			if (nodesInCurrentLevel == 0) {
				result += "\n";
				nodesInCurrentLevel = nodesInNextLevel;
				nodesInNextLevel = 0;
			}

		}
		System.out.println(result);

	}
	
	public static void main(String[] args) throws Exception{
		
		bulkTest();
		if(false){
		BPlusTree bplustree=new BPlusTree();
		Integer[] input= {2,3,5,6,7,8,14,15,16,22,24,27,29,33,34};
		for(int i=0;i<input.length;i++){
			bplustree.insert(input[i] , "Khush");
		}

		printTree(bplustree);
		bplustree.delete(2);
		printTree(bplustree);
		}
	}
	
	public static void bulkTest() throws Exception{
		BPlusTree btree=new BPlusTree();		
		ArrayList<Integer> testKeys = new ArrayList<Integer>();
		ArrayList<String> testValues = new ArrayList<String>();
		for(int i=1;i<=1000;i++) testKeys.add(i);
		for(int i=1;i<=1000;i++) {
			String s = "" + i;
			testValues.add(s);
		}
		Collections.shuffle(testKeys);
		Collections.shuffle(testValues);
		for(int i=0;i<1000;i++) btree.insert(testKeys.get(i), testValues.get(i));
		Utils.printTree(btree);
		Collections.shuffle(testKeys);
		for(Integer in: testKeys) {
			System.out.println("Deleting: "+in.toString());
			System.out.println(in);
			btree.delete(in);
			//Utils.printTree(btree);

		}
		System.out.println("Deleted everything");
		Utils.printTree(btree);
	}

}
