package edu.khush.db.bplus.tree;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;



public class Tests {

	@Test
	public void testHybrid1() {
		int primeNumbers[] = new int[] { 2, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14,
				15, 16 };
		BPlusTree tree=new BPlusTree();
		Utils.bulkInsert(tree, primeNumbers);
		
		String test=outputTree(tree);
		
		String correct="@10/@%%@5/8/@@12/14/@%%[(2,2);(4,4);]#[(5,5);(7,7);]#[(8,8);(9,9);]$[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
		Utils.printTree(tree);
		assertEquals(test, correct);
		
		//System.out.println(tree.search(2));
		
	    tree.delete(2);
	    
	    test=outputTree(tree);
	    correct="@8/10/12/14/@%%[(4,4);(5,5);(7,7);]#[(8,8);(9,9);]#[(10,10);(11,11);]#[(12,12);(13,13);]#[(14,14);(15,15);(16,16);]$%%";
	    assertEquals(test, correct);
		
	}
	
	/**
	 * return the current tree to console in comparable format
	 * 
	 * @param root
	 */
	public static String outputTree(BPlusTree tree) {
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
					result += "(" + leaf.keys.get(i) + ","
							+ leaf.values.get(i) + ");";
				}
				childrenPerIndex.set(0, childrenPerIndex.get(0) - 1);
				if (childrenPerIndex.get(0) == 0) {
					result += "]$";
					childrenPerIndex.remove(0);
				} else {
					result += "]#";
				}
			} else {
				IndexNode index = ((IndexNode) target);
				result += "@";
				for (int i = 0; i < index.keys.size(); i++) {
					result += "" + index.keys.get(i) + "/";
				}
				result += "@";
				queue.addAll(index.children);
				if (index.children.get(0).isLeafNode) {
					childrenPerIndex.add(index.children.size());
				}
				nodesInNextLevel += index.children.size();
			}

			if (nodesInCurrentLevel == 0) {
				result += "%%";
				nodesInCurrentLevel = nodesInNextLevel;
				nodesInNextLevel = 0;
			}

		}
		System.out.println(result);
		return result;

	}

}
