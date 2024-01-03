
import java.util.regex.PatternSyntaxException;

/**
 *
 * Scanner class, generated by JFlex.
 * Function nextToken is the important one as it reads the file and returns the next matched toke.
 *
 */

@SuppressWarnings({"javadoc","fallthrough"}) /* Not working, though... */
%%// Options of the scanner

%class LexicalAnalyzer // Name
%unicode               // Use unicode
%line                  // Use line counter (yyline variable)
%column                // Use character counter by line (yycolumn variable)
%function nextToken
%type Symbol
%yylexthrow PatternSyntaxException
%no_suppress_warnings   // Removes the default @SuppressWarnings("fallthrough") so that the custom one can be added above

%eofval{
	return new Symbol(LexicalUnit.EOS, yyline, yycolumn);
%eofval}

//Extended Regular Expressions

AlphaUpperCase    = [A-Z]
AlphaLowerCase    = [a-z]
Alpha             = {AlphaUpperCase}|{AlphaLowerCase}
Numeric           = [0-9]
AlphaNumeric      = {Alpha}|{Numeric}
// LowerAlphaNumeric = {AlphaLowerCase}|{Numeric}

BadInteger     = (0[0-9]+)
Integer        = ([1-9][0-9]*)|0
VarName        = ({AlphaLowerCase})({AlphaNumeric})*
LineFeed       = "\n"
CarriageReturn = "\r"
EndLine        = ({LineFeed}{CarriageReturn}?) | ({CarriageReturn}{LineFeed}?)
Space          = (\t | \f | " ")
Spaces         = {Space}+
Separator      = ({Spaces}) | ({EndLine}) // Was Space (no 's') was it a typo? (did produce a warning)
Any            = ([^"\n""\r"])*
UpToEnd        = ({Any}{EndLine}) | ({EndLine})

//Declare exclusive states
%xstate YYINITIAL, SHORTCOMMENTS, LONGCOMMENTS

%%// Identification of tokens


<LONGCOMMENTS> {
// End of comment
	"''"			{yybegin(YYINITIAL);} // go back to analysis
  <<EOF>>          {throw new PatternSyntaxException("A comment is never closed.",yytext(),yyline);}
	[^]					     {} //ignore any character
}

<YYINITIAL> {
// Comments
    "''"              {yybegin(LONGCOMMENTS);} // go to ignore mode
    "**"{UpToEnd}     {} // go to ignore mode
// Code delimiters
  "begin"             {return new Symbol(LexicalUnit.BEG, yyline, yycolumn, yytext());}
  "end"               {return new Symbol(LexicalUnit.END, yyline, yycolumn, yytext());}
  "..."               {return new Symbol(LexicalUnit.DOTS, yyline, yycolumn, yytext());}
// Assignation
  ":="                {return new Symbol(LexicalUnit.ASSIGN, yyline, yycolumn, yytext());}
// Parenthesis
  "("                 {return new Symbol(LexicalUnit.LPAREN, yyline, yycolumn, yytext());}
  ")"                 {return new Symbol(LexicalUnit.RPAREN, yyline, yycolumn, yytext());}
// Brackets
  "{"                 {return new Symbol(LexicalUnit.LBRACK, yyline, yycolumn, yytext());}
  "}"                 {return new Symbol(LexicalUnit.RBRACK, yyline, yycolumn, yytext());}
// Arithmetic signs
  "+"                 {return new Symbol(LexicalUnit.PLUS, yyline, yycolumn, yytext());}
  "-"                 {return new Symbol(LexicalUnit.MINUS, yyline, yycolumn, yytext());}
  "*"                 {return new Symbol(LexicalUnit.TIMES, yyline, yycolumn, yytext());}
  "/"                 {return new Symbol(LexicalUnit.DIVIDE, yyline, yycolumn, yytext());}
// Logical operators
  "and"               {return new Symbol(LexicalUnit.AND, yyline, yycolumn, yytext());}
  "or"                {return new Symbol(LexicalUnit.OR, yyline, yycolumn, yytext());}
// Conditional keywords
  "if"                {return new Symbol(LexicalUnit.IF, yyline, yycolumn, yytext());}
  "then"              {return new Symbol(LexicalUnit.THEN, yyline, yycolumn, yytext());}
  "else"              {return new Symbol(LexicalUnit.ELSE, yyline, yycolumn, yytext());}
// Loop keywords
  "while"             {return new Symbol(LexicalUnit.WHILE, yyline, yycolumn, yytext());}
  "do"                {return new Symbol(LexicalUnit.DO, yyline, yycolumn, yytext());}
// Comparison operators
  "="                 {return new Symbol(LexicalUnit.EQUAL, yyline, yycolumn, yytext());}
  "<"                 {return new Symbol(LexicalUnit.SMALLER, yyline, yycolumn, yytext());}
// IO keywords
  "print"             {return new Symbol(LexicalUnit.PRINT, yyline, yycolumn, yytext());}
  "read"              {return new Symbol(LexicalUnit.READ, yyline, yycolumn, yytext());}
// Numbers
  {BadInteger}        {System.err.println("Warning! Numbers with leading zeros are deprecated: " + yytext()); return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, Integer.valueOf(yytext()));}
  {Integer}           {return new Symbol(LexicalUnit.NUMBER, yyline, yycolumn, Integer.valueOf(yytext()));}
  {VarName}           {return new Symbol(LexicalUnit.VARNAME,yyline, yycolumn,yytext());}
  {Separator}         {}// ignore spaces
  [^]                 {throw new PatternSyntaxException("Unmatched token, out of symbols",yytext(),yyline);} // unmatched token gives an error
}
