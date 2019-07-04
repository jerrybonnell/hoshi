package src;

// import org.chasen.mecab.MeCab;
// import org.chasen.mecab.Tagger;
import java.io.IOException;

public class MecabAnalyzer implements Analyzer {

	// Tagger tagger;

	private static final String SPACE_ANNOTATION = " \t*,*,*,*,*,*,*,*,*";

	public MecabAnalyzer() {
		// tagger = new Tagger();
	}

	public static String execCmd(String[] cmd) throws java.io.IOException {
    	java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    	System.out.println("finished");
    	return s.hasNext() ? s.next() : "";
	}

	// public String[] tokenize2(String sentence) {
	// 	// replace any spaces with maru b/c mecab can't handle it
	// 	sentence = sentence.replace(" ", "。");
	// 	String output = tagger.parse(sentence);
	//     String[] tmp = output.split("\n");
	//     String[] listOut = new String[tmp.length - 1];
	//     // dont copy eos
	//     System.arraycopy(tmp, 0, listOut, 0, tmp.length - 1);

	//     for ( int i = 0 ; i < listOut.length - 1 ; i++ ) {
	//     	if ( listOut[i].charAt(0) == '。' ) {
	//     		listOut[i] = SPACE_ANNOTATION;
	//     	}
	//     }
	//     return listOut;
	// }

	@Override
	public String[] tokenize(String sentence) {
		// replace any spaces with maru b/c mecab can't handle it
		sentence = sentence.replace(" ", "。");

		try {
			String[] cmd = {
			"/bin/sh",
			"-c",
			"echo " + sentence + " | mecab"
			};
			String output = execCmd(cmd);
			String[] tmp = output.split("\n");
		    String[] listOut = new String[tmp.length - 1];
		    // dont copy eos
		    System.arraycopy(tmp, 0, listOut, 0, tmp.length - 1);
		    for ( int i = 0 ; i < listOut.length - 1 ; i++ ) {
		    	if ( listOut[i].charAt(0) == '。' ) {
		    		listOut[i] = SPACE_ANNOTATION;
		    	}
	    	}
		    return listOut;
		} catch(IOException e) {
			System.err.println(e);
		}
		return null;
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
