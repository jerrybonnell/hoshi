package src;

/**
 * Copyright © 2010-2012 Atilika Inc.  All rights reserved.
 *
 * See the NOTICE.txt file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Atilika Inc. licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  A copy of the License is distributed with this work in the
 * LICENSE.txt file.  You may also obtain a copy of the License from
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * Adapted by MO
 */
// package org.atilika.kuromoji.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;

//import org.atilika.kuromoji.Token;
//import org.atilika.kuromoji.Tokenizer;
//import org.atilika.kuromoji.Tokenizer.Mode;

// import org.chasen.mecab.MeCab;
// import org.chasen.mecab.Tagger;
// import org.chasen.mecab.Model;
// import org.chasen.mecab.Lattice;
// import org.chasen.mecab.Node;

public class Main
{

  // static {
  //   try {
  //      System.loadLibrary("MeCab");
  //   } catch (UnsatisfiedLinkError e) {
  //      System.err.println("Cannot load the example native code.\nMake sure your LD_LIBRARY_PATH contains \'.\'\n" + e);
  //      System.exit(1);
  //   }
  // }

  public static final int MARU = 12290;

  public static void error()
  {
    System.out.println(
        "Japanese Morphological Adorner for TEI XML -- " +
        "github.com/jerrybonnell/hoshi\n" +
        "usage: --analyze ANALYZER " +
        " --input FILE --output FILE [--model XML MODEL]"
        );
    System.exit( 1 );
  }

  public static void main( String[] args ) throws IOException
  {
    Analyzer analyzer = null;
    String aName = null;
    String inputName = null, outputName = null, modelName = null;
    for ( int i = 0; i < args.length; i += 2 )
    {
      switch ( args[ i ] )
      {
        case "--analyze": aName = args[ i + 1 ]; break;
        case "--input": inputName = args[ i + 1 ]; break;
        case "--output": outputName = args[ i + 1 ]; break;
        case "--model": modelName = args[ i + 1 ]; break;
        default: error();
      }
    }
    if ( inputName == null || outputName == null || aName == null )
    {
      error();
    }
    if (modelName == null) {
      modelName = "../schema/tei_pos.rnc"; // default location
    }

    if (aName.equals("MeCab")) {
      analyzer = new MecabAnalyzer();
    } else if (aName.equals("Kuromoji")) {
      analyzer = new KuroAnalyzer();
    } else if (aName.equals("Kagome")) {
      analyzer = new KagomeAnalyzer();
    } else {
      error();
    }
    // System.out.println(analyzer);
    Adorner ad = new Adorner(analyzer, inputName, outputName, modelName);
    ad.process();
  }
}
