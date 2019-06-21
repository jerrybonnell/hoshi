#
# A makefile for interfacing with hoshi
#

# location of the kuromoji jar files
KURO = jar/kuromoji-ipadic-0.9.0.jar
KURO_CORE = jar/kuromoji-core-0.9.0.jar
# location of the mecab jar file
MECAB = jar/MeCab.jar

# name of input file
INPUT_FILE = data/demo.xml
# name of output file
OUTPUT_FILE = out/out.xml
# select the parser to use (Kuromoji, MeCab, Kagome)
PARSER = Kuromoji
# specify the location of the custom schema
# (here, relative to the out/ directory)
XML_MODEL = "../schema/tei_pos.rnc"

JAR_FILE = "hoshi-ipadic.jar"

JCC = javac -cp "$(KURO):./"  -encoding UTF-8

# the -g flag compiles with debugging information
#
JFLAGS = -g

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

# Mecab targets

IPADIC_TARGET="https://drive.google.com/uc?export=download&id=0B4y35FiV1wh7MWVlSDBCSXZMTXM"
TAR_NAME=mecab-ipadic.tar.gz
UNZIPPED_NAME=mecab-ipadic-2.7.0-20070801
MECAB_DICT_INDEX=/usr/local/libexec/mecab/mecab-dict-index

build-dict:
	wget -O $(TAR_NAME) $(IPADIC_TARGET)
	tar zxfv $(TAR_NAME)
	cd $(UNZIPPED_NAME) && ./configure && make && sudo make install

change-to-utf:
	cd $(UNZIPPED_NAME) && $(MECAB_DICT_INDEX) -f euc-jp -t utf-8
	cd $(UNZIPPED_NAME) && sudo make install

# target for compiling the application

compile:
	$(CXX) -O3 -c -fpic wrappers/$(TARGET)_wrap.cxx $(INC)
	$(CXX) $(LIB_TYPE) $(TARGET)_wrap.o -o \
		lib$(TARGET).$(LIB_EXT) $(LIBS)
	javac -cp $(KURO_CORE):$(KURO):$(MECAB) src/*.java

# target for running the application

run:
	java -cp "$(KURO_CORE):$(KURO):$(MECAB):./" src/Main \
		--analyze $(PARSER) --input $(INPUT_FILE) \
		--output $(OUTPUT_FILE) --model $(XML_MODEL)

# build jar
# build-jar:
# 	javac -cp $(KURO_CORE):$(KURO):$(MECAB) src/*.java \
# 		src/Main.java
# 	jar -cvfe $(JAR_FILE) src/Main src/

# target for cleaning (removing) all class files generated

clean:
	rm src/*.class

