package src;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

public class KuroAnalyzer extends MecabAnalyzer {

	Tokenizer tokenizer;

	public KuroAnalyzer() throws IOException {

		tokenizer = new Tokenizer() ;
	}

	@Override
	public String[] tokenize(String sentence) {
		List< Token > result = tokenizer.tokenize( sentence );
		String[] out = new String[result.size()];
		int i = 0;
		for ( Token token : result ) {
			out[i] = token.getSurface() + "\t" + token.getAllFeatures();
			i++;
  		}
  		return out;
	}

}
