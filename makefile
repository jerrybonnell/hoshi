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
KURO = jar/kuromoji-0.7.7.jar
MECAB = jar/MeCab.jar

# name of input file 
INPUT_FILE = data/small-bad3.xml
#INPUT_FILE = dazai_said.xml
# name of output file 
OUTPUT_FILE = out/test-stack.xml

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
	javac -cp $(KURO):$(MECAB) src/*.java

run: 
	java -cp "$(KURO):$(MECAB):./" src/Main --analyze Kuromoji --input $(INPUT_FILE) --output $(OUTPUT_FILE)

test:
	make compile
	make run

clean: 
	$(RM) *.class

m-clean:
	rm -fr *.jar *.o *.$(LIB_EXT) *.class $(PACKAGE)/*.class
	
m-cleanall:
	rm -fr $(TARGET).java *.cxx

