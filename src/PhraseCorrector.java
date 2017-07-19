import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.apache.commons.math.stat.descriptive.moment.FourthMoment;


class Permutation implements Comparable<Permutation> {
	Suggestion tokens[];
	double gramScores[] = new double[5];
	public Permutation(Suggestion tokens[]) {
		this.tokens = tokens;
	}
	
	@Override
	public int compareTo(Permutation o) {
		for (int i=4; i>=0; i--) {
			if (o.gramScores[i]>gramScores[i])
				return 1;
			else if (gramScores[i]>o.gramScores[i])
				return -1;
		}
		return 0;
	}
}
public class PhraseCorrector {
	private TokenSuggester tokenSuggester = null;
	private NGramStore ngramStore = null;

	public PhraseCorrector() throws IOException, SQLException, ClassNotFoundException {
		tokenSuggester = new TokenSuggester("res/dictionary.lucene");
		ngramStore = new NGramStore();
	}
	
	// 1gm: 84413
	// 2gm: 2210
	// 3gm: 620
	// 4gm: 324
	// 5gm: 253

	List<String> suggest (String phrase) throws IOException, SQLException {
		String tokens[] = phrase.split(" ");

		List<List<Suggestion>> tokenSuggestions = new ArrayList<List<Suggestion>>();
		for (String token: tokens) {
			List<Suggestion> tokenSuggestion = tokenSuggester.suggest(token);
			//System.out.println(tokenSuggestion);
			tokenSuggestions.add(tokenSuggestion);
		}

		List<Suggestion[]> matrix = new ArrayList<Suggestion[]>();
		Suggestion combination[] = new Suggestion[tokens.length];
		for (List<Suggestion> tokenSuggestion: tokenSuggestions) {
			Suggestion suggestions[] = new Suggestion[Math.min(tokenSuggestion.size(), 3)];
			for (int i=0; i<suggestions.length && i<3; i++)
				suggestions[i] = tokenSuggestion.get(i);
			matrix.add(suggestions);
		}
		
		AllCombinationIteratable<Suggestion> allCombinations = new AllCombinationIteratable<Suggestion>(matrix, combination);
		
		List<Permutation> permutations = new ArrayList<Permutation>();
		while (allCombinations.hasNext()) {
			Permutation permutation = new Permutation(allCombinations.next());
			calcScore(permutation);
			//printPermutation(permutation);
			permutations.add(permutation);
		}
		
		Collections.sort(permutations);
		
		System.out.println("top 3 permutations are: ");
		printPermutation (permutations.get(0));
		printPermutation (permutations.get(1));
		printPermutation (permutations.get(2));
		return null;
	}
	
	void printPermutation (Permutation perm) {
		String phraseStr = "";
		for (Suggestion sug: perm.tokens)
			phraseStr+=sug.word+" ";
		phraseStr = phraseStr.trim();
		System.out.print(phraseStr+": ");
		for (double s: perm.gramScores) System.out.print(s+", ");
		System.out.println();

	}
	
	void calcScore (Permutation perm) {
		
		double avgScores[] = {84413, 2210, 620, 324, 253};
		
		Suggestion phrase[] = perm.tokens;
		if (phrase.length>1)
			for (int shingleLength=1; shingleLength<=phrase.length && shingleLength<=5; shingleLength++) {
				
				for (int i=0; i<phrase.length-shingleLength+1; i++) {
					String gram = "";
					for (int j=i; j<i+shingleLength; j++)
						gram+=phrase[j].word + " ";
					gram = gram.trim();
					int freq = ngramStore.getFrequency(gram);
					perm.gramScores[shingleLength-1] += freq;
					//System.out.println(gram+": "+freq);
				}
				perm.gramScores[shingleLength-1] /= avgScores[shingleLength-1]*(phrase.length-shingleLength+1);
				//System.out.println(shingleLength+" gram score="+gramScore[shingleLength-1]);
			}

		//printPermutation(perm);

	}

	public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
		PhraseCorrector phraseCorrector = new PhraseCorrector();
		//phraseCorrector.suggest("secretary of state illinios");
		//phraseCorrector.suggest("litigation on ada laws against police applications");
		//phraseCorrector.suggest("harles dicens tale of two cities");
		
		//phraseCorrector.suggest("tribute to bear on youtube");
		//phraseCorrector.suggest("Biligual Montessori at palo alto");
		//phraseCorrector.suggest("burning mouth from spicy foods");
		//phraseCorrector.suggest("ikuto and amu talk in the rain episode");
		//phraseCorrector.suggest("it kentuky derby the largest horse racing track");
		//phraseCorrector.suggest("how to get rid of a big hemroid"); // hemorrhoid
		//phraseCorrector.suggest("hajib islam"); // colloc
		//phraseCorrector.suggest("which mac liner is best");
		//phraseCorrector.suggest("goods and service tax in malaysia"); // stemm
		//phraseCorrector.suggest("karhman ghia engines for sale");
		//phraseCorrector.suggest("what finger to the engagment ring goes on"); // first letter
		//phraseCorrector.suggest("mature amateurmilf galleries"); //wordbreaking
		//phraseCorrector.suggest("jobs companies accenture");
		//phraseCorrector.suggest("ihone apple");
	}
}
