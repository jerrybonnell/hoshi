#
# A simple makefile for compiling three java classes
#
# date last modified: 1 jan 2019
# last modified by: jerry bonnell 
# 
# notes: added targets for compiling, running, and testing 
# 

# define a makefile variable for the java compiler
#

# location of the kuromoji jar file 
KURO = jar/kuromoji-ipadic-0.9.0.jar
KURO_CORE = jar/kuromoji-core-0.9.0.jar
#KURO = jar/kuromoji-0.7.7.jar
MECAB = jar/MeCab.jar

# name of input file 
INPUT_FILE = data/small-expan3.xml
#INPUT_FILE = data/dazai_said.xml
#INPUT_FILE = data/small-mecab.xml
# name of output file 
OUTPUT_FILE = out/test-stack.xml

TEST_ORIG_FILE = data/dazai_said.xml
TEST_REG_1 = data/small-reg1.xml
TEST_EXPAN_0 = data/small-expan0.xml
TEST_EXPAN_1 = data/small-expan1.xml
TEST_EXPAN_2 = data/small-expan2.xml
TEST_EXPAN_3 = data/small-expan3.xml
TEST_EXPAN_3B = data/small-expan3b.xml
TEST_EXPAN_3C = data/small-expan3c.xml
TEST_EXPAN_4 = data/small-expan4.xml
TEST_EXPAN_5 = data/small-expan5.xml
TEST_EXPAN_6 = data/small-expan6.xml
TEST_EXPAN_6A = data/small-expan6a.xml
TEST_EXPAN_7 = data/small-expan7.xml
TEST_EXPAN_8 = data/small-expan8.xml
TEST_EXPAN_9 = data/small-expan9.xml
TEST_EXPAN_10 = data/small-expan10.xml
TEST_CORR_1 = data/small-corr1.xml
TEST_NESTED_1 = data/small-nested1.xml
TEST_EX_1 = data/small-ex1.xml
TEST_SPLIT_1 = data/small-split.xml
TEST_SPLIT_2 = data/small-split2.xml
TEST_SPLIT_3 = data/small-split3.xml

SOL_ORIG_FILE = solution/dazai-sol.xml
SOL_TEST_REG_1 = solution/small-reg1-sol.xml
SOL_TEST_EXPAN_0 = solution/small-expan0-sol.xml
SOL_TEST_EXPAN_1 = solution/small-expan1-sol.xml
SOL_TEST_EXPAN_2 = solution/small-expan2-sol.xml
SOL_TEST_EXPAN_3 = solution/small-expan3-sol.xml
SOL_TEST_EXPAN_3B = solution/small-expan3b-sol.xml
SOL_TEST_EXPAN_3C = solution/small-expan3c-sol.xml
SOL_TEST_EXPAN_4 = solution/small-expan4-sol.xml
SOL_TEST_EXPAN_5 = solution/small-expan5-sol.xml
SOL_TEST_EXPAN_6 = solution/small-expan6-sol.xml
SOL_TEST_EXPAN_6A = solution/small-expan6a-sol.xml
SOL_TEST_EXPAN_7 = solution/small-expan7-sol.xml
SOL_TEST_EXPAN_8 = solution/small-expan8-sol.xml
SOL_TEST_EXPAN_9 = solution/small-expan9-sol.xml
SOL_TEST_EXPAN_10 = solution/small-expan10-sol.xml
SOL_TEST_CORR_1 = solution/small-corr1-sol.xml
SOL_TEST_NESTED_1 = solution/small-nested1-sol.xml
SOL_TEST_EX_1 = solution/small-ex1-sol.xml
SOL_TEST_SPLIT_1 = solution/small-split-sol.xml
SOL_TEST_SPLIT_2 = solution/small-split2-sol.xml
SOL_TEST_SPLIT_3 = solution/small-split3-sol.xml

JCC = javac -cp "$(KURO):./"  -encoding UTF-8

# define a makefile variable for compilation flags
# the -g flag compiles with debugging information
#
JFLAGS = -g

# typing 'make' will invoke the first target entry in the makefile 
# (the default one in this case)
#

TARGET=MeCab
JAVAC=javac
JAVA=java
JAR=jar
CXX=c++
ifeq ($(OS),Windows_NT)
	#TBA
else
	OS:=$(shell uname -s)
	ifeq ($(OS),Linux)
		JAVA_HOME=/usr/lib/jvm/java-6-openjdk
		JNI_MD_FOLDER=linux
		LIB_TYPE=-shared
		LIB_EXT=so
		LD_PATH_VAR=LD_LIBRARY_PATH
	endif
	ifeq ($(OS),Darwin)
		JAVA_HOME=$(shell /usr/libexec/java_home)
		JNI_MD_FOLDER=darwin
		LIB_TYPE=-dynamiclib
		LIB_EXT=dylib
		LD_PATH_VAR=DYLD_LIBRARY_PATH
	endif
	#TBA
endif

INCLUDE=$(JAVA_HOME)/include

PACKAGE=org/chasen/mecab

LIBS=`mecab-config --libs`
INC=`mecab-config --cflags` -I$(INCLUDE) -I$(INCLUDE)/$(JNI_MD_FOLDER)

ifeq ($(OS),Darwin)
	LIBS:=-arch x86_64 $(LIBS)
	INC:=-arch x86_64 $(INC)
endif

IPADIC_TARGET="https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
TAR_NAME=mecab-ipadic.tar.gz
UNZIPPED_NAME=mecab-ipadic-2.7.0-20070801

Main.class: Main.java XMLProcessor.class Adorner.class
	$(JCC) $(JFLAGS) Main.java

Adorner.class: Adorner.java Block.class
	$(JCC) $(JFLAGS) Adorner.java

XMLProcessor.class: XMLProcessor.java Block.class
	$(JCC) $(JFLAGS) XMLProcessor.java

Block.class: Block.java
	$(JCC) $(JFLAGS) Block.java

build-dict:
	wget -O $(TAR_NAME) $(IPADIC_TARGET)
	tar zxfv $(TAR_NAME)
	cd $(UNZIPPED_NAME) && ./configure && make && sudo make install

change-to-utf:
	cd $(UNZIPPED_NAME) && /usr/local/libexec/mecab/mecab-dict-index -f euc-jp -t utf-8
	cd $(UNZIPPED_NAME) && sudo make install

compile:
	#$(CXX) -O3 -c -fpic wrappers/$(TARGET)_wrap.cxx $(INC)
	#$(CXX) $(LIB_TYPE) $(TARGET)_wrap.o -o \
	#	lib$(TARGET).$(LIB_EXT) $(LIBS)
	javac -cp $(KURO_CORE):$(KURO):$(MECAB) src/*.java

run: 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji --input $(INPUT_FILE) --output $(OUTPUT_FILE)

test:
	make compile
	make run

test-all: 
	make compile
	# test original file
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_ORIG_FILE) --output $(OUTPUT_FILE) > /dev/null 
	diff $(OUTPUT_FILE) $(SOL_ORIG_FILE)
	# <expan> tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_0) --output $(OUTPUT_FILE) > /dev/null 
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_0)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_1) --output $(OUTPUT_FILE) > /dev/null 
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_1) 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_2) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_2) 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3B) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3B)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3C) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3C)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_4) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_4)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_5) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_5)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_6) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_6)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_6A) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_6A)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_7) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_7)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_8) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_8)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_9) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_9)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_10) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EXPAN_10)
	# corr tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_CORR_1) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_CORR_1)
	# ex tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EX_1) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_EX_1)	
	# nested choice tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_NESTED_1) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_NESTED_1)
	# split tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_1) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_SPLIT_1)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_2) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_SPLIT_2)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_3) --output $(OUTPUT_FILE) > /dev/null
	diff $(OUTPUT_FILE) $(SOL_TEST_SPLIT_3)

cp-all: 
	make compile
	# test original file
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_ORIG_FILE) --output $(OUTPUT_FILE) > /dev/null 
	cp $(OUTPUT_FILE) $(SOL_ORIG_FILE)
	# <expan> tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_0) --output $(OUTPUT_FILE) > /dev/null 
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_0)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_1) --output $(OUTPUT_FILE) > /dev/null 
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_1) 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_2) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_2) 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3B) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3B)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_3C) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_3C)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_4) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_4)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_5) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_5)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_6) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_6)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_6A) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_6A)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_7) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_7)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_8) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_8)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_9) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_9)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EXPAN_10) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EXPAN_10)
	# corr tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_CORR_1) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_CORR_1)
	# ex tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_EX_1) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_EX_1)	
	# nested choice tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_NESTED_1) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_NESTED_1)
	# split tests 
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_1) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_SPLIT_1)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_2) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_SPLIT_2)
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main --analyze Kuromoji \
		--input $(TEST_SPLIT_3) --output $(OUTPUT_FILE) > /dev/null
	cp $(OUTPUT_FILE) $(SOL_TEST_SPLIT_3)


clean: 
	$(RM) *.class

m-clean:
	rm -fr *.jar *.o *.$(LIB_EXT) *.class $(PACKAGE)/*.class
	
m-cleanall:
	rm -fr $(TARGET).java *.cxx

