package com.chattopadhyaya.icyspellchecker.util;


import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class AllCombinationIteratable<K> implements Iterator<K[]> {
	
	private List<K[]> matrix;
	private int[] counter ;
	private K[] combination;
	int len;
	public AllCombinationIteratable(List<K[]> matrix, K[] combination) {
		
		this.matrix = matrix;
		counter = new int[matrix.size()];
		this.combination = combination;
		this.len = combination.length;
	}
	
	public void remove() {
		throw new IllegalStateException("not implemented");
	}

	public boolean hasNext() {
		int s = counter.length;
		return counter[s - 1] < matrix.get(s - 1).length;
	}

	public K[] next(K[] ls) {
		if (!hasNext()) {
			throw new NoSuchElementException("no more elements");
		}
		
		for (int i = 0; i < counter.length; i++) {
			ls[i] = (matrix.get(i)[counter[i]]);
		}
		incrementCounter();
		return ls;
	}

	private void incrementCounter() {
		for (int i = 0; i < counter.length; i++) {
			counter[i]++;
			if (counter[i] == matrix.get(i).length
					&& i < counter.length - 1) {
				counter[i] = 0;
			} else {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public K[] next() {
		// TODO Auto-generated method stub
		combination = (K[]) java.lang.reflect.Array.newInstance(combination.getClass().getComponentType(), len);
		return next(combination);
	}

	

}
