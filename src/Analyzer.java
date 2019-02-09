package src;

interface Analyzer {

	String[] tokenize(String sentence); 
	String getSurfaceForm(String annotation);
	String getAllFeatures(String annotation);
}
