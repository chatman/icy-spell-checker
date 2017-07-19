package com.chattopadhyaya.icyspellchecker.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.BitSet;

public class BloomFilter {

	/** Number of hash functions to use.  Can support up to 10 */
	public static final int HASHES = 10;

	/** Debug value */
	public static final boolean DEBUG = false;

	/** Hash Function Library */
	public static GeneralHashFunctionLibrary lib = new GeneralHashFunctionLibrary();

	/** Data stucture to hold the bits */
	private BitSet bits[];

	/** Size of this Bloom Filter */
	//private int size;
	private long SIZE;
	
	public BloomFilter (String filename) throws IOException
	{
		bits = getBytesFromFile(new File(filename));
		for (BitSet bs: bits)
			SIZE+=bs.length();
		SIZE--;
	}
	/**
	 * Standard Constructor
	 * @param input the size of the bloom filter.
	 */
	public BloomFilter(long input) {
		// Convert to MBs
		input = 8 * input * 1024 * 1024 - 1;
		if (DEBUG) {
			System.out.println("The size of this bloom filter is " + SIZE);
		}
		
		SIZE=input;
		
		int noOfBitsets = (int) (input / ((long)Integer.MAX_VALUE+1)) + 1;
		if (DEBUG) System.out.println("Size: "+noOfBitsets);
		bits = new BitSet[noOfBitsets];
		int i;
		for (i=0; i<noOfBitsets-1; i++)
		{
			bits[i] = new BitSet (Integer.MAX_VALUE);
			bits[i].set(Integer.MAX_VALUE-1);
		}
		int remaining = (int)(input % ((long)Integer.MAX_VALUE+1));
		remaining = remaining - (remaining%8) + 8;
		bits[i] = new BitSet (remaining);
		bits[i].set(remaining-1);
	}

	private void setBits (long hash)
	{
		hash = hash % SIZE;
		int setNum = (int)(hash / ((long)Integer.MAX_VALUE+1));
		int position = (int) (hash % ((long)Integer.MAX_VALUE+1));
		//System.out.println(hash+", "+setNum+", "+position);
		bits[setNum].set(position);
		
	}

	public void addWord(String word) {
		if (DEBUG)
			System.out.println("Adding word to bloom filter: " + word);

		long hash = lib.APHash(word);
		setBits(hash);                
		hash = lib.BKDRHash(word);
		setBits(hash);
		hash = lib.DEKHash(word);
		setBits(hash);
		hash = lib.SDBMHash(word);
		setBits(hash);
		hash = lib.BPHash(word);
		setBits(hash);
		hash = lib.RSHash(word);
		setBits(hash);
		hash = lib.JSHash(word);
		setBits(hash);
		hash = lib.PJWHash(word);
		setBits(hash);
		hash = lib.ELFHash(word);
		setBits(hash);
		hash = lib.DJBHash(word);
		setBits(hash);

	}

	private boolean checkHash (long hash)
	{
		hash = hash % SIZE;
		int setNum = (int)(hash / ((long)Integer.MAX_VALUE+1));
		int position = (int) (hash % ((long)Integer.MAX_VALUE+1));
		return (bits[setNum].get(position));
	}

	public boolean wordExists(String word) {

		if (!checkHash(lib.APHash(word)))
			return false;
		if (!checkHash(lib.BKDRHash(word)))
			return false;
		if (!checkHash(lib.DEKHash(word)))
			return false;
		if (!checkHash(lib.SDBMHash(word)))
			return false;
		if (!checkHash(lib.BPHash(word)))
			return false;
		//(new MD5()).hexDigest(word)
		if (!checkHash(lib.RSHash(word)))
			return false;
		if (!checkHash(lib.JSHash(word)))
			return false;
		if (!checkHash(lib.ELFHash(word)))
			return false;
		if (!checkHash(lib.PJWHash(word)))
			return false;
		if (!checkHash(lib.DJBHash(word)))
			return false;

		return true;
	}


	// Returns a bitset containing the values in bytes.
	// The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
	private BitSet fromByteArray(byte[] bytes) {
		if (DEBUG) System.err.println("Bytes size:"+bytes.length);
		BitSet bits = new BitSet(bytes.length*8-1);
		for (int i=0; i<bytes.length*8-1; i++) {
			if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	private byte[] toByteArray(BitSet bits) {
		byte[] bytes = new byte[bits.length()/8+1];
		for (int i=0; i<bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length-i/8-1] |= 1<<(i%8);
			}
		}
		return bytes;
	}

	public BitSet[] getBytesFromFile(File file) throws IOException {
		//InputStream is = new FileInputStream(file);
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		
		long offset = 0;
		long length = file.length();
		
		if (DEBUG) System.out.println(length);
		
		int SIZE = (int)Math.ceil(((double)length / ((double)((long) 1<<28))));
		BitSet bits[] = new BitSet[SIZE];
		
		if (DEBUG) System.out.println("Length of file = "+length);
		if (DEBUG) System.out.println("To read "+SIZE+" bitsets.");
		
		int bsCounter = 0;
		while (offset<length)
		{
			byte bytes[];
			
			if (length-offset>(1L<<28))
				bytes = new byte[(1<<28)];
			else
				bytes = new byte[(int)(length-offset)];
			
			if (DEBUG) System.out.println("Trying to read "+bytes.length+" bytes from file.");
			
			raf.seek(offset);
			if (DEBUG) 
				System.out.println("Result of read: "+raf.read(bytes));
			else
				raf.read(bytes);
			
			BitSet bs = fromByteArray(bytes);
			bits[bsCounter] = bs;
			offset+=bytes.length;
			if (DEBUG) System.out.println("Bitset "+bsCounter+" has length="+bs.length()+", set bits="+bs.cardinality());
			if (DEBUG) System.out.println(bs.nextSetBit(100000)+"is first set bit.");
			
			bsCounter++;
			
		}	
		
		raf.close();
		return bits;

		/*// Get the size of the file
		long length = file.length();

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}*/

		// Ensure all the bytes have been read in
		/*if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
			
		}	*/
		

		// Close the input stream and return bytes
		//is.close();
		//return bytes;
	}

	public void writeToFile (String filename) throws IOException
	{
		byte bytes[][] = new byte[bits.length][];

		File file = new File(filename);
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		
		long length = 0;

		try {
			for (int i=0; i<bits.length; i++)
			{
				bytes[i] = toByteArray(bits[i]);
				//raf.seek(offset);
				if (DEBUG) System.out.println(i + " i , Byte size = "+bytes[i].length+", bitset size = "+bits[i].length());
				bos.write(bytes[i]);
				length+=bytes[i].length;
				//bos.flush();
				//offset+=bytes[i].length;
				//fos.write(bytes[i]);
				if (DEBUG) System.out.println("Bitset "+i+" has set bits="+bits[i].cardinality());
				if (DEBUG) System.out.println(bits[i].nextSetBit(100000)+"is first set bit.");
			}
		}
		finally {
			bos.close();
			if (DEBUG) System.out.println("File length written: "+length);
		}

	}
}
