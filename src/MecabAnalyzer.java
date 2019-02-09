package src;

import org.chasen.mecab.MeCab;
import org.chasen.mecab.Tagger;

public class MecabAnalyzer implements Analyzer {

	Tagger tagger; 

	public MecabAnalyzer() {
		tagger = new Tagger();
	}

	@Override
	public String[] tokenize(String sentence) {
		String output = tagger.parse(sentence);
	    String[] tmp = output.split("\n");
	    String[] listOut = new String[tmp.length - 1]; 
	    // dont copy eos
	    System.arraycopy(tmp, 0, listOut, 0, tmp.length - 1);
	    return listOut; 
	}

	@Override
	public String getSurfaceForm(String annotation) {
		return annotation.split("\t")[0];
	}

	@Override
	public String getAllFeatures(String annotation) {
		return annotation.split("\t")[1];
	}

}