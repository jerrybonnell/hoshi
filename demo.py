import subprocess
import sys, getopt

#command = "pcregrep -n -Mi '<gloss n=\"type1\">副詞</gloss>(\n.*){0,7}<gloss n=\"lemma\">必ず</gloss>' out/test-stack.xml | awk '{print $1}' FS=\"<\" | cut -f1 -d \":\""
def parse(inputs):
    print(inputs)
    # TODO what happens when you only give lemma 
    gloss = "'<gloss n=\"type1\">名詞</gloss>(\n.*){0,7}" + \
         "<gloss n=\"type2\">一般</gloss>' "
    command = "pcregrep -n -Mi " + gloss + inputs[0] + \
        " | awk '{print $1}' FS=\"<\" | cut -f1 -d \":\""

    result = subprocess.Popen(command, 
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
    output, err = result.communicate()

    output_str = str(output)
    output_str = output_str[2:-1] # remove the b and single quotes from output
    result_list = output_str.replace(" ", "").split("\\n\\n")
    # clean up whitespace
    result_list = [int(r) for r in result_list if r.isdigit()] 
    result_list = [r - 1 for r in result_list] # TODO deal with offset 
    # sed pattern is 'q;d'
    patterns = [str(r) + 'q;d' for r in result_list]
    output_list = []
    for pattern in patterns: 
        # sed "136q;d" out/test-stack.xml 
        command = "sed '" + pattern + "' " + inputs[0] + \
            " | grep -o '\".*\"' " + "| tr -d '\"'"
        result = subprocess.Popen(command, 
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True)
        output, err = result.communicate()
        output_list.append(output.decode().replace("\n", ""))

    words = set(output_list)
    print(words)
    print("num : " + str(len(words)))

def main(argv):
    inputfile = None
    type1 = type2 = type3 = type4 = None
    number = rule = lemma = spelled = spoken = None
    try:
        opts, args = getopt.getopt(argv,
            "hi:1:2:3:4:n:r:l:s:p:",
            ["ifile=","type1=","type2=","type3=","type4=",
            "number=","rule=","lemma=","spelled=","spoken="])
    except getopt.GetoptError:
        print('test.py -i <inputfile> ')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('test.py -i <inputfile> ')
            sys.exit()
        elif opt in ("-i", "--ifile"):
            inputfile = arg
        elif opt in ("-1", "--type1"):
            type1 = arg
        elif opt in ("-2", "--type2"):
            type2 = arg
        elif opt in ("-3", "--type3"):
            type3 = arg
        elif opt in ("-4", "--type4"):
            type4 = arg
        elif opt in ("-n", "--number"):
            number = arg
        elif opt in ("-r", "--rule"):
            rule = arg
        elif opt in ("-l", "--lemma"):
            lemma = arg
        elif opt in ("-s", "--spelled"):
            spelled = arg
        elif opt in ("-p", "--spoken"):
            spoken = arg

    parse([inputfile, type1, type2, type3, 
        type4, number, rule, lemma, spelled, spoken])

if __name__ == "__main__":
    main(sys.argv[1:])


