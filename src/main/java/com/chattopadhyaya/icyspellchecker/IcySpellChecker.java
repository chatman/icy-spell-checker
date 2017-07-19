package com.chattopadhyaya.icyspellchecker;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chattopadhyaya.icyspellchecker.util.BloomFilter;
import com.chattopadhyaya.icyspellchecker.util.CombinationGenerator;
import com.chattopadhyaya.icyspellchecker.util.DoubleMeta;
import com.chattopadhyaya.icyspellchecker.util.IcyDistance;
import com.chattopadhyaya.icyspellchecker.util.KeylessHashTable;
import com.chattopadhyaya.icyspellchecker.util.LevDistance;


/**
 * 
 * @author Ishan Chattopadhyaya
 *
 */
public class IcySpellChecker {

	private Log logger = LogFactory.getLog(IcySpellChecker.class.getName());
	private KeylessHashTable<String, String> map;
	private BloomFilter bf = new BloomFilter(16);
	private BloomFilter tokenFilter = new BloomFilter(16);
	private Set<String> dictionaryWords = new HashSet<String>(); 
	private BloomFilter candidateFilter = new BloomFilter(80);

	private BloomFilter positionalFilters[] = new BloomFilter[15];

	private BloomFilter dmCandidateFilter = new BloomFilter(15);
	private BloomFilter dmValuesFilter = new BloomFilter(5);
	public KeylessHashTable<String, String> dmIntermediateMap = new KeylessHashTable<String, String>(1000000);

	public IcySpellChecker(int capacity) {
		map = new KeylessHashTable<String, String>(capacity);
		for (int i=0; i<positionalFilters.length; i++)
			positionalFilters[i] = new BloomFilter(5);
	}

	public IcySpellChecker() {
		map = new KeylessHashTable<String, String>(200);
		for (int i=0; i<positionalFilters.length; i++)
			positionalFilters[i] = new BloomFilter(5);
	}

	private static boolean containsNumber (String str) {
		for (int i=0; i<str.length(); i++) {
			if (str.charAt(i)>='0' && str.charAt(i)<='9')
				return true;
		}
		return false;
	}

	private static List<String> generate (String str, boolean searchTime)
	{
		List<String> list = new ArrayList<String>();

		if (str.length()>0)
			list.add(str);
		if (containsNumber(str))
			return list;
		if (str.length()<=2 && searchTime)
			return list;
		for (int i=0; i<str.length(); i++) {
			StringBuilder tmp = new StringBuilder();
			for (int j=0; j<str.length(); j++) {
				if (i!=j)
					tmp.append(str.charAt(j)); 
			}
			if (tmp.length()>0)
				list.add(tmp.toString());
		}
		if (str.length()>5)
			for (int i=0; i<str.length(); i++) {
				for (int k=i+1; k<str.length(); k++) {	
					StringBuilder tmp=new StringBuilder();
					for (int j=0; j<str.length(); j++) {
						if (i!=j && j!=k)
							tmp.append(str.charAt(j)); 
					}
					if (tmp.length()>0)
						list.add(tmp.toString());
				}
			}


		return list;
	}

	private void write (String path, String bffile, String candidateFile, String tokenFilterFile) throws IOException
	{

		FileOutputStream fos = null ;
		ObjectOutputStream oos = null ;

		try {
			fos = new FileOutputStream ( path ) ;
			oos = new ObjectOutputStream ( fos ) ;

			oos.writeObject( this );
			System.out.println("Written...");
		}
		catch ( Exception e )
		{     }
		finally
		{
			if ( oos != null ) {
				oos.close ();
			}
		}

		bf.writeToFile(bffile);
		for (int i=0; i<positionalFilters.length; i++)
			positionalFilters[i].writeToFile(bffile+i);
		candidateFilter.writeToFile(candidateFile);
		tokenFilter.writeToFile(tokenFilterFile);
	}

	private void read (String path, String bffile, String dictionaryWordsFile, 
			String candidatesFile, String tokenFilterFile) throws IOException {

		// Load the map
		try {
			FileInputStream fis = null ;
			ObjectInputStream ois = null ;

			fis = new FileInputStream ( path ) ;
			ois = new ObjectInputStream ( fis ) ;

			Object ob =  ois.readObject();

			if ( ob == null ) {
				throw new RuntimeException("KeylessHashTable cannot read object from file "+path);
			} else 			
				if (ob instanceof KeylessHashTable)
					map =  (KeylessHashTable<String, String>) ob;
				else
					throw new RuntimeException("KeylessHashTable cannot read object from file "+path);
		} catch (ClassNotFoundException ce) {
			logger.error("Cound not instatiate spellchecker", ce);
			throw new RuntimeException(ce);
		} catch (IOException ie) {
			logger.error("Cound not instatiate spellchecker: ", ie);
			throw new RuntimeException(ie);
		}

		bf = new BloomFilter(bffile);
		tokenFilter = new BloomFilter(tokenFilterFile);
		logger.debug("Spell filter loaded...");
		for (int i=0; i<positionalFilters.length; i++)
			positionalFilters[i] = new BloomFilter(bffile+i);
		logger.debug("Positional filters loaded...");
		BufferedReader br = new BufferedReader(new FileReader(dictionaryWordsFile));
		String line;
		while ((line=br.readLine())!=null)
			dictionaryWords.add(line.toLowerCase());
		br.close();

		logger.debug("Words filter loaded...");
		candidateFilter = new BloomFilter(candidatesFile);
		logger.debug("Candidates filter loaded...");
	}

	Set<String> stopWords;
	public long rawAdds = 0;
	public long totalLength = 0;
	public long numTokens=0;


	final int THRESHOLD_LENGTH = 20;
	DoubleMeta dm = new DoubleMeta();

	public void add (String str) throws UnsupportedEncodingException
	{
		String tokens[] = str.split(" ");

		for (int t=0; t<tokens.length && t<15; t++)
		{
			String word = new String(tokens[t]).toLowerCase(); 
			word = new String (word);

			tokenFilter.addWord(word);

			if (word.length()<= THRESHOLD_LENGTH+2)
				for (String candidate: generate(word, false))
				{
					map.put(candidate, word);
					if (candidateFilter.wordExists(candidate)==false)
						rawAdds++;
					candidateFilter.addWord(candidate);
				}

			if (word.length()>=THRESHOLD_LENGTH-2) {
				if (word.equalsIgnoreCase("philadelphia"))
					System.out.println("PHILLY");
				String tokenDm = dm.transform(word);
				//dmMap.add(tokenDm, word);
				dmValuesFilter.addWord(tokenDm);


				for (int i=0; i<=tokenDm.length(); i++) {
					StringBuilder tmp = new StringBuilder();
					for (int j=0; j<tokenDm.length(); j++) {
						if (i!=j)
							tmp.append(tokenDm.charAt(j)); 
					}
					if (tmp.length()>0) {
						//System.err.println(tmp);
						dmCandidateFilter.addWord(tmp.toString());

						dmIntermediateMap.put(tmp.toString(), word);
						//System.err.println("INTMAP: "+dmIntermediateMap);
					}
				}
			}

			positionalFilters[t].addWord(word);
		}
		bf.addWord(str);
	}

	private String sanitize(String query)
	{
		while (query.contains("  "))
			query = query.replace("  ", " ");
		return query.replace("'", "");
	}

	public Set<String> search (String query, double dist) throws UnsupportedEncodingException
	{
		query = sanitize (query);
		Set<String> results = new HashSet<String>();

		CombinationGenerator combinations = new CombinationGenerator();
		List<Set<String>> tokenSuggestions = new ArrayList<Set<String>>();

		String tokens[] = query.split(" ");

		if (bf.wordExists(query) && tokens.length>=2)
		{
			results.add(query);
			return results;
		}

		if (tokens.length>5)
			return results;

		for (int i=tokens.length-1; i>=0; i-- )
		{
			String q = tokens[i].toLowerCase();
			Set<String> suggestions = new HashSet<String>();

			if(((q.length()>=4 && dictionaryWords.contains(q) && positionalFilters[i].wordExists(q)) ||
					(q.length()>=4 && dictionaryWords.contains(q) && tokens.length>=3)
					|| q.length()>=4 && tokens.length>=3 && tokenFilter.wordExists(q)) )
			{
				suggestions.add((String)q);
				logger.debug(q+" found in dictionary, needn't find it.");
			}
			else {

				if (q.length()>=THRESHOLD_LENGTH-2) {
					/*if (q.equalsIgnoreCase("philadelphia"))
						System.out.println("PHILLY");
					String queryDm = dm.transform(q);

					IcyDistance damlev = new IcyDistance(q);

					for (int z=0; z<=queryDm.length(); z++) {
						StringBuilder tmp = new StringBuilder();
						for (int j=0; j<queryDm.length(); j++) {
							if (z!=j)
								tmp.append(queryDm.charAt(j)); 
						}
						if (tmp.length()>0) {
							//list.add(tmp.toString());
							String intermediate = tmp.toString();
							//System.out.println(intermediate);
							if (dmCandidateFilter.wordExists(intermediate)) {

								for (String s: dmIntermediateMap.getAll(intermediate)) {

									//for (String sug: (List<String>)dmMap.getValues(s)) {
										double distance = damlev.getDistance(q, s); 
										if (distance<=2) {
											suggestions.add(s);
										}


									//}
								}
							}

						}
					}*/
				}

				if (q.length()<=THRESHOLD_LENGTH)
					for (String candidate: generate (q, true)) {
						IcyDistance damlev = new IcyDistance(q);
						LevDistance lev = new LevDistance();
						if (candidateFilter.wordExists(candidate)) {
							//results.add(map.get(candidate));
							//System.out.println(candidate);
							List<String> sugs ;

							if (candidate.length()<=2 && bf.wordExists(candidate)) {						
								sugs = new ArrayList<String>();
								sugs.add(candidate);
							}
							else 
								sugs = map.getAll(candidate);


							logger.debug(candidate+": "+sugs);
							if  (sugs!=null)
								for (String s: sugs)
								{
									//double suggestionDistance = IcyDistance.getDistance(q, s);
									double suggestionDistance = Double.MAX_VALUE;

									if (Math.abs(s.length()-q.length())<=3) {
										suggestionDistance = lev.getDistance(s,q);
										if (suggestionDistance>=2)
											suggestionDistance = damlev.getDistance(s);
									}
									//System.out.println("dam("+q+", "+s+")");

									// Hack to stop spell correction of dict words, fixes "best buy stores"
									if (dictionaryWords.contains(q)) 
										suggestions.add(q);
									else if (q.length()<=2 || containsNumber(q)) {
										if (suggestionDistance<=0 && positionalFilters[i].wordExists((String)s))
											suggestions.add((String)s);

									}
									else if (q.length()<=6) {
										if (suggestionDistance<=1 && positionalFilters[i].wordExists((String)s))
											suggestions.add((String)s);
									}
									else
										if (suggestionDistance<=2 && positionalFilters[i].wordExists((String)s) && dictionaryWords.contains(q)==false)
											suggestions.add((String)s);

									logger.debug("Sug: "+s);
								}
						}
					}
			}

			if (suggestions.size()==0) {
				return results;
			}

			tokenSuggestions.add(suggestions);
			combinations.add(suggestions.toArray());
		}

		if (tokens.length==1)
			return tokenSuggestions.get(0);

		Iterator<Object[]> itAllCombinations = combinations.iterator();
		while (itAllCombinations.hasNext()) {
			Object[] suggestions = itAllCombinations.next();
			String result="";

			for (int i=suggestions.length-1; i>=0; i--)
			{
				Object sug = suggestions[i];
				result+=sug+" ";
			}
			result=result.trim();
			logger.debug("Combinations tried: "+result);
			if (bf.wordExists(result))
				results.add(result);
		}

		return results;
	}

	static List<String> correct (IcySpellChecker spell, String query) throws UnsupportedEncodingException 
	{
		List<String> answer = new ArrayList<String>();
		String tokens[] = query.split(" |,");

		for (int i=1; i<=tokens.length; i++)
		{

			for (int j=0; j<tokens.length-i+1; j++)
			{
				StringBuilder sb = new StringBuilder();
				for (int k=j; k<j+i; k++)
					sb.append(tokens[k]+" ");
				String shingle = sb.toString().trim();

				long stTime = System.currentTimeMillis();
				Set<String> suggestions = spell.search(shingle, 2);
				long endTime = System.currentTimeMillis();
				System.out.println("Time for "+shingle+": "+(endTime-stTime));

				if (!suggestions.contains(shingle))
					System.out.println(shingle+": "+shingle);
				else
					System.out.println(shingle+": "+suggestions);
			}

		}
		return answer;
	}


	public static void build (String PATH) throws IOException {
		IcySpellChecker spell = new IcySpellChecker(); //IcySpellChecker.getInstance(PATH);
		BufferedReader reader = new BufferedReader(new FileReader(PATH+"admin_names.txt"));

		String line=null;
		int i=0;

		while ((line=reader.readLine())!=null) {
			spell.add(line);
			i++;
			if (i%100000==0)
			{
				{	
					String ans = ""; //(String)spell.map.get(line);
					System.out.println(i+": <"+line+","+ans+">"+" (rawadds="+spell.rawAdds+", "+spell.map.size()+")");
				}
			}
		}
		System.out.println("Insert Done");
		reader.close();
		spell.write(PATH+"spell.icy", PATH+"spell.bf", PATH+"candidates.filter", PATH+"token.filter");

	}


	public List<SpellCheckResult> doSpellCheck(String token, double editDistance) {
		List<SpellCheckResult> result = new ArrayList<SpellCheckResult>();
		IcyDistance icyDistance = new IcyDistance(token);
		try {
			Set<String> set = search(token, editDistance);
			for (String sug: set)
			{
				//System.out.println("Suggestion for "+token+": "+sug);
				//double dist = IcyDistance.getWeightedDistance(sug, token);
				double dist = icyDistance.getDistance(sug);
				SpellCheckResult spRes = new SpellCheckResult(sug, dist);
				if (dist<=editDistance)
					result.add(spRes);
			}
		} catch (UnsupportedEncodingException e) {
			System.err.println("Exception for: "+token);
			e.printStackTrace();
		}


		return result;
	}

}
