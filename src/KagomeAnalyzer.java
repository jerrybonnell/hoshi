package src;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException; 
import java.io.InputStream;

public class KagomeAnalyzer extends MecabAnalyzer {

	private String dict; 
	private String mode;

	public KagomeAnalyzer(String dict, String mode) {
		this.dict = dict; 
		this.mode = mode; 
	}

	public static String execCmd(String[] cmd) throws java.io.IOException {
    	java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    	System.out.println("finished");
    	return s.hasNext() ? s.next() : "";
	}

	public String[] tokenize(String sentence) {
		try {
			String[] cmd = {
			"/bin/sh",
			"-c",
			"echo " + sentence + " | go run main.go tokenize"
			};
			String output = execCmd(cmd);
			String[] tmp = output.split("\n");
		    String[] listOut = new String[tmp.length - 1]; 
		    // dont copy eos
		    System.arraycopy(tmp, 0, listOut, 0, tmp.length - 1);		
		    return listOut; 
		} catch(IOException e) {
			System.err.println(e);
		}

		return null; 
		
	}

}