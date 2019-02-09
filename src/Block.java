package src;

import java.util.LinkedList;

/**
 * class for recording information about an entry in an xml file
 */
public class Block{
  /*
   * Constants
   *
   * Below are the types of record, and
   * Names of the types
   */
  public static int NONTAG = 0;	// non-tag
  public static int HEADER = 1;	// starting with <? ending with ?>
  public static int COMMENT = 2;// start with <!-- endwing with -->
  public static int SINGLE = 3;	// start with < ending with /> 
  public static int OPEN = 4;	// start with < not with </
  public static int CLOSE = 5;	// start with </..>
  public static int HTAG = 6;	// start with &
  public static String[] TAG_NAMES = {
    "NONTAG", "HEADER", "COMMENT", "SINGLE", "OPEN", "CLOSE", "HTAG" };

  /*
   * instance variables
   */
  private int tagType;		// the type
  private String tagName;	// the name of the tag
  private String input;		// the input
  private String[] attributes;	// the attributes, can be of odd length
  private int fromLine;		// the starting line of the token
  private int toLine;		// the position of the first letter
  private int fromPosition;	// the ending line of the token
  private int toPosition;	// the position of the last letter
  /**
   * constructor
   * @param	input	the string to be parsed
   * @param	fromLine	the line position of the start of the entry
   * @param	fromPosition	the char position of the start of the entry
   * @param	endLine	the line position of the end of the entry
   * @param	endPosition	the char position of the end of the entry
   */
  Block( String input, int fromLine, int fromPosition,
      int toLine, int toPosition )
  {
    // after setting the int parameters perform analysis
    this.fromLine = fromLine;
    this.toLine = toLine;
    this.fromPosition = fromPosition;
    this.toPosition = toPosition;
    this.input = input;
    analyze( input );
  }
  /**
   * constructor
   * @param	input	the string to be parsed
   */
  Block( String input )
  {
      this( input, 0, 0, 0, 0 );
  }
  /**
   * analyze the input string and determine other values
   * @param	input	the input string
   */
  void analyze( String input )
  {
    /*
     * tentatively set the attribute array to null, and then
     * obtain the location of the first white space chracter
     */
    attributes = null;
    int p = input.indexOf( ' ' );
    /* ********************************************************
     * CASE 0 the first letter is '&' :
     * Store the entire string as the tagName
     * and set the type to HTAG
     * ******************************************************** */
    if ( input.startsWith( "&" ) )
    {
      attributes = null;
      tagType = HTAG;
      tagName = input;
    }
    /* ********************************************************
     * CASE 1 the first letter of input is not '<' :
     * Store the trimmed version of the input as the tagName
     * but set the type to NONTAG
     * ******************************************************** */
    else if ( !input.startsWith( "<" ) )
    {
      attributes = null;
      tagType = NONTAG;
      tagName = input.trim();
    }
    /* ********************************************************
     * CASE 2 the strings is "<!-- XXX -->"
     * Store the XXX as the tagName
     * and set the type to COMMENT
     * ******************************************************** */
    else if ( input.startsWith( "<!--" ) && input.endsWith( "-->" ) )
    {
      tagType = COMMENT;
      tagName = input.substring( 4, input.length() - 3 );
    }
    /* ********************************************************
     * CASE 2-dash the strings is "<!DOCTYPE XXX>"
     * Store the XXX as the tagName
     * and set the type to COMMENT
     * ******************************************************** */
    else if ( input.startsWith( "<!DOCTYPE " ) )
    {
      tagType = COMMENT;
      tagName = input.substring( 10, input.length() - 1 );
    }
    /* ********************************************************
     * CASE 3 the strings is "</XXX>"
     * Store the XXX as the tagName after trimming
     * and set the type to CLOSE
     * ******************************************************** */
    else if ( input.startsWith( "</" ) )
    {
      tagType = CLOSE;
      tagName = input.substring( 2, input.length() - 1 ).trim();
    }
    /* ********************************************************
     * CASE 4 the strings is "<?XXX, YYY?>"
     * analyze the YYY part,
     * set the type to HEADER,
     * set the tagName to XXX
     * ******************************************************** */
    else if ( input.startsWith( "<?" ) && input.endsWith( "?>" ) )
    {
      tagType = HEADER;
      tagName = input.substring( 2, p );
      analyzeCore( input.substring( p + 1, input.length() - 2 ) );
    }
    /* ********************************************************
     * CASE 5 the strings is "<XXX/>"
     * set the type to SINGLE,
     * if XXX contains ' ' then split and analyze the part after
     * ******************************************************** */
    else if ( input.endsWith( "/>" ) )
    {
      tagType = SINGLE;
      if ( p >= 0 )
      {
        tagName = input.substring( 1, p );
        analyzeCore( input.substring( p + 1, input.length() - 2 ) );
      }
      else
      {
        tagName = input.substring( 1, input.length() - 2 );
      }
    }
    /* ********************************************************
     * CASE 6 the strings is "<XXX>" and no "/", "?" or "!--"
     * set the type to OPEN,
     * if XXX contains ' ' then split and analyze the part after
     * ******************************************************** */
    else
    {
      tagType = OPEN;
      if ( p >= 0 )
      {
        tagName = input.substring( 1, p );
        analyzeCore( input.substring( p + 1 , input.length() - 1) );
      }
      else
      {
        tagName = input.substring( 1 , input.length() - 1 );
      }
    }
  }

  /**
   * analyze an attribute string
   * @param	core	the String to be analyzed
   */
  public void analyzeCore( String core )
  {
    /* 
     * Use list to store elements, later convert it to array.
     * p, q, r, and s are index values to be used for searching
     * p for the start position,
     * q the position of '=',
     * r for '"'
     * s for another '"'
     */
    LinkedList< String > list = new LinkedList< String >();
    int p = 0, q, r, s;
    while ( ( q = core.indexOf( '=', p ) ) >= 0 )
    {
      /* set q to the next '=' and
       * if it is undefined quit the loop, then:
       *   add to the list the string between p and q after trimming
       *   check the positions of the next two '"'
       *   if both are position add the substring including the '"'s
       *     and update p to the next character over
       */
      list.add( core.substring( p, q ).trim());
      r = core.indexOf( '"', q + 1 );
      s = core.indexOf( '"', r + 1 );
      if ( r < s )
      {
        list.add( core.substring( r, s + 1 ) );
      }
      p = s + 1;
    }
    /*
     * if anything remaining (possible for comments)
     * add it to the list
     */
    if ( core.substring( p ).trim().length() > 0 )
    {
      list.add( core.substring( p ).trim() );
    }
    /*
     * convert the list to an array
     */
    attributes = new String[ list.size() ];
    list.toArray( attributes );
  }
  /* *********************************************************
   * accessors, tag testers
   * ********************************************************* */
  /**
   * @return whether the token is of the NONTAG type
   */
  public boolean isNonTag()
  {
    return tagType == NONTAG;
  }
  /**
   * @return whether the token is of the HEADER type
   */
  public boolean isHeader() 
  {
    return tagType == HEADER;
  }
  /**
   * @return whether the token is of the COMMENT type
   */
  public boolean isComment()
  {
    return tagType == COMMENT;
  }
  /**
   * @return whether the token is of the SINGLE type
   */
  public boolean isSingle()
  {
    return tagType == SINGLE;
  }
  /**
   * @return whether the token is of the OPEN type
   */
  public boolean isOpen()
  {
    return tagType == OPEN;
  }
  /**
   * @return whether the token is of the CLOSE type
   */
  public boolean isClose()
  {
    return tagType == CLOSE;
  }
  /**
   * @return whether the token is an html tag
   */
  public boolean isHTag()
  {
    return tagType == HTAG;
  }
  /* *********************************************************
   * getters
   * ********************************************************* */
  /**
   * @return input
   */
  public String getInput()
  {
    return input;
  }
  /**
   * @return tag type
   */
  public int getTagType()
  {
    return tagType;
  }
  /**
   * @return tag name
   */
  public String getTagName()
  {
    return tagName;
  }
  /**
   * @return the starting line
   */
  public int getFromLine()
  {
    return fromLine;
  }
  /**
   * @return the starting position
   */
  public int getFromPosition()
  {
    return fromPosition;
  }
  /**
   * @return the end line
   */
  public int getToLine()
  {
    return toLine;
  }
  /**
   * @return the ending position
   */
  public int getToPosition()
  {
    return toPosition;
  }
  /**
   * @return the attributes as an array
   */
  public String[] getAttributes()
  {
    return attributes;
  }
  /**
   * @return the number of attributes
   */
  public int getAttributeSize()
  {
    return ( attributes != null ) ? attributes.length : 0;
  }
  /**
   * @param index	the index
   * @return the attribute at that index
   */
  public String getAttribute( int index )
  {
    return ( attributes != null && index >= 0 &&
        index < attributes.length ) ? attributes[ index ] : null;
  }
  /**
   * @param key 	the key to search for
   * @return the attribute at that index
   */
  public String getValue( String key )
  {
    if ( attributes == null )
    {
      return null;
    }
    for (int index = 0; index < attributes.length - 1; index += 2)
    {
      if ( attributes[ index ].equals( key ) && 
           attributes[ index + 1 ].startsWith( "\"" ) &&
           attributes[ index + 1 ].endsWith( "\"" ) )
      {
        return attributes[ index + 1 ].substring(
                   1, attributes[ index + 1 ].length() - 1);
      }
    }
    return null;
  }
  /*
   * toString method
   */
  @Override
  public String toString()
  {
    StringBuilder w = new StringBuilder();
    w.append( String.format( "[%d,%d,%d,%d];[%d:%s:%s]",
        fromLine, fromPosition,
        toLine, toPosition,
        tagType, TAG_NAMES[ tagType ], tagName ) );
    if ( attributes != null )
    {
      for ( int index = 0; index < attributes.length; index ++ )
      {
        w.append( ( ( index % 2 ) == 0 ) ? ";" : "=" );
        w.append( attributes[ index ]);
      }
    }
    return w.toString();
  }
}
