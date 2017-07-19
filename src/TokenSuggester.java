import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;


public class TokenSuggester {
	
	SpellChecker spellchecker = null;
	Directory dictionaryIndex = null;
	IndexReader reader = null;
	Map<String, Integer> freqMap = new HashMap<String, Integer>();
	//DistanceImpl distance = new DistanceImpl();
	IcyDistance distance = new IcyDistance();
	
	List<String> stopwords = null;
	
	public TokenSuggester(String indexPath) throws IOException {
		dictionaryIndex = FSDirectory.open(new File(indexPath));
		Directory spellIndex = new RAMDirectory();
		reader = IndexReader.open(dictionaryIndex);
		spellchecker = new SpellChecker(spellIndex, new LevensteinDistance());
		System.out.println("Indexing...");
		//spellchecker.indexDictionary(new PlainTextDictionary(new File(textFile)));
		spellchecker.indexDictionary(new LuceneDictionary(reader, "word"));
		System.out.println("Done.");
		
		stopwords = (List<String>)Arrays.asList("in", "from", "to", "of", "at", "on", "along", "with");
		
		for (int i=0; i<reader.numDocs(); i++) {
			freqMap.put(reader.document(i).get("word"), Integer.parseInt(reader.document(i).get("freq")));
		}

		System.out.println("Map populated with words: "+freqMap.size());
		//System.out.println(spellchecker.suggestSimilar("denver",10));
		/*spellchecker.close();
		reader.close();*/
	}
	
	List<Suggestion> suggest (String word) throws IOException {
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		
		if (spellchecker.exist(word) || stopwords.contains(word) || word.length()<=2) {
			int freq = freqMap.containsKey(word)?freqMap.get(word):0;
			suggestions.add(new Suggestion(word, freq, distance.getDistance(word, word)));
		}
		
		for (String sug: spellchecker.suggestSimilar(word, 30))
			suggestions.add(new Suggestion(sug, freqMap.get(sug), distance.getDistance(word, sug)));
		Collections.sort(suggestions);
		return suggestions;
	}
	
	public static void main(String[] args) throws IOException {
		TokenSuggester suggester = new TokenSuggester("res/dictionary.lucene");
		System.out.println(suggester.suggest("hemroid"));
		System.out.println(suggester.suggest("hajib"));
		System.out.println(suggester.suggest("karhman"));
		System.out.println(suggester.suggest("the"));
		System.out.println(suggester.suggest("in"));
		System.out.println(suggester.suggest("ihone"));
	}

}
