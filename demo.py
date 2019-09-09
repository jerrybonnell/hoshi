"""
short demo python script to demonstrate querying
"""
import sys
import getopt
from pprint import pprint
import re


def parse(inputs):
    print(inputs)
    # build regular expression
    regex = r"("
    for key, value in inputs.items():
        if value is not None and key != 'infile':
            regex += key + "=\"" + value + "\"" + " "
        elif key != 'infile':
            regex += ".*"

    if regex[-1] == ' ':
        regex = regex[:-1]  # trim extra white space
    regex += ")"
    # regex = "(type1=\"助詞\" .*type3=\"引用\".*.*.*.*)"
    # need to do an optimization if we have runs of .*
    regex = re.sub(r'(\.\*)\1+', r'\1', regex)
    #print(regex)
    f = open(inputs['infile'], 'r')
    lines = f.readlines()
    f.close()

    # Iterate each line
    lis = []
    for i in range(len(lines)):
        # Regex applied to each line
        match = re.search(regex, lines[i])
        # situation where text is segmented
        if "token" in lines[i] and match:
            # just get the token b/c it has the answer
            lis.append(lines[i].split("\"")[1])
        elif match:
            # NOTE we should never have an exception
            lis.append(lines[i + 1].strip())

    words = set(lis)
    pprint(words)
    print("tokens found : " + str(len(words)))


def main(argv):
    inputfile = None
    type1 = type2 = type3 = type4 = None
    number = rule = root = spelled = spoken = None
    try:
        opts, args = getopt.getopt(argv,
                                   "hi:1:2:3:4:n:r:l:s:p:",
                                   ["ifile=", "type1=", "type2=", "type3=",
                                    "type4=", "number=", "rule=", "root=",
                                    "spelled=", "spoken="])
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
        elif opt in ("-o", "--root"):
            root = arg
        elif opt in ("-s", "--spelled"):
            spelled = arg
        elif opt in ("-p", "--spoken"):
            spoken = arg

    parse({'infile': inputfile,
           'type1': type1,
           'type2': type2,
           'type3': type3,
           'type4': type4,
           'number': number,
           'rule': rule,
           'root': root,
           'spelled': spelled,
           'spoken': spoken})


if __name__ == "__main__":
    main(sys.argv[1:])
