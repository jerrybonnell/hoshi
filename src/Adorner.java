package src;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.LinkedList;


/**
 * The class for adorning a Japanese xml file using Kuromoji
 * 1. It will adorn only the area inside <text>
 * 2. The sentence ends must be MARU
 * 3. For <choice> use only <corr>
 * 4. The adornment appears in the regular (meaning, non-tag) block where
 *    the word starts (there are cases in which two components of a word
 *    are separted by a tag.
 * 5. All the tags will be preserved.
 *
 *
 *
 */
public class Adorner
{
  //////// CONSTANTS ////////
  // the value for the "Kuten" "MARU"
  public static final int MARU = 12290;

  // the size of indentation
  public static final String INDENT = "    ";

  // the 9 possible attribbutes
  public static final String[] ATTRIBUTES = {
      "type1", "type2", "type3", "type4", "number", "rule",
      "root", "spelled", "spoken" };

  // the prefix of the open tag
  public static final String SPECIAL_OPEN_PREFIX = "<w ";
  public static final String SPECIAL_OPEN_SUFFIX = ">";
  public static final String SPECIAL_CLOSE = "</w>";

  // the prefix and suffix of the term tag
  public static final String TERM_OPEN_PREFIX = "<term ";
  public static final String TERM_CLOSE = "</term>";
  // the prefix and suffix of the gloss tag
  public static final String GLOSS_OPEN_PREFIX = "<gloss ";
  public static final String GLOSS_CLOSE = "</gloss>";
  // the suffix of the open tag
  public static final String OPEN_SUFFIX = ">";

  // verbose mode
  public static final boolean VERBOSE = false;

  //////// INSTANCE VARIABLES ////////
  private Analyzer analyzer;          // Kuromoji tokenizer
  private String iName;			// input file name
  private String oName;			// output file name
  private String mName;     // location of XML schema model
  private BufferedReader reader;	// input file reader
  private BufferedWriter writer;	// output file writer
  private XMLProcessor parser;		// XMLProcessor
  private Iterator< Block > iter;	// Iterator of text blocks
  private int depth;			// indentation depth

  private String sentence;              // the sentence to be parsed

  private Block presentBlock;           // the Block being processed

  private ArrayList< Block > blockList; // the blocks forming the sentence

  private ArrayList< Integer > blockStart;
      // the character starting position of the tag word in the sentence
  private ArrayList< Integer > blockEnd;
      // the character ending position of the tag word in the sentence
  private ArrayList< Boolean > blockUse;
      // whether the tag word is used as part of the sentence
  private Stack< String > tagStack;

  /**
   * @constructor
   * @param	analyzer	the Tokenizer object
   * @param	inFileName	the input file name
   * @param	outFileName	the output file name
   * @param modelName the XML model name/location
   */
  public Adorner( Analyzer a, String inFileName, String outFileName,
                  String modelName )
      throws IOException
  {
    analyzer = a;
    iName = inFileName;
    oName = outFileName;
    mName = "<?xml-model href=\"" + modelName + "\"" +
    " type=\"application/relax-ng-compact-syntax\"?>";
    reader = new BufferedReader( new InputStreamReader(
        new FileInputStream( iName ), "UTF-8") );
    writer = new BufferedWriter( new OutputStreamWriter(
        new FileOutputStream( oName ), "UTF-8") );

    parser = new XMLProcessor( reader );
    iter = parser.iterator();
    tagStack = new Stack< String >();
    tagStack.push("@"); // dummy push

    cleanBuffer();
    depth = 0;
  }

  /**
   * clean the components of the buffer
   */
  private void cleanBuffer()
  {
    presentBlock = null;
    blockList = new ArrayList< Block >();
    blockStart = new ArrayList< Integer >();
    blockEnd = new ArrayList< Integer >();
    blockUse = new ArrayList< Boolean >();
    sentence = "";
    //expanToken = "";
  }

  /**
   * @return if an input string contains a MARU
   * @param	w	the input
   */
  private int findMaru( String w )
  {
    for ( int i = 0; i < w.length(); i ++ )
    {
      if ( (int)( w.charAt( i ) ) == MARU )
      {
        return i;
      }
    }
    return -1;
  }

  /**
   * @return if a block is a nontag containing a MARU
   * @param	w	the input
   */

  private int findMaru( Block w )
  {
    return ( !w.isNonTag() || tagStack.peek().equals("choice")
      && !tagStack.peek().equals("choice") ) ? -1 : findMaru( w.getTagName() );
  }

  /**
   * produce indentation
   */
  private void indent() throws IOException
  {
    for ( int i = 1; i <= depth; i ++ )
    {
      writer.append( INDENT );
    }
  }

  /**
   * Generate a String representing the adornment and print it
   * type1 = ..., type2 = ..., etc.
   */
  private void tokenAttribute( String token, String[] features,
      LinkedList<String> stack ) throws IOException
  {
    if (VERBOSE) System.out.println("adding w tags to " + token);
    String value = (!stack.isEmpty()
      && stack.getFirst().equals("w")) ? " " : SPECIAL_OPEN_PREFIX;
    for ( int i = 0; i < features.length; i ++ )
    {
      value += ATTRIBUTES[ i ] + "=\"" + features[ i ] + "\"";
      value += ( i < features.length - 1 ) ? " " : SPECIAL_OPEN_SUFFIX;
    }

    if (!stack.isEmpty() && stack.getFirst().equals("w")) {
      writer.append( value + "\n" );
      depth--;
      indentAndWrite(token);
      depth++;
    } else {
      indentAndWrite(value);
      indentAndWrite(token);
      indentAndWrite( SPECIAL_CLOSE );
    }
  }

  /**
   * Generate a String representing the adornment and print it
   * token= ..., type1 = ..., type2 = ..., etc.
   */
  private void tokenAttribute (String token, String[] features,
    LinkedList<String> stack, String original) throws IOException
  {
    if(VERBOSE) System.out.println("add SPECIAL w tag to " + token);

    String value = (!stack.isEmpty() &&
      stack.getFirst().equals("w")) ? " " : SPECIAL_OPEN_PREFIX;
    value += "token"+ "=\"" + original + "\" ";
    for ( int i = 0; i < features.length; i ++ )
    {
      value += ATTRIBUTES[ i ] + "=\"" + features[ i ] + "\"";
      value += ( i < features.length - 1 ) ? " " : SPECIAL_OPEN_SUFFIX;
    }

    if (!stack.isEmpty() && stack.getFirst().equals("w")) {
      writer.append( value + "\n" );
      depth--;
      indentAndWrite(token);
      depth++;
    } else {
      indentAndWrite(value);
      indentAndWrite(token);
      indentAndWrite( SPECIAL_CLOSE );
    }
  }

  /**
   * print a block in the output stream
   * @param	block	the block
   *
   * presently, only presentBlock is used as the actual;
   * parameter
   */
  private void simpleBlockWrite( Block block ) throws IOException
  {
    /* if the block is a close tag, decrease the depth by 1
     * indent, and then print the block
     * if the block is an open increase the depth by 1
     */
    if ( block.getTagType() == Block.CLOSE )
    {
      depth --;
    }
    indentAndWrite( block.getInput() );
    if ( block.getTagType() == Block.OPEN )
    {
      depth ++;
    }
  }

  /**
   * print a block in the output stream
   * @param block the block
   *
   * presently, only presentBlock is used as the actual;
   * parameter
   */
  private void simpleBlockWrite( Block block, boolean isW) throws IOException
  {
    /* if the block is a close tag, decrease the depth by 1
     * indent, and then print the block
     * if the block is an open increase the depth by 1
     */
    if ( block.getTagType() == Block.CLOSE )
    {
      depth --;
    }
    if (isW) {
      String a = block.getInput().substring(0, block.getInput().length() - 1);
      indent();
      writer.append( a );
    } else {
      indentAndWrite( block.getInput() );
    }
    if ( block.getTagType() == Block.OPEN )
    {
      depth ++;
    }
  }

  /**
   * produce indentation and then write the string with
   * a newline
   * @param	a	the string to be printed
   */
  private void indentAndWrite( String a ) throws IOException
  {
    indent();
    writer.append( a + "\n" );
  }

  /**
   * generate parsing
   * When this method is invoked:
   *
   * .  sentence is the String to be parsed
   *
   * .  blockList is the series of Block objects
   *    whose contents ( the values of the String
   *    field variable "input", which can be obtained
   *    with method "getInput" ) for the sentence
   *
   * .  blockUse is the series of Boolean indicating whether
   *    its corresponding Block objects are used in forming
   *    sentence
   *
   * .  blockStart and blockEnd are the start and end
   *    positions of of the contributing Blocks objects
   */
  private void parseSentence() throws IOException
  {
    // if the blockList has size 0, there is nothing to do
    if ( blockList.size() == 0 )
    {
      return;
    }

    // print, on the screen, the sentence to parse
    System.out.println( "----------" + sentence );

    /* call the tokenizer
     * produce the outcome on the screen
     * convert the list to an array
     * create arrays for storing the start and end
     * positions of the token in "sentence"
     */

    String[] result = analyzer.tokenize( sentence );
    for ( String line : result )
    {
      System.out.println( analyzer.getSurfaceForm(line) + "\t"
          + analyzer.getAllFeatures(line) );
    }

    int[] tokenStart = new int[ result.length ];
    int[] tokenEnd = new int[ result.length ];
    int p = 0;
    for ( int i = 0; i < result.length; i++ )
    {
      tokenStart[ i ] = p;
      if (VERBOSE) System.out.println("tokenStart : " + tokenStart[i]);
      p += analyzer.getSurfaceForm(result[ i ]).length();
      tokenEnd[ i ] = p;
      if (VERBOSE) System.out.println("tokenEnd : " + tokenEnd[i]);
    }

    /* double-loop using the indexes to the blocks, "bPos", and
     * to the tokens, "tPos"
     *
     * Here is the plan:
     *
     * Each token has its start and end (start < end) on "sentence",
     * represented as tokenStart[ tPos ] and tokenEnd[ tPos ]
     *
     * Each block has its start and end (start <= end) on "sentence",
     * represented as blockStart.get( bPos ) and blockEnd.get( bPos )
     *
     * tokenStart[ 0 ] == blockStart.get( 0 ) == 0
     *
     * The interior loop invariant is:
     *     tokenStart[ tPos ] <= blockEnd.get( bPos )
     * If this is no longer met, bPos is increased by 1
     * to move on to the next Block object on the list.
     *
     * The method prints the portion covered by both
     * the Block object and the token
     * In addition, if the token's start position is >=
     * Block's start position, it means that this is the
     * first time output is generated for the token, so
     * the method prints the attributes.
     *
     * After that, if the end position of the token is <=
     * the end position of the Block, move on to the next
     * token; otherwise, move on to the next Block object
     * by breaking the internal loop.
     */

    int bPos = 0;
    int tPos = 0;
    String piece = "";
    String word = "";
    String residual = "";
    int i = 0; // wordIndex
    LinkedList<String> linkedTags = new LinkedList<>();
    //System.out.println("** expanToken  " + expanToken + "**");
    //String[] expanResult = analyzer.tokenize( expanToken );
    //System.out.println("** len of expanResult   " + expanResult.length + "**");
    /*for (int index = 0 ; index < expanResult.length; index++) {
      System.out.print("[" + analyzer.getSurfaceForm(expanResult[index]) + "]");
    }*/
    System.out.println();

    for ( bPos = 0; bPos < blockList.size(); bPos ++ )
    {
      if (VERBOSE) {
        System.out.println("===========");
        System.out.println("[ " + blockList.get(bPos) + " ]");
        System.out.println("[" + blockList.get(bPos).getTagType()
          + "   " + blockList.get(bPos).getTagName() + "]");
      }

      // build a stack of current tags seen so far
      if (blockList.get(bPos).isOpen()) {
        linkedTags.addFirst(blockList.get(bPos).getTagName());
      } else if (blockList.get(bPos).isClose()) {
        linkedTags.remove(blockList.get(bPos).getTagName());
      }

      if (VERBOSE) {
      System.out.println("current chain  " + linkedTags);
      System.out.println("blockUse.get(bPos)    "
        + blockUse.get( bPos ));
      System.out.println("blockList.get(bPos)   "
        + blockList.get( bPos ));
      }

      if ( !blockUse.get( bPos ) ) // not doing the definition mode
      {
        if (!linkedTags.isEmpty() && linkedTags.getFirst().equals("w")) {
          simpleBlockWrite( blockList.get( bPos ), true);
        } else {
          simpleBlockWrite( blockList.get( bPos ) );
        }
      }
      else
      {
        // there are words that haven't been adorned yet
        while ( tPos < result.length &&
            tokenStart[ tPos ] < blockEnd.get( bPos ) )
        {
          String inp = "";
          inp = sentence.substring(
                  Math.max( tokenStart[ tPos ], blockStart.get( bPos ) ),
                  Math.min(tokenEnd[ tPos ], blockEnd.get(bPos)));

          if (VERBOSE) {
            System.out.println("inp : [" + inp + "]");
            System.out.println("********" + inp);
            System.out.println("********" + blockList.get(bPos));

            System.out.println(tokenStart[ tPos ]);
            System.out.println(blockStart.get( bPos ));
            System.out.println(tokenEnd[ tPos ]);
            System.out.println(blockEnd.get( bPos ));
          }
          // if the block we're looking at is currently ahead of the current
          // token being processed, then this is a segmented token so don't
          // apply any adorning
          if ( tokenStart[ tPos ] < blockStart.get( bPos ) )
          {
            /*if (bPos - 1 >= 0 && blockList.get(bPos - 1).isNonTag()) {
              // we've already printed this inp so skip it, as is the case
              // when the next character (inp) is on a newline
              indentAndWrite( inp ); // prints the character after </ex>
              //tPos++;
              //continue;
            }
            else {
              // if we're here, there is some residual to print out
              System.out.println("writing w/o def  " + inp);
              indentAndWrite( inp ); // prints the character after </ex>

            }*/
            indentAndWrite( inp );
          }
          else
          {
            // we compare temp to kuro in the case that the token is split,
            // e.g. segmentation by <ex>,<choice> or by newline
            String temp = sentence.substring(
                  Math.max( tokenStart[ tPos ], blockStart.get( bPos ) ),
                  Math.min(tokenEnd[ tPos ], blockEnd.get(bPos)));
            if (VERBOSE) System.out.println("temp " + temp);
            String kuro = sentence.substring(
                  Math.max( tokenStart[ tPos ], blockStart.get( bPos ) ),
                  tokenEnd[ tPos ]);
            if (VERBOSE) System.out.println("kuro " + kuro);

            if (!temp.equals(kuro)) //&&
              // additional case is if next block happens to be a non-tag, as
              // in the case of small-split.xml
             // bPos + 1 < blockList.size() &&!blockList.get(bPos + 1).isNonTag())
            {
              tokenAttribute(inp,
                analyzer.getAllFeatures(result[ tPos ]).split(","),
                linkedTags, kuro);
            } else
            {
              tokenAttribute(inp,
                analyzer.getAllFeatures(result[ tPos ]).split(","),
                linkedTags);
            }

          }

          if ( tokenEnd[ tPos ] <= blockEnd.get( bPos ) )
          {
            if (VERBOSE) {
              System.out.println("tPos " + tPos + " >> " + (tPos + 1));
            }
            tPos ++;
          }
          else
          {
            break;
          }
        }
      }
    }
    cleanBuffer();
  }

  /**
   * @return whether the tag forces parsing
   */
  private boolean trigger()
  {
    return presentBlock.isClose() &&
        presentBlock.getTagName().equals( "l" );
  }

  /**
   * incorporate the present block
   * the present block is expected to not have a MARU in the middle
   */
  private void incorporate()
  {
    if (VERBOSE) {
      System.out.println("blockList " + blockList);
      System.out.println("tagStack  " + tagStack);
    }
    /////  update choice and corr
    if (presentBlock.isOpen()
      && presentBlock.getTagName().equals( "choice" ))
    {
      tagStack.push(presentBlock.getTagName()); // push "choice"
    }
    else if (presentBlock.isOpen()
      && presentBlock.getTagName().equals("corr"))
    {
      tagStack.push(presentBlock.getTagName());
    }
    else if (presentBlock.isClose()
      && presentBlock.getTagName().equals("corr"))
    {
      tagStack.pop();
    }
    else if (presentBlock.isOpen()
      && presentBlock.getTagName().equals("reg"))
    {
      tagStack.push(presentBlock.getTagName());
    }
    else if (presentBlock.isClose()
      && presentBlock.getTagName().equals("reg"))
    {
      tagStack.pop();
    }
    else if (presentBlock.isOpen()
      && presentBlock.getTagName().equals("expan"))
    {
      tagStack.push(presentBlock.getTagName());
    }
    else if (presentBlock.isClose()
      && presentBlock.getTagName().equals("expan"))
    {
      tagStack.pop();
    }
    else if (presentBlock.isClose()
      && presentBlock.getTagName().equals("choice"))
    {
      tagStack.pop();
    }

    String comp = "";
    // not equal to expan is important because otherwise kuromoji will eat
    // those characters
    if (presentBlock.isNonTag() && tagStack.peek().equals("corr")
        && tagStack.contains("expan"))
    {
      //expanToken += presentBlock.getTagName();
      comp += presentBlock.getTagName();
    } else if ( presentBlock.isNonTag() && !tagStack.peek().equals("choice")
        && !tagStack.peek().equals("expan"))
    {
      comp = presentBlock.getTagName();
      if (VERBOSE) System.out.println("in if - comp : " + comp);
      // we know we are inside <expan>, start building "road" to give to
      // kuromoji
    } else if (presentBlock.isNonTag() && tagStack.peek().equals("expan")) {
      //expanToken += presentBlock.getTagName();
      comp += presentBlock.getTagName();
    }

    blockUse.add( comp.length() > 0 );
    blockStart.add( sentence.length() );
    blockEnd.add( sentence.length() + comp.length() );
    blockList.add( presentBlock );
    //System.out.println(" presentBlock   " + presentBlock);
    sentence += comp;
    //System.out.println("s : " + sentence);
    if (VERBOSE) System.out.println(blockList);
  }

  /**
   * process the input file
   */
  public void process() throws IOException
  {
    boolean addedSchema = false;
    // skip until <text> is encountered
    while ( iter.hasNext() )
    {
      presentBlock = iter.next();
      if ( presentBlock.isOpen() &&
           presentBlock.getTagName().equals( "text" ) )
      {
        break;
      }
      // moved here due to apperance of a duplicate <text> tag
      if ( presentBlock.isHeader() && !addedSchema &&
           presentBlock.getTagName().equals( "xml-model" ) )
      {
        // write the appropriate xml model for our schema
        simpleBlockWrite(new Block( mName ) );
        addedSchema = true;
      } else if (!presentBlock.getTagName().equals( "xml-model" ) ) {
        simpleBlockWrite( presentBlock );
      }
    }

    System.out.println( "---------------START---------------" );
    int i = 0;
    while ( true )
    {
      i += 1;
      if ( presentBlock == null && iter.hasNext() )
      {
        presentBlock = iter.next();
      }
      if ( presentBlock == null )
      {
        break;
      }
      // if the block contains a MARU
      // split the input into two parts at the first MARU
      // push the first part into the buffer and process
      // and then redefine presentBlock with the second part
      //   if the part is not empty
      Block residual = null;
      int pos = findMaru( presentBlock );
      // there is maru in this line
      if ( pos >= 0 )
      {
        String base = presentBlock.getTagName();
        String pre = base.substring( 0, pos + 1 );
        String post = base.substring( pos + 1 );
        if ( post.length() > 0 ) {
          //System.out.println(" !!  >> " + presentBlock.getInput());
          residual = new Block( post );
          // necessary to update; otherwise duplicate blocks will appear
          presentBlock = new Block( pre );
          //System.out.println(" CHANGED!!  >> " + presentBlock.getInput());
          //System.out.println(" RESIDUAL! >> " + residual);
        } else {
          residual = null;
        }
        //residual = ( post.length() > 0 ) ? new Block( post ) : null;
      }
      incorporate();
      if ( pos >= 0 || trigger() )
      {
        if (VERBOSE) System.out.println("parse sentence called from if");
        parseSentence();
      }

      presentBlock = residual;
    }
    if (VERBOSE) System.out.println("parse sentence called");
    parseSentence();	// process any remaining blocks
    reader.close();
    writer.flush();
    writer.close();
  }
}
