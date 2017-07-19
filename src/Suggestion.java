
public class Suggestion implements Comparable<Suggestion> {
	String word;
	int freq;
	double distance;
	double score;
	
	public Suggestion(String w, int f, double d) {
		word = w; freq = f; distance = d;
		if (distance==0)
			score = Double.MAX_VALUE;
		else
			score = Math.pow(freq, 0.1) * Math.pow(distance, 1);
	}
	
	@Override
	public String toString() {
		return word+"="+freq+" ("+score+")";
	}

	@Override
	public int compareTo(Suggestion o) {
		if (distance!=o.distance)
			if (distance > o.distance)
				return 1;
			else return -1;
		return o.freq - freq;
	}
}
