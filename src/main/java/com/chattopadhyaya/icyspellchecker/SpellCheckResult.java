package com.chattopadhyaya.icyspellchecker;

public class SpellCheckResult {

	private double distance;
	private String term;
	
	public SpellCheckResult(String term, double distance) {
		this.term = term;
		this.distance = distance;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	
	@Override
	public String toString() {
		return term+"("+distance+")";
	}
}


