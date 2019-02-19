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

import org.atilika.kuromoji.Token;
import org.atilika.kuromoji.Tokenizer;
import org.atilika.kuromoji.Tokenizer.Mode;

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
 * date last modified: 1 jan 2019
 * last modified by: jerry bonnell 
 * notes: replace <ww> with <term> and <gloss> tags to make 
 *        adornment TEI-conformant
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
      "lemma", "spelled", "spoken" }; 

  // the prefix and suffix of the term tag 
  public static final String TERM_OPEN_PREFIX = "<term ";
  public static final String TERM_CLOSE = "</term>";  
  // the prefix and suffix of the gloss tag 
  public static final String GLOSS_OPEN_PREFIX = "<gloss ";
  public static final String GLOSS_CLOSE = "</gloss>"; 
  // the suffix of the open tag 
  public static final String OPEN_SUFFIX = ">";

  // the prefix of the open tag
  //public static final String SPECIAL_OPEN_PREFIX = "<ww ";
  //public static final String SPECIAL_OPEN_SUFFIX = ">";
  //public static final String SPECIAL_CLOSE = "</ww>";

  //////// INSTANCE VARIABLES ////////
  private Analyzer analyzer;          // Kuromoji tokenizer
  private String iName;			// input file name
  private String oName;			// output file name
  private BufferedReader reader;	// input file reader
  private BufferedWriter writer;	// output file writer
  private XMLProcessor parser;		// XMLProcessor
  private Iterator< Block > iter;	// Iterator of text blocks
  private int depth;			// indentation depth
  // private boolean inChoice;		// whether it is inside <choice>
  // private boolean inCorr;               // whether it is inside <corr>
  // private boolean inReg;                // whether it is inside <reg>
  // private boolean inExpan;              // whether it is inside <expan> 

  private String sentence;              // the sentence to be parsed
  private String expanToken;           
      // the portion within an <expan> tag to be parsed   
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
   */
  public Adorner( Analyzer a, String inFileName, String outFileName )
      throws IOException
  {
    analyzer = a;
    iName = inFileName;
    oName = outFileName;
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
    expanToken = "";
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
   * Generate a String representing the adornment
   * type1 = ..., type2 = ..., etc. 
   */
  // private static String tokenAttribute( Token token )
  // {
  //   String[] list = token.getAllFeatures().split( "," );
  //   String value = SPECIAL_OPEN_PREFIX;
  //   for ( int i = 0; i < list.length; i ++ )
  //   {
  //     value += ATTRIBUTES[ i ] + "=\"" + list[ i ] + "\"";
  //     value += ( i < list.length - 1 ) ? " " : SPECIAL_OPEN_SUFFIX;
  //   }
  //   return value;
  // }

  /**
   * Generates the adornment for a token using a combination of 
   * <term> and <gloss> tags in a "list" structure, and writes it to file 
   * ex:  <term n=token> 
   *         <gloss n="type1"> ... </gloss>
   *         <gloss n="type2"> ... </gloss>
   *         etc..
   *      </term> 
   *
   * @param token the input to be adorned 
   * @param features the array of attributes to adorn the input with 
   * 
   */
  private void tokenAdorn (String token, String[] features) throws IOException
  {
    System.out.println("adding term gloss tags to " + token); 
    // generates a <term> tag
    String termTag = TERM_OPEN_PREFIX + "n=\"" + token + "\"" + OPEN_SUFFIX; 
    indentAndWrite(termTag);
    // generates a <gloss> tag for each POS 
    for ( int i = 0; i < features.length; i ++ ) 
    {
      String glossTag = INDENT + GLOSS_OPEN_PREFIX + "n=\"" 
          + ATTRIBUTES[i] + "\"" + OPEN_SUFFIX + features[i] + GLOSS_CLOSE; 
      indentAndWrite(glossTag);
    }

    indentAndWrite( TERM_CLOSE );
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
      System.out.println("tokenStart : " + tokenStart[i]);
      p += analyzer.getSurfaceForm(result[ i ]).length(); 
      tokenEnd[ i ] = p;
      System.out.println("tokenEnd : " + tokenEnd[i]);
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
    boolean temp = true; 
    int i = 0; // wordIndex 
    LinkedList<String> linkedTags = new LinkedList<>(); 
    System.out.println("** expanToken  " + expanToken + "**");
    String[] expanResult = analyzer.tokenize( expanToken ); 
    System.out.println("** len of expanResult   " + expanResult.length + "**");
    for (int index = 0 ; index < expanResult.length; index++) {
      System.out.print("[" + analyzer.getSurfaceForm(expanResult[index]) + "]");
    }
    System.out.println();

    for ( bPos = 0; bPos < blockList.size(); bPos ++ )
    {
      System.out.println("===========");
      System.out.println("[ " + blockList.get(bPos) + " ]");
      System.out.println("[" + blockList.get(bPos).getTagType() + "   " + 
                          blockList.get(bPos).getTagName() + "]");
      // build a stack of current tags seen so far 
      if (blockList.get(bPos).isOpen()) {
        linkedTags.addFirst(blockList.get(bPos).getTagName());
      } else if (blockList.get(bPos).isClose()) {
        linkedTags.remove(blockList.get(bPos).getTagName());
      }
      
      if ( !blockUse.get( bPos ) ) // not doing the definition mode 
      {
        System.out.println("current chain  " + linkedTags);
        System.out.println("blockUse.get(bPos)    " + blockUse.get( bPos ));
        System.out.println("blockList.get(bPos)   " + blockList.get( bPos ));
        simpleBlockWrite( blockList.get( bPos ) );
        // typically we print out all tags that dont need definitions here, 
        // but when we see <expan> we need to do something special 
        if ((!linkedTags.isEmpty() && linkedTags.getFirst().equals("expan")
          && (
            linkedTags.indexOf("expan") == linkedTags.lastIndexOf("expan") || 
            // if piece is empty then we see a <expan> again, it might be a new
            // word 
            piece.length() == 0) 
          && blockList.get(bPos + 1).isNonTag())
          // we need to come back in if there is some residual left 
          || (residual.length() > 0 && !linkedTags.getFirst().equals("ex"))
          )
          {
          // first <expan> we seen so far 
          piece = blockList.get(bPos + 1).getTagName();
          if (residual.length() > 0) {
            System.out.println("residual > 0 true");
            if (piece.length() < residual.length()) {
              // situation where a word is segmented by multiple <ex>
              System.out.println("@@ piece < residual now");
              System.out.println("@@ piece " + piece); 
              System.out.println("@@ residual " + residual); 
              //residual = residual.substring(piece.length(), residual.length());
              System.out.println("@@ residual updated! " + residual); 
              simpleBlockWrite(new Block(piece));
              bPos++;
              continue;
            }
            simpleBlockWrite(new Block(residual));
            piece = piece.substring(residual.length(), piece.length());
            if (piece.length() == 0 && i < expanResult.length) {
              // if piece is empty and there is more to go from kuromoji then 
              // we should update it 
              bPos++; 
              piece = blockList.get(bPos + 1).getTagName(); 
              System.out.println("piece updated!  [" + piece + "]");
            }
            residual = "";
            if (!blockList.get(bPos + 1).isNonTag()) {
              // piece may become a tag like expan
              continue;
            }
          }
          if (i == expanResult.length) {
            if (piece.length() > 0) {
              simpleBlockWrite(new Block(piece));
            }
            bPos++;
            System.out.println("  >>>    continued");
            System.out.println("piece  [" + piece + "]");
            System.out.println("word   [" + word + "]");  
            System.out.println("residual   [" + residual + "]");
            System.out.println("blockList   [" + 
              blockList.get(bPos).getTagName() + "]");  
            continue;
          }
          System.out.println("piece     [" + piece + "]"); 
          word = analyzer.getSurfaceForm(expanResult[i]);
          System.out.println("word      " + word);
          // assumes that length of piece is longer than word 
          while (piece.indexOf(word) == 0) {
            // print out the explanation for word 
            String [] features = analyzer.getAllFeatures(
              expanResult[i]).split(",");
            tokenAdorn(word, features);
            piece = piece.substring(word.length(), piece.length()); 
            if (piece.length() == 0) {
              System.out.println("breaking now"); 
              break;
            }
            i++;
            word = analyzer.getSurfaceForm(expanResult[i]);
            System.out.println("## i " + i); 
            System.out.println("## expanResult[i]    " + expanResult[i]); 
            System.out.println("## piece      " + piece +  " ##");
            System.out.println("## word      " + word +  " ##");  
          }

          System.out.println("##### out of while with...");
          System.out.println("##### piece     [" + piece +  "]");
          System.out.println("##### word      [" + word +  "]");  
          // when that is no longer true, there is something segmenting the 
          // next word to parse into fragments 
          if (piece.length() > 0) {
            String [] features = analyzer.getAllFeatures(
              expanResult[i]).split(",");
            tokenAdorn(piece, features);
          }
          i++; 
          bPos++; // we don't want to print the first part (に) again 
        } else if (!linkedTags.isEmpty() 
          && (linkedTags.getFirst().equals("ex"))) {
          String chars = blockList.get(bPos + 1).getTagName(); 
          piece = piece + chars; 
          System.out.println("ex word [" + word + "]"); 
          System.out.println("ex piece [" + piece + "]"); 
          if (word.indexOf(piece) == 0) {
            residual = word.substring(piece.length(), word.length()); 
          } else if (residual.indexOf(piece) == 0) {
            // compensate for chains of <ex> tags 
            residual = residual.substring(piece.length(), residual.length()); 
          }
          System.out.println("ex piece updated   [" + piece + "]"); 
          System.out.println("ex residual updated [" + residual + "]"); 
          simpleBlockWrite( blockList.get( bPos + 1 ) );
          bPos++; 
        } 
      }
      else
      {
        while ( tPos < result.length &&
            tokenStart[ tPos ] < blockEnd.get( bPos ) )
        {
          String inp = "";
          if (bPos + 1 < blockList.size() 
            && !blockList.get(bPos + 1).isNonTag()) {
            inp = sentence.substring(
                  Math.max( tokenStart[ tPos ], blockStart.get( bPos ) ),
                  // by removing Math.min, we assume that tokenEnd[tPos] is 
                  // always smaller than blockEnd.get(bPos)
                  Math.min(tokenEnd[ tPos ], blockEnd.get(bPos)));
          } else {
            inp = sentence.substring(
                  Math.max( tokenStart[ tPos ], blockStart.get( bPos ) ),
                  // by removing Math.min, we assume that tokenEnd[tPos] is 
                  // always smaller than blockEnd.get(bPos)
                  tokenEnd[ tPos ]); 
          }
          System.out.println("inp : [" + inp + "]");
          System.out.println("********" + inp);
          System.out.println("********" + blockList.get(bPos));
          if (bPos + 1 < blockList.size()) {
            System.out.println("********" + blockList.get(bPos + 1));
          }
          System.out.println(tokenStart[ tPos ]);
          System.out.println(blockStart.get( bPos ));
          System.out.println(tokenEnd[ tPos ]);
          System.out.println(blockEnd.get( bPos ));
          if ( tokenStart[ tPos ] < blockStart.get( bPos ) )
          {
            // if blockEnd.get(bPos - 1), meaning when we were at the iteration
            // of when we observed 浚わ, >= tokenEnd[tPos], then the output 
            // of this block is not following the kuromoji order; that's why 
            // we need to continue to the next token; in other words we are 
            // recreating the Math.min() operation here 
            if (tokenEnd[ tPos ] >= blockEnd.get( bPos - 1 ) 
               && bPos - 1 >= 0 
               && blockList.get(bPos - 1).isNonTag()) {
              // we've already printed this inp so skip it, as is the case 
              // when the next character (inp) is on a newline 
              tPos++; 
              continue;
            } else {
              // if we're here, there is some residual to print out 
              System.out.println("writing w/o def  " + inp);
              indentAndWrite( inp ); // prints the character after </ex>
            }
            
          }
          else
          {
            tokenAdorn(inp, 
              analyzer.getAllFeatures(result[ tPos ]).split(",")); 
          }

          if ( tokenEnd[ tPos ] <= blockEnd.get( bPos ) )
          {
            System.out.println("tPos " + tPos + " >> " + (tPos + 1));
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
    System.out.println("tagStack  " + tagStack);
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
    if ( presentBlock.isNonTag() && !tagStack.peek().equals("choice") 
        && !tagStack.peek().equals("expan"))
    {
      comp = presentBlock.getTagName();
      System.out.println("in if - comp : " + comp);
      // we know we are inside <expan>, start building "road" to give to 
      // kuromoji 
    } else if (presentBlock.isNonTag() && tagStack.peek().equals("expan")) {
      expanToken += presentBlock.getTagName();
    } 

    blockUse.add( comp.length() > 0 );
    blockStart.add( sentence.length() );
    blockEnd.add( sentence.length() + comp.length() );
    blockList.add( presentBlock );
    System.out.println(" presentBlock   " + presentBlock);
    sentence += comp;
    System.out.println("s : " + sentence);
  }

  /**
   * process the input file
   */
  public void process() throws IOException
  {
    // skip until <text> is encountered
    while ( iter.hasNext() )
    {
      presentBlock = iter.next();
      ///// System.out.println( presentBlock );
      if ( presentBlock.isOpen() &&
           presentBlock.getTagName().equals( "text" ) )
      {
        break;
      }
      // moved here due to apperance of a duplicate <text> tag  
      simpleBlockWrite( presentBlock ); 
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
        System.out.println("parse sentence called from if");
        parseSentence();
      }

      presentBlock = residual;
    }
    System.out.println("parse sentence called");
    parseSentence();	// process any remaining blocks
    reader.close();
    writer.flush();
    writer.close();
  }
}
