import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;


public class DictionaryIndexer {

	public void buildIndex (String textFile, String indexPath) throws IOException {
		Directory dictionaryIndex = FSDirectory.open(new File(indexPath));
		IndexWriter writer = new IndexWriter(dictionaryIndex, new KeywordAnalyzer(), true, MaxFieldLength.UNLIMITED);

		Map<String, Integer> map = new TreeMap<String, Integer>(); 
		BufferedReader br = new BufferedReader(new FileReader(textFile));
		String line;
		System.out.println("Reading words...");
		while ((line=br.readLine())!=null) {
			String tokens[] = line.split("\\t");
			if (tokens.length<2) continue;
			String word = tokens[0];
			int freq = 0;
			try { freq = Integer.parseInt(tokens[1]); } catch (NumberFormatException ex) { freq = -1; }
			if (freq==-1)
				continue;
			if (word.matches("[a-zA-Z]+")) {
				word = word.toLowerCase();
				freq = map.containsKey(word) ? freq + map.get(word) : freq;
				map.put(word, freq);
			}
		}
		br.close();

		System.out.println(map.size()+" words to be inserted.");
		for (String word: map.keySet()) {
			if (map.get(word)>=15000 && word.length()<=16) {
				Document doc = new Document();
				doc.add(new Field("word", word, Store.YES, Index.ANALYZED));
				doc.add(new Field("freq", map.get(word).toString(), Store.YES, Index.ANALYZED));
				writer.addDocument(doc);
			}

		}
		writer.close();

		/*IndexReader reader = IndexReader.open(dictionaryIndex);
		SpellChecker spellchecker = new SpellChecker(dictionaryIndex);
		System.out.println("Indexing...");
		//spellchecker.indexDictionary(new PlainTextDictionary(new File(textFile)));
		spellchecker.indexDictionary(new LuceneDictionary(reader, "word"));
		System.out.println("Done.");

		spellchecker.close();
		reader.close();*/
	}

	/*public void testIndex (String indexPath) throws IOException {
		Directory spellIndexDirectory = FSDirectory.open(new File(indexPath));
		SpellChecker spellchecker = new SpellChecker(spellIndexDirectory);

		String[] suggestions = spellchecker.suggestSimilar("washngton", 20);
		for (String sug: suggestions)
			System.out.println("SUGGESTION: "+sug);
		spellchecker.close();
	}*/

	public static void main(String[] args) throws IOException {

		DictionaryIndexer dictionaryIndexer = new DictionaryIndexer();

		dictionaryIndexer.buildIndex("res/vocab.sorted", "res/dictionary.lucene");
		//dictionaryIndexer.testIndex("res/dictionary.lucene");

	}
}
