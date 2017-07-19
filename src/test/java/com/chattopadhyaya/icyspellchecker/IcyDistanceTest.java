package com.chattopadhyaya.icyspellchecker;

import org.junit.Test;

import com.chattopadhyaya.icyspellchecker.util.IcyDistance;

import static org.junit.Assert.*;

public class IcyDistanceTest {
	@Test
	public void test() {
		IcyDistance iD = new IcyDistance();

		assertEquals(1.0, iD.getDistance("off", "of"), 0.001);
		assertEquals(1.0, iD.getDistance("off", "of"), 0.001);
		assertEquals(1.0, iD.getDistance("o", "of"), 0.001);
		assertEquals(0.0, iD.getDistance("portland", "portland"), 0.001);
		assertEquals(1.0, iD.getDistance("phoenix", "pheonix"), 0.001);
		assertEquals(1.0, iD.getDistance("teh", "the"), 0.001);
		assertEquals(2.0, iD.getDistance("delhi", "dilli"), 0.001);
		assertEquals(1.0, iD.getDistance("porland", "portland"), 0.001);
		assertEquals(1.0, iD.getDistance("porland", "poland"), 0.001);

	}
}
