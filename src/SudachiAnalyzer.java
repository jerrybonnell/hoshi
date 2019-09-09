package src;


import java.io.IOException;

public class SudachiAnalyzer implements Analyzer {

    public SudachiAnalyzer() {
    }

    public static String execCmd(String[] cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        System.out.println("finished");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public String[] tokenize(String sentence) {
        try {
            String[] cmd = {
            "/bin/sh",
            "-c",
            "echo " + sentence + " | sudachipy -a"
            };
            String output = execCmd(cmd).replace("\t", ",");
            String[] tmp = output.split("\n");
            String[] listOut = new String[tmp.length - 1];
            // we go to tmp.length - 1 so that we don't copy in EOS
            for (int i = 0; i < tmp.length - 1; i++) {
                String[] attributeList = tmp[i].split(",");
                String newLine = attributeList[0] + "\t";
                for (int j = 1; j <= 9; j++) {
                    newLine += attributeList[j] + ",";
                }
                newLine = newLine.substring(0, newLine.length() - 1);
                listOut[i] = newLine;
            }

            for (String s : listOut) {
                System.out.println(s);
            }

            return listOut;
        } catch(IOException e) {
            System.err.println(e);
            System.exit(1);
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
