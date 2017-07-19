package com.chattopadhyaya.icyspellchecker.util;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class CombinationGenerator {
    
    private List<Object[]> suggestionMatrix = new LinkedList<Object[]>();
    
    public CombinationGenerator() {
        // TODO Auto-generated constructor stub
        suggestionMatrix = new LinkedList<Object[]>();
    }
    
    public class SuggestionIterator implements Iterator<Object[]> {
        private int[] counter = new int[suggestionMatrix.size()];
        public void remove() {
            throw new IllegalStateException("not implemented");
        }
        public boolean hasNext() {
            int s = counter.length;
            return counter[s - 1] < suggestionMatrix.get(s - 1).length;
        }
        public Object[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException("no more elements");
            }
            Object[] ls = new String[counter.length];
            for (int i = 0; i < counter.length; i++) {
                ls[i] = (suggestionMatrix.get(i)[counter[i]]);
            }
            incrementCounter();
            return ls;
        }
        
        private void incrementCounter() {
            for (int i = 0; i < counter.length; i++) {
                counter[i]++;
                if (counter[i] == suggestionMatrix.get(i).length &&
                        i < counter.length - 1) {
                    counter[i] = 0;
                } else {
                    break;
                }
            }
        }
    }
    
    public Iterator<Object[]> iterator()
    {
        return new SuggestionIterator();
    }

    public void add (Object[] tuple)
    {
        suggestionMatrix.add(tuple);
    }

    
}