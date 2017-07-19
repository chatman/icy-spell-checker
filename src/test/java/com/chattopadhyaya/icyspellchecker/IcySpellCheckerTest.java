package com.chattopadhyaya.icyspellchecker;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

import com.chattopadhyaya.icyspellchecker.IcySpellChecker;

import static org.junit.Assert.*;

public class IcySpellCheckerTest {

	@Test
	public void basicTest() throws UnsupportedEncodingException {
		IcySpellChecker icy = new IcySpellChecker(2000000);
		
		String dictionary[] = {"india", "pakistan", "bhutan", "nepal", "maldives", "sri lanka",
				"bangladesh", "thailand", "myanmar", "china", "tibet", "poland", "portland",
				"roland", "albequerque"};
		for (String entry: dictionary) {
			icy.add(entry);
		}

		assertEquals("albequerque", icy.doSpellCheck("albuquerque", 2).get(0).getTerm());
		assertEquals(3, icy.doSpellCheck("potland", 2).size());
		assertEquals(1, icy.doSpellCheck("srl lankao", 2).size());
	}
	
}
