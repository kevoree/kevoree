grammar EsperEPL2Grammar;

options
{
	k = 4;
	output = AST;
	ASTLabelType=CommonTree;
}

// language tokens
// Declare all tokens here and add to the "parserTokenParaphases" map below.
tokens
{
	CREATE='create';
	WINDOW='window';
	IN_SET='in';
	BETWEEN='between';
	LIKE='like';
	REGEXP='regexp';
	ESCAPE='escape';
	OR_EXPR='or';
	AND_EXPR='and';
	NOT_EXPR='not';
    	EVERY_EXPR='every';
    	EVERY_DISTINCT_EXPR='every-distinct';
	WHERE='where';
	AS='as';	
	SUM='sum';
	AVG='avg';
	MAX='max';
	MIN='min';
	COALESCE='coalesce';
	MEDIAN='median';
	STDDEV='stddev';
	AVEDEV='avedev';
	COUNT='count';
	SELECT='select';
	CASE='case';
   	CASE2;
	ELSE='else';
	WHEN='when';
	THEN='then';
	END='end';
	FROM='from';
	OUTER='outer';
	INNER='inner';
	JOIN='join';
	LEFT='left';
	RIGHT='right';
	FULL='full';
	ON='on';	
	IS='is';
	BY='by';
	GROUP='group';
	HAVING='having';
	DISTINCT='distinct';
	ALL='all';
	ANY='any';
	SOME='some';
	OUTPUT='output';
	EVENTS='events';
	FIRST='first';
	LAST='last';
	INSERT='insert';
	INTO='into';
	ORDER='order';
	ASC='asc';
	DESC='desc';
	RSTREAM='rstream';
	ISTREAM='istream';
	IRSTREAM='irstream';
	SCHEMA='schema';
	UNIDIRECTIONAL='unidirectional';
	RETAINUNION='retain-union';
	RETAININTERSECTION='retain-intersection';
	PATTERN='pattern';
	SQL='sql';
	METADATASQL='metadatasql';
	PREVIOUS='prev';
	PREVIOUSTAIL='prevtail';
	PREVIOUSCOUNT='prevcount';
	PREVIOUSWINDOW='prevwindow';
	PRIOR='prior';
	EXISTS='exists';
	WEEKDAY='weekday';
	LW='lastweekday';
	INSTANCEOF='instanceof';
	TYPEOF='typeof';
	CAST='cast';
	CURRENT_TIMESTAMP='current_timestamp';
	DELETE='delete';
	SNAPSHOT='snapshot';
	SET='set';
	VARIABLE='variable';
	UNTIL='until';
	AT='at';
	INDEX='index';
	TIMEPERIOD_YEAR='year';
	TIMEPERIOD_YEARS='years';
	TIMEPERIOD_MONTH='month';
	TIMEPERIOD_MONTHS='months';
	TIMEPERIOD_WEEK='week';
	TIMEPERIOD_WEEKS='weeks';
	TIMEPERIOD_DAY='day';
	TIMEPERIOD_DAYS='days';
	TIMEPERIOD_HOUR='hour';
	TIMEPERIOD_HOURS='hours';
	TIMEPERIOD_MINUTE='minute';
	TIMEPERIOD_MINUTES='minutes';
	TIMEPERIOD_SEC='sec';
	TIMEPERIOD_SECOND='second';
	TIMEPERIOD_SECONDS='seconds';	
	TIMEPERIOD_MILLISEC='msec';
	TIMEPERIOD_MILLISECOND='millisecond';
	TIMEPERIOD_MILLISECONDS='milliseconds';
	BOOLEAN_TRUE='true';
	BOOLEAN_FALSE='false';
	VALUE_NULL='null';
	ROW_LIMIT_EXPR='limit';
	OFFSET='offset';
	UPDATE='update';
	MATCH_RECOGNIZE='match_recognize';
	MEASURES='measures';
	DEFINE='define';
	PARTITION='partition';
	MATCHES='matches';
	AFTER='after';	
	FOR='for';	
	WHILE='while';	
	USING='using';
	MERGE='merge';
	MATCHED='matched';
	
   	NUMERIC_PARAM_RANGE;
   	NUMERIC_PARAM_LIST;
   	NUMERIC_PARAM_FREQUENCY;   	
   	OBJECT_PARAM_ORDERED_EXPR;
   	FOLLOWED_BY_EXPR;
   	FOLLOWED_BY_ITEM;
   	ARRAY_PARAM_LIST;
   	PATTERN_FILTER_EXPR;
   	PATTERN_NOT_EXPR;
   	PATTERN_EVERY_DISTINCT_EXPR;
   	EVENT_FILTER_EXPR;
   	EVENT_FILTER_PROPERTY_EXPR;
   	EVENT_FILTER_PROPERTY_EXPR_ATOM;
   	PROPERTY_SELECTION_ELEMENT_EXPR;
   	PROPERTY_SELECTION_STREAM;
   	PROPERTY_WILDCARD_SELECT;
   	EVENT_FILTER_IDENT;
   	EVENT_FILTER_PARAM;
   	EVENT_FILTER_RANGE;
   	EVENT_FILTER_NOT_RANGE;
   	EVENT_FILTER_IN;
   	EVENT_FILTER_NOT_IN;
   	EVENT_FILTER_BETWEEN;
   	EVENT_FILTER_NOT_BETWEEN;
   	CLASS_IDENT;
   	GUARD_EXPR;
   	OBSERVER_EXPR;
   	VIEW_EXPR;
   	PATTERN_INCL_EXPR;
   	DATABASE_JOIN_EXPR;
   	WHERE_EXPR;
   	HAVING_EXPR;
	EVAL_BITWISE_EXPR;
   	EVAL_AND_EXPR;
   	EVAL_OR_EXPR;
   	EVAL_EQUALS_EXPR;
   	EVAL_NOTEQUALS_EXPR;
   	EVAL_EQUALS_GROUP_EXPR;
   	EVAL_NOTEQUALS_GROUP_EXPR;
   	EVAL_IDENT;
   	SELECTION_EXPR;
   	SELECTION_ELEMENT_EXPR;
   	SELECTION_STREAM;
   	STREAM_EXPR;
   	OUTERJOIN_EXPR;
   	INNERJOIN_EXPR;
   	LEFT_OUTERJOIN_EXPR;
   	RIGHT_OUTERJOIN_EXPR;
   	FULL_OUTERJOIN_EXPR;
   	GROUP_BY_EXPR;
   	ORDER_BY_EXPR;
   	ORDER_ELEMENT_EXPR;
   	EVENT_PROP_EXPR;
   	EVENT_PROP_SIMPLE;
   	EVENT_PROP_MAPPED;
   	EVENT_PROP_INDEXED;
   	EVENT_PROP_DYNAMIC_SIMPLE;
   	EVENT_PROP_DYNAMIC_INDEXED;
   	EVENT_PROP_DYNAMIC_MAPPED;
   	EVENT_LIMIT_EXPR;
	TIMEPERIOD_LIMIT_EXPR;
   	AFTER_LIMIT_EXPR;
	CRONTAB_LIMIT_EXPR;
	CRONTAB_LIMIT_EXPR_PARAM;
	WHEN_LIMIT_EXPR;
	INSERTINTO_EXPR;
	EXPRCOL;
	CONCAT;	
	LIB_FUNCTION;
	LIB_FUNC_CHAIN;
	DOT_EXPR;
	UNARY_MINUS;
	TIME_PERIOD;
	ARRAY_EXPR;
	YEAR_PART;
	MONTH_PART;
	WEEK_PART;
	DAY_PART;
	HOUR_PART;
	MINUTE_PART;
	SECOND_PART;
	MILLISECOND_PART;
	NOT_IN_SET;
	NOT_BETWEEN;
	NOT_LIKE;
	NOT_REGEXP;
   	DBSELECT_EXPR;
   	DBFROM_CLAUSE;
   	DBWHERE_CLAUSE;
   	WILDCARD_SELECT;
	INSERTINTO_STREAM_NAME;
	IN_RANGE;
	NOT_IN_RANGE;
	SUBSELECT_EXPR;
	SUBSELECT_GROUP_EXPR;
	EXISTS_SUBSELECT_EXPR;
	IN_SUBSELECT_EXPR;
	NOT_IN_SUBSELECT_EXPR;
	IN_SUBSELECT_QUERY_EXPR;
	LAST_OPERATOR;
	WEEKDAY_OPERATOR;
	SUBSTITUTION;
	CAST_EXPR;
	CREATE_INDEX_EXPR;
	CREATE_WINDOW_EXPR;
	CREATE_WINDOW_SELECT_EXPR;
	ON_EXPR;
	ON_STREAM;
	ON_DELETE_EXPR;
	ON_SELECT_EXPR;
	ON_UPDATE_EXPR;
	ON_MERGE_EXPR;
	ON_SELECT_INSERT_EXPR;
	ON_SELECT_INSERT_OUTPUT;
	ON_EXPR_FROM;
	ON_SET_EXPR;
	CREATE_VARIABLE_EXPR;
	METHOD_JOIN_EXPR;
	MATCH_UNTIL_EXPR;
	MATCH_UNTIL_RANGE_HALFOPEN;
	MATCH_UNTIL_RANGE_HALFCLOSED;
	MATCH_UNTIL_RANGE_CLOSED;
	MATCH_UNTIL_RANGE_BOUNDED;
	CREATE_COL_TYPE_LIST;
	CREATE_COL_TYPE;
	NUMBERSETSTAR;
	ANNOTATION;
	ANNOTATION_ARRAY;
	ANNOTATION_VALUE;
	FIRST_AGGREG;
	LAST_AGGREG;
	WINDOW_AGGREG;
	UPDATE_EXPR;
	ON_SET_EXPR_ITEM;
	CREATE_SCHEMA_EXPR;
	CREATE_SCHEMA_EXPR_QUAL;
	CREATE_SCHEMA_EXPR_INH;
	VARIANT_LIST;
	MERGE_UPD;
	MERGE_INS;
	
   	INT_TYPE;
   	LONG_TYPE;
   	FLOAT_TYPE;
   	DOUBLE_TYPE;
   	STRING_TYPE;
   	BOOL_TYPE;
   	NULL_TYPE;
   	NUM_DOUBLE;
   	
   	EPL_EXPR;
   	MATCHREC_PATTERN;   	
   	MATCHREC_PATTERN_ATOM;   	
   	MATCHREC_PATTERN_CONCAT;
   	MATCHREC_PATTERN_ALTER;
   	MATCHREC_PATTERN_NESTED;
   	MATCHREC_AFTER_SKIP;
   	MATCHREC_INTERVAL;
   	MATCHREC_DEFINE;
   	MATCHREC_DEFINE_ITEM;
   	MATCHREC_MEASURES;
   	MATCHREC_MEASURE_ITEM;
   	MATCHREC_PARTITION;
}

@header {
  package com.espertech.esper.epl.generated;
}
@lexer::header {
  package com.espertech.esper.epl.generated;
}
@lexer::members {
  protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new MismatchedTokenException(ttype, input);  
  }

  public void recoverFromMismatchedToken(IntStream intStream, RecognitionException recognitionException, int i, BitSet bitSet) throws RecognitionException {
    throw recognitionException;
  }

  public Object recoverFromMismatchedSet(IntStream intStream, RecognitionException recognitionException, BitSet bitSet) throws RecognitionException {
    throw recognitionException;
  }

  protected boolean recoverFromMismatchedElement(IntStream intStream, RecognitionException recognitionException, BitSet bitSet) {
    throw new RuntimeException("Error recovering from mismatched element", recognitionException);
  }
  
  public String getErrorMessage(RecognitionException e, String[] tokenNames) {
    if(e instanceof EarlyExitException)
        {
            throw new RuntimeException(e);
        }
    return super.getErrorMessage(e, tokenNames);
  }
}
@members {
  // provide nice error messages
  private Stack<String> paraphrases = new Stack<String>();
  
  // static information initialized once
  private static Map<Integer, String> lexerTokenParaphases = new HashMap<Integer, String>();
  private static Map<Integer, String> parserTokenParaphases = new HashMap<Integer, String>();
  private static java.util.Set<String> parserKeywordSet = new java.util.HashSet<String>();
    
  public Stack getParaphrases() {
    return paraphrases;
  }

  public java.util.Set<String> getKeywords() {
  	getParserTokenParaphrases();
  	return parserKeywordSet;
  }
  
  public String removeTicks(String tickedString) {
  	return com.espertech.esper.epl.parse.ASTConstantHelper.removeTicks(tickedString);
  }
  
  public Map<Integer, String> getLexerTokenParaphrases() {
    if (lexerTokenParaphases.size() == 0) {
      	lexerTokenParaphases.put(IDENT, "an identifier");
      	lexerTokenParaphases.put(NUM_INT, "a numeric literal");
	lexerTokenParaphases.put(FOLLOWED_BY, "an followed-by '->'");
	lexerTokenParaphases.put(EQUALS, "an equals '='");
	lexerTokenParaphases.put(SQL_NE, "a sql-style not equals '<>'");
	lexerTokenParaphases.put(QUESTION, "a questionmark '?'");
	lexerTokenParaphases.put(LPAREN, "an opening parenthesis '('");
	lexerTokenParaphases.put(RPAREN, "a closing parenthesis ')'");
	lexerTokenParaphases.put(LBRACK, "a left angle bracket '['");
	lexerTokenParaphases.put(RBRACK, "a right angle bracket ']'");
	lexerTokenParaphases.put(LCURLY, "a left curly bracket '{'");
	lexerTokenParaphases.put(RCURLY, "a right curly bracket '}'");
	lexerTokenParaphases.put(COLON, "a colon ':'");
	lexerTokenParaphases.put(COMMA, "a comma ','");
	lexerTokenParaphases.put(EQUAL, "an equals compare '=='");
	lexerTokenParaphases.put(LNOT, "a not '!'");
	lexerTokenParaphases.put(BNOT, "a binary not '~'");
	lexerTokenParaphases.put(NOT_EQUAL, "a not equals '!='");
	lexerTokenParaphases.put(DIV, "a division operator '\'");
	lexerTokenParaphases.put(DIV_ASSIGN, "a division assign '/='");
	lexerTokenParaphases.put(PLUS, "a plus operator '+'");
	lexerTokenParaphases.put(PLUS_ASSIGN, "a plus assign '+='");
	lexerTokenParaphases.put(INC, "an increment operator '++'");
	lexerTokenParaphases.put(MINUS, "a minus '-'");
	lexerTokenParaphases.put(MINUS_ASSIGN, "a minus assign '-='");
	lexerTokenParaphases.put(DEC, "a decrement operator '--'");
	lexerTokenParaphases.put(STAR, "a star '*'");
	lexerTokenParaphases.put(STAR_ASSIGN, "a star assign '*='");
	lexerTokenParaphases.put(MOD, "a modulo");
	lexerTokenParaphases.put(MOD_ASSIGN, "a modulo assign");
	lexerTokenParaphases.put(SR, "a shift right '>>'");
	lexerTokenParaphases.put(SR_ASSIGN, "a shift right assign '>>='");
	lexerTokenParaphases.put(BSR, "a binary shift right '>>>'");
	lexerTokenParaphases.put(BSR_ASSIGN, "a binary shift right assign '>>>='");
	lexerTokenParaphases.put(GE, "a greater equals '>='");
	lexerTokenParaphases.put(GT, "a greater then '>'");
	lexerTokenParaphases.put(SL, "a shift left '<<'");
	lexerTokenParaphases.put(SL_ASSIGN, "a shift left assign '<<='");
	lexerTokenParaphases.put(LE, "a less equals '<='");
	lexerTokenParaphases.put(LT, "a lesser then '<'");
	lexerTokenParaphases.put(BXOR, "a binary xor '^'");
	lexerTokenParaphases.put(BXOR_ASSIGN, "a binary xor assign '^='");
	lexerTokenParaphases.put(BOR, "a binary or '|'");
	lexerTokenParaphases.put(BOR_ASSIGN, "a binary or assign '|='");
	lexerTokenParaphases.put(LOR, "a logical or '||'");
	lexerTokenParaphases.put(BAND, "a binary and '&'");
	lexerTokenParaphases.put(BAND_ASSIGN, "a binary and assign '&='");
	lexerTokenParaphases.put(LAND, "a logical and '&&'");
	lexerTokenParaphases.put(SEMI, "a semicolon ';'");
	lexerTokenParaphases.put(DOT, "a dot '.'");		
    }
    return lexerTokenParaphases;
  }
  
  public Map<Integer, String> getParserTokenParaphrases() {
    if (parserTokenParaphases.size() == 0) {
	parserTokenParaphases.put(CREATE, "'create'");
	parserTokenParaphases.put(WINDOW, "'window'");
	parserTokenParaphases.put(IN_SET, "'in'");
	parserTokenParaphases.put(BETWEEN, "'between'");
	parserTokenParaphases.put(LIKE, "'like'");
	parserTokenParaphases.put(REGEXP, "'regexp'");
	parserTokenParaphases.put(ESCAPE, "'escape'");
	parserTokenParaphases.put(OR_EXPR, "'or'");
	parserTokenParaphases.put(AND_EXPR, "'and'");
	parserTokenParaphases.put(NOT_EXPR, "'not'");
	parserTokenParaphases.put(EVERY_EXPR, "'every'");
	parserTokenParaphases.put(EVERY_DISTINCT_EXPR, "'every-distinct'");
	parserTokenParaphases.put(WHERE, "'where'");
	parserTokenParaphases.put(AS, "'as'");	
	parserTokenParaphases.put(SUM, "'sum'");
	parserTokenParaphases.put(AVG, "'avg'");
	parserTokenParaphases.put(MAX, "'max'");
	parserTokenParaphases.put(MIN, "'min'");
	parserTokenParaphases.put(COALESCE, "'coalesce'");
	parserTokenParaphases.put(MEDIAN, "'median'");
	parserTokenParaphases.put(STDDEV, "'stddev'");
	parserTokenParaphases.put(AVEDEV, "'avedev'");
	parserTokenParaphases.put(COUNT, "'count'");
	parserTokenParaphases.put(SELECT, "'select'");
	parserTokenParaphases.put(CASE, "'case'");
	parserTokenParaphases.put(CASE2, "'case'");
	parserTokenParaphases.put(ELSE, "'else'");
	parserTokenParaphases.put(WHEN, "'when'");
	parserTokenParaphases.put(THEN, "'then'");
	parserTokenParaphases.put(END, "'end'");
	parserTokenParaphases.put(FROM, "'from'");
	parserTokenParaphases.put(OUTER, "'outer'");
	parserTokenParaphases.put(INNER, "'inner'");
	parserTokenParaphases.put(JOIN, "'join'");
	parserTokenParaphases.put(LEFT, "'left'");
	parserTokenParaphases.put(RIGHT, "'right'");
	parserTokenParaphases.put(FULL, "'full'");
	parserTokenParaphases.put(ON, "'on'");	
	parserTokenParaphases.put(IS, "'is'");
	parserTokenParaphases.put(BY, "'by'");
	parserTokenParaphases.put(GROUP, "'group'");
	parserTokenParaphases.put(HAVING, "'having'");
	parserTokenParaphases.put(ALL, "'all'");
	parserTokenParaphases.put(ANY, "'any'");
	parserTokenParaphases.put(SOME, "'some'");
	parserTokenParaphases.put(OUTPUT, "'output'");
	parserTokenParaphases.put(EVENTS, "'events'");
	parserTokenParaphases.put(FIRST, "'first'");
	parserTokenParaphases.put(LAST, "'last'");
	parserTokenParaphases.put(INSERT, "'insert'");
	parserTokenParaphases.put(INTO, "'into'");
	parserTokenParaphases.put(ORDER, "'order'");
	parserTokenParaphases.put(ASC, "'asc'");
	parserTokenParaphases.put(DESC, "'desc'");
	parserTokenParaphases.put(RSTREAM, "'rstream'");
	parserTokenParaphases.put(ISTREAM, "'istream'");
	parserTokenParaphases.put(IRSTREAM, "'irstream'");
	parserTokenParaphases.put(SCHEMA, "'schema'");
	parserTokenParaphases.put(UNIDIRECTIONAL, "'unidirectional'");
	parserTokenParaphases.put(RETAINUNION, "'retain-union'");
	parserTokenParaphases.put(RETAININTERSECTION, "'retain-intersection'");
	parserTokenParaphases.put(PATTERN, "'pattern'");
	parserTokenParaphases.put(SQL, "'sql'");
	parserTokenParaphases.put(METADATASQL, "'metadatasql'");
	parserTokenParaphases.put(PREVIOUS, "'prev'");
	parserTokenParaphases.put(PREVIOUSTAIL, "'prevtail'");
	parserTokenParaphases.put(PREVIOUSCOUNT, "'prevcount'");
	parserTokenParaphases.put(PREVIOUSWINDOW, "'prevwindow'");
	parserTokenParaphases.put(PRIOR, "'prior'");
	parserTokenParaphases.put(EXISTS, "'exists'");
	parserTokenParaphases.put(WEEKDAY, "'weekday'");
	parserTokenParaphases.put(LW, "'lastweekday'");
	parserTokenParaphases.put(INSTANCEOF, "'instanceof'");
	parserTokenParaphases.put(TYPEOF, "'typeof'");
	parserTokenParaphases.put(CAST, "'cast'");
	parserTokenParaphases.put(CURRENT_TIMESTAMP, "'current_timestamp'");
	parserTokenParaphases.put(DELETE, "'delete'");
	parserTokenParaphases.put(DISTINCT, "'distinct'");
	parserTokenParaphases.put(SNAPSHOT, "'snapshot'");
	parserTokenParaphases.put(SET, "'set'");
	parserTokenParaphases.put(VARIABLE, "'variable'");
	parserTokenParaphases.put(INDEX, "'index'");
	parserTokenParaphases.put(UNTIL, "'until'");
	parserTokenParaphases.put(AT, "'at'");
	parserTokenParaphases.put(TIMEPERIOD_YEAR, "'year'");
	parserTokenParaphases.put(TIMEPERIOD_YEARS, "'years'");
	parserTokenParaphases.put(TIMEPERIOD_MONTH, "'month'");
	parserTokenParaphases.put(TIMEPERIOD_MONTHS, "'months'");
	parserTokenParaphases.put(TIMEPERIOD_WEEK, "'week'");
	parserTokenParaphases.put(TIMEPERIOD_WEEKS, "'weeks'");
	parserTokenParaphases.put(TIMEPERIOD_DAY, "'day'");
	parserTokenParaphases.put(TIMEPERIOD_DAYS, "'days'");
	parserTokenParaphases.put(TIMEPERIOD_HOUR, "'hour'");
	parserTokenParaphases.put(TIMEPERIOD_HOURS, "'hours'");
	parserTokenParaphases.put(TIMEPERIOD_MINUTE, "'minute'");
	parserTokenParaphases.put(TIMEPERIOD_MINUTES, "'minutes'");
	parserTokenParaphases.put(TIMEPERIOD_SEC, "'sec'");
	parserTokenParaphases.put(TIMEPERIOD_SECOND, "'second'");
	parserTokenParaphases.put(TIMEPERIOD_SECONDS, "'seconds'");
	parserTokenParaphases.put(TIMEPERIOD_MILLISEC, "'msec'");
	parserTokenParaphases.put(TIMEPERIOD_MILLISECOND, "'millisecond'");
	parserTokenParaphases.put(TIMEPERIOD_MILLISECONDS, "'milliseconds'");
	parserTokenParaphases.put(BOOLEAN_TRUE, "'true'");
	parserTokenParaphases.put(BOOLEAN_FALSE, "'false'");
	parserTokenParaphases.put(VALUE_NULL, "'null'");
	parserTokenParaphases.put(ROW_LIMIT_EXPR, "'limit'");
	parserTokenParaphases.put(OFFSET, "'offset'");
	parserTokenParaphases.put(UPDATE, "'update'");
	parserTokenParaphases.put(MATCH_RECOGNIZE, "'match_recognize'");
	parserTokenParaphases.put(MEASURES, "'measures'");
	parserTokenParaphases.put(DEFINE, "'define'");
	parserTokenParaphases.put(PARTITION, "'partition'");
	parserTokenParaphases.put(MATCHES, "'matches'");
	parserTokenParaphases.put(AFTER, "'after'");
	parserTokenParaphases.put(FOR, "'for'");
	parserTokenParaphases.put(WHILE, "'while'");
	parserTokenParaphases.put(MERGE, "'merge'");
	parserTokenParaphases.put(MATCHED, "'matched'");

	parserKeywordSet = new java.util.TreeSet<String>(parserTokenParaphases.values());
    }
    return parserTokenParaphases;
  }

  protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new MismatchedTokenException(ttype, input);  
  }

  public void recoverFromMismatchedToken(IntStream intStream, RecognitionException recognitionException, int i, BitSet bitSet) throws RecognitionException {
    throw recognitionException;
  }

  public Object recoverFromMismatchedSet(IntStream intStream, RecognitionException recognitionException, BitSet bitSet) throws RecognitionException {
    throw recognitionException;
  }

  protected boolean recoverFromMismatchedElement(IntStream intStream, RecognitionException recognitionException, BitSet bitSet) {
    throw new RuntimeException("Error recovering from mismatched element: " + recognitionException.getMessage(), recognitionException);
  }
  public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
    throw new RuntimeException(e);
  }
}
@rulecatch {
  catch (RecognitionException rex) {
    throw rex;
  }
}

startPatternExpressionRule
	:	annotationNoEnum*
		patternExpression
		EOF!
	;
	
startEPLExpressionRule 
	:	annotationEnum*	
		eplExpression
		EOF
		-> ^(EPL_EXPR annotationEnum* eplExpression) 
	;

startEventPropertyRule 
	:	eventProperty
		EOF!
	;

//----------------------------------------------------------------------------
// Annotations
//----------------------------------------------------------------------------
annotationNoEnum
    :   '@' classIdentifier ( '(' ( elementValuePairsNoEnum | elementValueNoEnum )? ')' )?
	-> ^(ANNOTATION classIdentifier elementValuePairsNoEnum? elementValueNoEnum?)
    ;
    
annotationEnum
    :   '@' classIdentifier ( '(' ( elementValuePairsEnum | elementValueEnum )? ')' )?
	-> ^(ANNOTATION classIdentifier elementValuePairsEnum? elementValueEnum?)
    ;
    
elementValuePairsNoEnum
    :   elementValuePairNoEnum (COMMA! elementValuePairNoEnum)*
    ;

elementValuePairsEnum
    :   elementValuePairEnum (COMMA! elementValuePairEnum)*
    ;

elementValuePairNoEnum
    :   i=IDENT '=' elementValueNoEnum
	-> ^(ANNOTATION_VALUE $i elementValueNoEnum)
    ;
    
elementValuePairEnum
    :   i=IDENT '=' elementValueEnum
	-> ^(ANNOTATION_VALUE $i elementValueEnum)
    ;

elementValueNoEnum
    :   annotationEnum
    |   (elementValueArrayNoEnum) -> elementValueArrayNoEnum
    |	constant
    ;
    
elementValueEnum
    :   annotationEnum
    |   (elementValueArrayEnum) -> elementValueArrayEnum
    |	constant
    |	classIdentifier	
    ;

elementValueArrayNoEnum
    :   '{' (elementValueNoEnum (',' elementValueNoEnum)*)? (',')? '}'
	-> ^(ANNOTATION_ARRAY elementValueNoEnum*)
    ;

elementValueArrayEnum
    :   '{' (elementValueEnum (',' elementValueEnum)*)? (',')? '}'
	-> ^(ANNOTATION_ARRAY elementValueEnum*)
    ;
    
//----------------------------------------------------------------------------
// EPL expression
//----------------------------------------------------------------------------
eplExpression 
	:	(selectExpr
	|	createWindowExpr
	|	createIndexExpr
	|	createVariableExpr
	|	createSchemaExpr
	|	onExpr
	|	updateExpr) forExpr?
	;
	
selectExpr
	:	(INSERT! insertIntoExpr)?
		SELECT! selectClause
		FROM! fromClause
		matchRecog?
		(WHERE! whereClause)?
		(GROUP! BY! groupByListExpr)?
		(HAVING! havingClause)?
		(OUTPUT! outputLimit)?
		(ORDER! BY! orderByListExpr)?
		(ROW_LIMIT_EXPR! rowLimit)?
	;
	
onExpr 
	:	ON onStreamExpr
		(onDeleteExpr | onSelectExpr (onSelectInsertExpr+ outputClauseInsert?)? | onSetExpr | onUpdateExpr | onMergeExpr)
		-> ^(ON_EXPR onStreamExpr onDeleteExpr? onSelectExpr? onSelectInsertExpr* outputClauseInsert? onSetExpr? onUpdateExpr? onMergeExpr?)
	;
	
onStreamExpr
	:	(eventFilterExpression | patternInclusionExpression) (AS i=IDENT | i=IDENT)?
		-> ^(ON_STREAM eventFilterExpression? patternInclusionExpression? $i?)
	;

updateExpr
	:	UPDATE ISTREAM classIdentifier (AS i=IDENT | i=IDENT)?
		SET onSetAssignment (COMMA onSetAssignment)* 
		(WHERE whereClause)?		
		-> ^(UPDATE_EXPR classIdentifier $i? onSetAssignment+ whereClause?)
	;

onMergeExpr
	:	MERGE INTO? n=IDENT (AS i=IDENT | i=IDENT)?
		(WHERE whereClause)?		
		mergeItem+
		-> ^(ON_MERGE_EXPR $n $i? mergeItem+ whereClause?)
	;

mergeItem
	:	(mergeMatched | mergeUnmatched)
	;
	
mergeMatched
	:	WHEN MATCHED (AND_EXPR expression)? THEN
		(
		  (i=UPDATE SET onSetAssignment (COMMA onSetAssignment)*)
		| d=DELETE 		
		)
		-> ^(MERGE_UPD expression? $i? $d? onSetAssignment*)
	;

mergeUnmatched
	:	WHEN NOT_EXPR MATCHED (AND_EXPR expression)? THEN
		INSERT (LPAREN columnList RPAREN)? SELECT selectionList
		-> ^(MERGE_INS selectionList columnList? expression?)
	;	
	
onSelectExpr	
@init  { paraphrases.push("on-select clause"); }
@after { paraphrases.pop(); }
	:	(INSERT insertIntoExpr)?
		SELECT DISTINCT? selectionList
		onExprFrom?
		(WHERE whereClause)?		
		(GROUP BY groupByListExpr)?
		(HAVING havingClause)?
		(ORDER BY orderByListExpr)?
		(ROW_LIMIT_EXPR rowLimit)?
		-> ^(ON_SELECT_EXPR insertIntoExpr? DISTINCT? selectionList onExprFrom? whereClause? groupByListExpr? havingClause? orderByListExpr? rowLimit?)
	;
	
onUpdateExpr	
@init  { paraphrases.push("on-update clause"); }
@after { paraphrases.pop(); }
	:	UPDATE 
		n=IDENT (AS i=IDENT | i=IDENT)?
		SET onSetAssignment (COMMA onSetAssignment)*
		(WHERE whereClause)?		
		-> ^(ON_UPDATE_EXPR ^(ON_EXPR_FROM $n $i?) onSetAssignment+ whereClause?)
	;

onSelectInsertExpr
@init  { paraphrases.push("on-select-insert clause"); }
@after { paraphrases.pop(); }
	:	INSERT insertIntoExpr
		SELECT selectionList
		(WHERE whereClause)?		
		-> ^(ON_SELECT_INSERT_EXPR insertIntoExpr selectionList whereClause?)
	;
	
outputClauseInsert
	:	OUTPUT (f=FIRST | a=ALL)
		-> ^(ON_SELECT_INSERT_OUTPUT $f? $a?)	
	;
	
onDeleteExpr	
@init  { paraphrases.push("on-delete clause"); }
@after { paraphrases.pop(); }
	:	DELETE
		onExprFrom
		(WHERE whereClause)?		
		-> ^(ON_DELETE_EXPR onExprFrom whereClause?)
	;
	
onSetExpr
@init  { paraphrases.push("on-set clause"); }
@after { paraphrases.pop(); }
	:	SET onSetAssignment (COMMA onSetAssignment)*
		-> ^(ON_SET_EXPR onSetAssignment+)
	;
	
onSetAssignment
	:	eventProperty EQUALS expression
		-> ^(ON_SET_EXPR_ITEM eventProperty expression)
	;
		
onExprFrom
	:	FROM n=IDENT (AS i=IDENT | i=IDENT)?
		-> ^(ON_EXPR_FROM $n $i?)
	;

createWindowExpr
	:	CREATE WINDOW i=IDENT (DOT viewExpression (DOT viewExpression)*)? (ru=RETAINUNION|ri=RETAININTERSECTION)? AS? 
		  (
		  	createWindowExprModelAfter		  
		  |   	LPAREN createColumnList RPAREN
		  )		
		  (i1=INSERT (WHERE expression)? )?
		-> {i1 != null}? ^(CREATE_WINDOW_EXPR $i viewExpression* $ru? $ri? createWindowExprModelAfter? createColumnList? 
				^(INSERT expression?))
		-> ^(CREATE_WINDOW_EXPR $i viewExpression* $ru? $ri? createWindowExprModelAfter? createColumnList?)
	;

createWindowExprModelAfter
	:	(SELECT! createSelectionList FROM!)? classIdentifier
	;
		
createIndexExpr
	:	CREATE INDEX n=IDENT ON w=IDENT LPAREN columnList RPAREN
		-> ^(CREATE_INDEX_EXPR $n $w columnList)
	;

createVariableExpr
	:	CREATE VARIABLE classIdentifier n=IDENT (EQUALS expression)?
		-> ^(CREATE_VARIABLE_EXPR classIdentifier $n expression?)
	;

createColumnList 	
@init  { paraphrases.push("column list"); }
@after { paraphrases.pop(); }
	:	createColumnListElement (COMMA createColumnListElement)*
		-> ^(CREATE_COL_TYPE_LIST createColumnListElement+)
	;
	
createColumnListElement
	:   	classIdentifierNonGreedy (classIdentifier (b=LBRACK RBRACK)?)
		-> ^(CREATE_COL_TYPE classIdentifierNonGreedy classIdentifier $b?)
	;

createSelectionList 	
@init  { paraphrases.push("select clause"); }
@after { paraphrases.pop(); }
	:	createSelectionListElement (COMMA createSelectionListElement)*
		-> ^(CREATE_WINDOW_SELECT_EXPR createSelectionListElement+)
	;

createSelectionListElement
	:   	s=STAR
		-> WILDCARD_SELECT[$s]
	|	eventProperty (AS i=IDENT)?
		-> ^(SELECTION_ELEMENT_EXPR eventProperty $i?)
	|	constant AS i=IDENT
		-> ^(SELECTION_ELEMENT_EXPR constant $i?)
	;

createSchemaExpr
	:	CREATE keyword=IDENT? SCHEMA name=IDENT AS? 
		  (
			variantList
		  |   	LPAREN createColumnList? RPAREN (inherits=IDENT columnList)?
		  )		  
		-> {$inherits != null}? ^(CREATE_SCHEMA_EXPR $name createColumnList? ^(CREATE_SCHEMA_EXPR_INH $inherits columnList))
		-> {$keyword != null}? ^(CREATE_SCHEMA_EXPR $name variantList ^(CREATE_SCHEMA_EXPR_QUAL $keyword))
		-> ^(CREATE_SCHEMA_EXPR $name variantList? createColumnList?)
	;

variantList 	
	:	variantListElement (COMMA variantListElement)*
		-> ^(VARIANT_LIST variantListElement+)
	;

variantListElement
	:   	STAR^
	|	classIdentifier
	;

insertIntoExpr
@init  { paraphrases.push("insert-into clause"); }
@after { paraphrases.pop(); }
	:	(s=ISTREAM | s=RSTREAM)? INTO classIdentifier (LPAREN columnList RPAREN)?
		-> ^(INSERTINTO_EXPR $s? classIdentifier columnList?)
	;
		
columnList
	: 	IDENT (COMMA IDENT)* 
		-> ^(EXPRCOL IDENT*)
	;
	
fromClause 
@init  { paraphrases.push("from clause"); }
@after { paraphrases.pop(); }
	:	streamExpression (regularJoin | outerJoinList)
	;
	
regularJoin
	:	(COMMA! streamExpression)*
	;
	
outerJoinList
	:	outerJoin (outerJoin)*
	;

outerJoin
@init  { paraphrases.push("outer join"); }
@after { paraphrases.pop(); }
	:	(
	            ((tl=LEFT|tr=RIGHT|tf=FULL) OUTER)? 
	          | (i=INNER)
	        ) JOIN streamExpression outerJoinIdent
		-> {$i != null}? streamExpression ^(INNERJOIN_EXPR outerJoinIdent)
		-> {$tl != null}? streamExpression ^(LEFT_OUTERJOIN_EXPR outerJoinIdent)
		-> {$tr != null}? streamExpression ^(RIGHT_OUTERJOIN_EXPR outerJoinIdent)
		-> streamExpression ^(FULL_OUTERJOIN_EXPR outerJoinIdent)
	;

outerJoinIdent
	:	ON! outerJoinIdentPair (AND_EXPR! outerJoinIdentPair)*
	;
	
outerJoinIdentPair 
	:	eventProperty EQUALS! eventProperty
	;

whereClause
@init  { paraphrases.push("where clause"); }
@after { paraphrases.pop(); }
	:	evalOrExpression
		-> ^(WHERE_EXPR evalOrExpression)
	;
	
selectClause
@init  { paraphrases.push("select clause"); }
@after { paraphrases.pop(); }
	:	(s=RSTREAM | s=ISTREAM | s=IRSTREAM)? d=DISTINCT? selectionList
		-> ^(SELECTION_EXPR $s? $d? selectionList)
	;

selectionList 	
	:	selectionListElement (COMMA! selectionListElement)*
	;

selectionListElement
  @init { String identifier = null; } 
	:   	s=STAR -> WILDCARD_SELECT[$s]
	|	(streamSelector) => streamSelector
	|	expression (AS i=keywordAllowedIdent { identifier = i.getTree().toString(); } )?
		-> {identifier != null}? ^(SELECTION_ELEMENT_EXPR expression IDENT[identifier])
		-> {identifier == null}? ^(SELECTION_ELEMENT_EXPR expression)
		-> ^(SELECTION_ELEMENT_EXPR expression)
	;
	
streamSelector
	:	s=IDENT DOT STAR (AS i=IDENT)?
		-> ^(SELECTION_STREAM $s $i?)
	;
	
// TODO: review should row pattern recognition be a stream or before the WHERE; if after WHERE then make it the only thing
streamExpression
	:	(eventFilterExpression | patternInclusionExpression | databaseJoinExpression | methodJoinExpression )
		(DOT viewExpression (DOT viewExpression)*)? (AS i=IDENT | i=IDENT)? (u=UNIDIRECTIONAL)? (ru=RETAINUNION|ri=RETAININTERSECTION)?
		-> ^(STREAM_EXPR eventFilterExpression? patternInclusionExpression? databaseJoinExpression? methodJoinExpression?
		viewExpression* $i? $u? $ru? $ri?)
	;
		
forExpr
	:	FOR i=IDENT (LPAREN expressionList? RPAREN)?
		-> ^(FOR $i expressionList?)
	;


// Start match recognize
//
// Lowest precedence is listed first, order is (highest to lowest):  
// Single-character-ERE duplication * + ? {m,n}
// Concatenation
// Anchoring ^ $
// Alternation  |
//
matchRecog
	:	MATCH_RECOGNIZE 
	          LPAREN 
			matchRecogPartitionBy? 
			matchRecogMeasures
			matchRecogMatchesSelection?
			matchRecogMatchesAfterSkip?
			matchRecogPattern 
			matchRecogMatchesInterval?
			matchRecogDefine 
		  RPAREN
		-> ^(MATCH_RECOGNIZE matchRecogPartitionBy? matchRecogMeasures matchRecogMatchesSelection? matchRecogMatchesAfterSkip? matchRecogPattern matchRecogMatchesInterval? matchRecogDefine)
	;

matchRecogPartitionBy
	:	PARTITION BY expression (COMMA expression)*
		-> ^(MATCHREC_PARTITION expression+)
	;		
		
matchRecogMeasures 
	:	MEASURES matchRecogMeasureItem (COMMA matchRecogMeasureItem)*
		-> ^(MATCHREC_MEASURES matchRecogMeasureItem*)
	;
	
matchRecogMeasureItem
	:	expression (AS (i=IDENT)? )?
		-> ^(MATCHREC_MEASURE_ITEM expression $i?)
	;
	
matchRecogMatchesSelection
	:	ALL^ MATCHES! 
	;
		
matchRecogPattern
	:	PATTERN LPAREN matchRecogPatternAlteration RPAREN
		-> ^(MATCHREC_PATTERN matchRecogPatternAlteration)
	;
	
matchRecogMatchesAfterSkip
	:	AFTER i1=keywordAllowedIdent i2=keywordAllowedIdent i3=keywordAllowedIdent i4=keywordAllowedIdent i5=keywordAllowedIdent
		-> ^(MATCHREC_AFTER_SKIP $i1 $i2 $i3 $i4 $i5)
	;

matchRecogMatchesInterval
	:	i=IDENT timePeriod
		-> ^(MATCHREC_INTERVAL $i timePeriod)
	;
		
matchRecogPatternAlteration
	:	matchRecogPatternConcat (o=BOR matchRecogPatternConcat)*
		-> {$o != null}? ^(MATCHREC_PATTERN_ALTER matchRecogPatternConcat+)
		-> matchRecogPatternConcat
	;	

matchRecogPatternConcat
	:	matchRecogPatternUnary+
		-> ^(MATCHREC_PATTERN_CONCAT matchRecogPatternUnary+)
	;	

matchRecogPatternUnary
	: 	matchRecogPatternNested
	| 	matchRecogPatternAtom
	;

matchRecogPatternNested
	: 	LPAREN matchRecogPatternAlteration RPAREN (s=STAR | p=PLUS | q=QUESTION)?
		-> ^(MATCHREC_PATTERN_NESTED matchRecogPatternAlteration $s? $p? $q?)
	;
		
matchRecogPatternAtom
	:	i=IDENT ((s=STAR | p=PLUS | q=QUESTION) (reluctant=QUESTION)? )?
		-> ^(MATCHREC_PATTERN_ATOM $i $s? $p? $q? $reluctant?)
	;
	
matchRecogDefine
	:	DEFINE matchRecogDefineItem (COMMA matchRecogDefineItem)*
		-> ^(MATCHREC_DEFINE matchRecogDefineItem+)
	;	

matchRecogDefineItem
	:	i=IDENT AS expression
		-> ^(MATCHREC_DEFINE_ITEM $i expression)
	;	

patternInclusionExpression
	:	PATTERN LBRACK patternExpression RBRACK
		-> ^(PATTERN_INCL_EXPR patternExpression)
	;
	
databaseJoinExpression
@init  { paraphrases.push("relational data join"); }
@after { paraphrases.pop(); }
	:	SQL COLON i=IDENT LBRACK (s=STRING_LITERAL | s=QUOTED_STRING_LITERAL) (METADATASQL (s2=STRING_LITERAL | s2=QUOTED_STRING_LITERAL))? RBRACK
		-> ^(DATABASE_JOIN_EXPR $i $s $s2?)
	;	
	
methodJoinExpression
@init  { paraphrases.push("method invocation join"); }
@after { paraphrases.pop(); }
    	:   	i=IDENT COLON classIdentifier (LPAREN expressionList? RPAREN)?
       		-> ^(METHOD_JOIN_EXPR $i classIdentifier expressionList?)
    	;

viewExpression
@init  { paraphrases.push("view specifications"); }
@after { paraphrases.pop(); }
	:	ns=IDENT COLON (i=IDENT|m=MERGE) LPAREN expressionWithTimeList? RPAREN
		-> {m != null}? ^(VIEW_EXPR $ns ^(IDENT["merge"]) expressionWithTimeList?)
		-> ^(VIEW_EXPR $ns $i expressionWithTimeList?)
	;

groupByListExpr
@init  { paraphrases.push("group-by clause"); }
@after { paraphrases.pop(); }
	:	expression (COMMA expression)*
		-> ^(GROUP_BY_EXPR expression+)
	;

orderByListExpr
@init  { paraphrases.push("order by clause"); }
@after { paraphrases.pop(); }
	:	orderByListElement (COMMA orderByListElement)*
		-> ^(ORDER_BY_EXPR orderByListElement+) 
	;

orderByListElement
	:	expression (d=ASC|d=DESC)?
		-> ^(ORDER_ELEMENT_EXPR expression $d?)
	;

havingClause
@init  { paraphrases.push("having clause"); }
@after { paraphrases.pop(); }
	:	evalOrExpression
		-> ^(HAVING_EXPR evalOrExpression) 
	;

outputLimit
@init  { paraphrases.push("output rate clause"); }
@after { paraphrases.pop(); }
	:      outputLimitAfter?
 	       (k=ALL|k=FIRST|k=LAST|k=SNAPSHOT)? 
	        (
	          ( ev=EVERY_EXPR 
		    ( 
		      (timePeriod) => timePeriod
		    | (number | i=IDENT) (e=EVENTS)
		    )
		  )
		  |
		  ( at=AT crontabLimitParameterSet)
		  |
		  ( wh=WHEN expression (THEN onSetExpr)? )
		  |
	        )
	    -> {$ev != null && $e != null}? ^(EVENT_LIMIT_EXPR $k? number? $i? outputLimitAfter?)
	    -> {$ev != null}? ^(TIMEPERIOD_LIMIT_EXPR $k? timePeriod outputLimitAfter?)		
	    -> {$at != null}? ^(CRONTAB_LIMIT_EXPR $k? crontabLimitParameterSet outputLimitAfter?)		
	    -> {$wh != null}? ^(WHEN_LIMIT_EXPR $k? expression onSetExpr? outputLimitAfter?)
	    -> ^(AFTER_LIMIT_EXPR outputLimitAfter)
	;	

outputLimitAfter
	:   a=AFTER (timePeriod | number EVENTS)
	    -> ^(AFTER timePeriod? number?)
	;	

rowLimit
@init  { paraphrases.push("row limit clause"); }
@after { paraphrases.pop(); }
	:   (n1=numberconstant | i1=IDENT) ((c=COMMA | o=OFFSET) (n2=numberconstant | i2=IDENT))?
	    -> ^(ROW_LIMIT_EXPR $n1? $i1? $n2? $i2? $o? $c?)		
	;	

crontabLimitParameterSet
	:	LPAREN expressionWithTime COMMA expressionWithTime COMMA expressionWithTime COMMA expressionWithTime COMMA expressionWithTime (COMMA expressionWithTime)? RPAREN 
		-> ^(CRONTAB_LIMIT_EXPR_PARAM expressionWithTime*)			
	;			

whenClause
	: (WHEN! expression THEN! expression)
	;

elseClause
	: (ELSE! expression)
	;

// Main expression rule
expression
	: caseExpression
	;

caseExpression
	: { paraphrases.push("case expression"); }  CASE^ whenClause+ elseClause? END! { paraphrases.pop(); }
	| { paraphrases.push("case expression"); }  CASE expression whenClause+ elseClause? END { paraphrases.pop(); }
	  -> ^(CASE2 expression whenClause+ elseClause?)
	| evalOrExpression
	;

evalOrExpression
	: evalAndExpression (op=OR_EXPR evalAndExpression)*
	 -> {$op != null}? ^(EVAL_OR_EXPR evalAndExpression*)
	 -> evalAndExpression
	;

evalAndExpression
	: bitWiseExpression (op=AND_EXPR bitWiseExpression)*
	 -> {$op != null}? ^(EVAL_AND_EXPR bitWiseExpression+)
	 -> bitWiseExpression
	;

bitWiseExpression
	: negatedExpression ( (BAND^|BOR^|BXOR^) negatedExpression)*
	;		

negatedExpression
	: evalEqualsExpression 
	| NOT_EXPR^ evalEqualsExpression
	;		

evalEqualsExpression
	: evalRelationalExpression ( 
	    (eq=EQUALS
    	      |  is=IS
	      |  isnot=IS NOT_EXPR
	      |  sqlne=SQL_NE
	      |  ne=NOT_EQUAL
	     ) 
	       (
	       	evalRelationalExpression
	       	|  (a=ANY | a=SOME | a=ALL) ( (LPAREN expressionList? RPAREN) | subSelectGroupExpression )
	       )
	     )*	     
	    -> {$a == null && ($eq != null || $is != null)}? ^(EVAL_EQUALS_EXPR evalRelationalExpression+)
	    -> {$a != null && ($eq != null || $is != null)}? ^(EVAL_EQUALS_GROUP_EXPR evalRelationalExpression $a expressionList? subSelectGroupExpression?)
	    -> {$a == null && ($isnot != null || $sqlne != null || $ne != null)}? ^(EVAL_NOTEQUALS_EXPR evalRelationalExpression+)
	    -> {$a != null && ($isnot != null || $sqlne != null || $ne != null)}? ^(EVAL_NOTEQUALS_GROUP_EXPR evalRelationalExpression $a expressionList? subSelectGroupExpression?)
	    -> evalRelationalExpression+
	;

evalRelationalExpression
	: concatenationExpr 
		( 
			( 
			  ( 
			    (r=LT|r=GT|r=LE|r=GE) 
			    	(
			    	  concatenationExpr
			    	  | (g=ANY | g=SOME | g=ALL) ( (LPAREN expressionList? RPAREN) | subSelectGroupExpression )
			    	)
			    	
			  )*
			  -> {$g == null && $r != null}? ^({adaptor.create($r)} concatenationExpr+)
			  -> {$g != null && $r != null}? ^({adaptor.create($r)} concatenationExpr $g expressionList? subSelectGroupExpression?)
			  -> concatenationExpr+
			)  
			| (n=NOT_EXPR)? 
			(
				// Represent the optional NOT prefix using the token type by
				// testing 'n' and setting the token type accordingly.
				(i=IN_SET
					  (l=LPAREN | l=LBRACK) expression	// brackets are for inclusive/exclusive
						(
							( col=COLON (expression) )		// range
							|
							( (COMMA expression)* )		// list of values
						)
					  (r=RPAREN | r=RBRACK)	
					)
				    -> {$col == null && $n == null}? ^(IN_SET concatenationExpr $l expression+ $r)
				    -> {$col == null && $n != null}? ^(NOT_IN_SET concatenationExpr $l expression+ $r)
				    -> {$col != null && $n == null}? ^(IN_RANGE concatenationExpr $l expression+ $r)
				    -> ^(NOT_IN_RANGE concatenationExpr $l expression+ $r)
				| IN_SET inSubSelectQuery
				    -> {$n == null}? ^(IN_SUBSELECT_EXPR concatenationExpr inSubSelectQuery)
				    -> ^(NOT_IN_SUBSELECT_EXPR concatenationExpr inSubSelectQuery)
				| BETWEEN betweenList
				    -> {$n == null}? ^(BETWEEN concatenationExpr betweenList)
				    -> ^(NOT_BETWEEN concatenationExpr betweenList)				
				| LIKE concatenationExpr (ESCAPE stringconstant)?
				    -> {$n == null}? ^(LIKE concatenationExpr* stringconstant?)
				    -> ^(NOT_LIKE concatenationExpr* stringconstant?)
				| REGEXP concatenationExpr
				    -> {$n == null}? ^(REGEXP concatenationExpr+)
				    -> ^(NOT_REGEXP concatenationExpr+)				
			)	
		)
	;
	
inSubSelectQuery
	: subQueryExpr
	  -> ^(IN_SUBSELECT_QUERY_EXPR subQueryExpr)
	;
			
concatenationExpr
	: additiveExpression ( c=LOR additiveExpression ( LOR additiveExpression)* )?
	    -> {$c != null}? ^(CONCAT additiveExpression+)
	    -> additiveExpression
	;

additiveExpression
	: multiplyExpression ( (PLUS^|MINUS^) multiplyExpression )*
	;

multiplyExpression
	: unaryExpression ( (STAR^|DIV^|MOD^) unaryExpression )*
	;
	
unaryExpression
	: MINUS eventProperty -> ^(UNARY_MINUS eventProperty)
	| constant
	| substitution
	| LPAREN expression RPAREN (d=DOT libFunctionNoClass (d=DOT libFunctionNoClass)* )?
	    -> {$d != null}? ^(DOT_EXPR expression libFunctionNoClass+)
	    -> expression
	| eventPropertyOrLibFunction
	| (builtinFunc) => (builtinFunc)
	| arrayExpression
	| subSelectExpression
	| existsSubSelectExpression
	;
	    
subSelectExpression 
	:	subQueryExpr
		-> ^(SUBSELECT_EXPR subQueryExpr)
	;

subSelectGroupExpression 
	:	subQueryExpr
		-> ^(SUBSELECT_GROUP_EXPR subQueryExpr)
	;

existsSubSelectExpression 
	:	EXISTS subQueryExpr
		-> ^(EXISTS_SUBSELECT_EXPR subQueryExpr)
	;

subQueryExpr 
@init  { paraphrases.push("subquery"); }
@after { paraphrases.pop(); }
	:	LPAREN! 
		SELECT! DISTINCT? selectionList
	    FROM! subSelectFilterExpr
	    (WHERE! whereClause)?
	    RPAREN!
	;
	
subSelectFilterExpr
@init  { paraphrases.push("subquery filter specification"); }
@after { paraphrases.pop(); }
	:	eventFilterExpression
		(DOT viewExpression (DOT viewExpression)*)? (AS i=IDENT | i=IDENT)? (ru=RETAINUNION|ri=RETAININTERSECTION)?
		-> ^(STREAM_EXPR eventFilterExpression viewExpression* $i? $ru? $ri?)
	;
		
arrayExpression
	: LCURLY (expression (COMMA expression)* )? RCURLY
	  -> ^(ARRAY_EXPR expression*)
	;

builtinFunc
	: SUM^ LPAREN! (ALL! | DISTINCT)? expression RPAREN!
	| AVG^ LPAREN! (ALL! | DISTINCT)? expression RPAREN!
	| COUNT^ LPAREN!
		(
			((ALL! | DISTINCT)? expression)
		|
			(STAR!) 
		)
		RPAREN!
	| MEDIAN^ LPAREN! (ALL! | DISTINCT)? expression RPAREN!
	| STDDEV^ LPAREN! (ALL! | DISTINCT)? expression RPAREN!
	| AVEDEV^ LPAREN! (ALL! | DISTINCT)? expression RPAREN!
	| firstAggregation
	| lastAggregation
	| windowAggregation
	| COALESCE^ LPAREN! expression COMMA! expression (COMMA! expression)* RPAREN!
	| PREVIOUS^ LPAREN! expression (COMMA! expression)? RPAREN!
	| PREVIOUSTAIL^ LPAREN! expression (COMMA! expression)? RPAREN!
	| PREVIOUSCOUNT^ LPAREN! expression RPAREN!
	| PREVIOUSWINDOW^ LPAREN! expression RPAREN!
	| PRIOR^ LPAREN! NUM_INT COMMA! eventProperty RPAREN!
	// MIN and MAX can also be "Math.min" static function and "min(price)" aggregation function and "min(a, b, c...)" built-in function
	// therefore handled in code via libFunction as below
	| INSTANCEOF^ LPAREN! expression COMMA! classIdentifier (COMMA! classIdentifier)* RPAREN!
	| TYPEOF^ LPAREN! expression RPAREN!
	| CAST^ LPAREN! expression (COMMA! | AS!) classIdentifier RPAREN!
	| EXISTS^ LPAREN! eventProperty RPAREN!
	| CURRENT_TIMESTAMP^ (LPAREN! RPAREN!)?
	;
	
firstAggregation
	: FIRST LPAREN accessAggExpr (COMMA expression)? RPAREN
	  -> ^(FIRST_AGGREG accessAggExpr expression?)
	;

lastAggregation
	: LAST LPAREN accessAggExpr (COMMA expression)? RPAREN
	  -> ^(LAST_AGGREG accessAggExpr expression?)
	;
	
windowAggregation
	: WINDOW LPAREN accessAggExpr RPAREN
	  -> ^(WINDOW_AGGREG accessAggExpr)
	;

accessAggExpr
   	:   	s=STAR -> PROPERTY_WILDCARD_SELECT[$s]
	|	(propertyStreamSelector) => propertyStreamSelector
	|	expression
	;


maxFunc
	: (MAX^ | MIN^) LPAREN! expression (COMMA! expression (COMMA! expression)* )? RPAREN!
	;
	
eventPropertyOrLibFunction
	: (eventProperty) => eventProperty
	| libFunction
	;
	
libFunction
	: libFunctionWithClass (DOT libFunctionNoClass)*
	  -> ^(LIB_FUNC_CHAIN libFunctionWithClass libFunctionNoClass*)
	;
		
libFunctionWithClass
	: (classIdentifierNonGreedy DOT)? funcIdent LPAREN (libFunctionArgs)? RPAREN
	  -> ^(LIB_FUNCTION classIdentifierNonGreedy? funcIdent libFunctionArgs?)
	;	

libFunctionNoClass
	: funcIdent LPAREN (libFunctionArgs)? RPAREN
	  -> ^(LIB_FUNCTION funcIdent libFunctionArgs?)
	;	

funcIdent
	: escapableIdent
	| max=MAX -> IDENT[$max]
	| min=MIN -> IDENT[$min]
	;
	
libFunctionArgs
	: (ALL! | DISTINCT)? expression (COMMA! expression)*
	;
	
betweenList
	: concatenationExpr AND_EXPR! concatenationExpr
	;

//----------------------------------------------------------------------------
// Pattern event expressions / event pattern operators
//   Operators are: followed-by (->), or, and, not, every, where
//   Lowest precedence is listed first, order is (lowest to highest):  ->, or, and, not/every, within.
//   On the atomic level an expression has filters, and observer-statements.
//----------------------------------------------------------------------------
patternExpression
@init  { paraphrases.push("pattern expression"); }
@after { paraphrases.pop(); }
	: followedByExpression
	;

followedByExpression
  @init { boolean fb = false; } 
	: orExpression (followedByRepeat { fb = true; } )*
	    -> {fb == true}? ^(FOLLOWED_BY_EXPR ^(FOLLOWED_BY_ITEM orExpression) followedByRepeat+)
	    -> orExpression
	;
	
followedByRepeat
	:   (f=FOLLOWED_BY | (g=FOLLOWMAX_BEGIN expression FOLLOWMAX_END)) orExpression
    		-> ^(FOLLOWED_BY_ITEM expression? orExpression)
	;
	
orExpression
	:	andExpression (o=OR_EXPR andExpression)*
		-> {$o != null}? ^(OR_EXPR andExpression+)
		-> andExpression
	;

andExpression
	:	matchUntilExpression (a=AND_EXPR matchUntilExpression)*
		-> {$a != null}? ^(AND_EXPR matchUntilExpression+)
		-> matchUntilExpression
	;

matchUntilExpression
	:	(r=matchUntilRange)? qualifyExpression (a=UNTIL qualifyExpression)?
		-> {r != null && a != null}? ^(MATCH_UNTIL_EXPR matchUntilRange qualifyExpression+)
		-> {r != null && a == null}? ^(MATCH_UNTIL_EXPR matchUntilRange qualifyExpression)
		-> {$a != null}? ^(MATCH_UNTIL_EXPR qualifyExpression+)
		-> qualifyExpression
	;

qualifyExpression
	:	((e=EVERY_EXPR | n=NOT_EXPR | d=EVERY_DISTINCT_EXPR distinctExpressionList) (r=matchUntilRange)? )?
		guardPostFix
		-> {e != null && r == null}? ^(EVERY_EXPR guardPostFix)
		-> {n != null && r == null}? ^(PATTERN_NOT_EXPR guardPostFix)
		-> {d != null && r == null}? ^(EVERY_DISTINCT_EXPR distinctExpressionList guardPostFix)
		-> {e != null && r != null}? ^(EVERY_EXPR ^(MATCH_UNTIL_EXPR matchUntilRange guardPostFix) )
		-> {n != null && r != null}? ^(PATTERN_NOT_EXPR ^(MATCH_UNTIL_EXPR matchUntilRange guardPostFix) )
		-> {d != null && r != null}? ^(EVERY_DISTINCT_EXPR distinctExpressionList ^(MATCH_UNTIL_EXPR matchUntilRange guardPostFix) )
		-> guardPostFix
	;
	
distinctExpressionList
	:	LPAREN distinctExpressionAtom (COMMA distinctExpressionAtom)* RPAREN
		-> ^(PATTERN_EVERY_DISTINCT_EXPR distinctExpressionAtom+) 
	;

distinctExpressionAtom
	:	expressionWithTime
   	;

guardPostFix
	:	(atomicExpression | l=LPAREN patternExpression RPAREN) ((wh=WHERE guardWhereExpression) | (wi=WHILE guardWhileExpression))?
		-> {$wh != null}? ^(GUARD_EXPR atomicExpression? patternExpression? guardWhereExpression) 
		-> {$wi != null}? ^(GUARD_EXPR atomicExpression? patternExpression? guardWhileExpression) 
		-> atomicExpression? patternExpression?
	;

atomicExpression
	:	observerExpression | patternFilterExpression
	;
		
observerExpression
	:	ns=IDENT COLON (nm=IDENT | a=AT) LPAREN expressionWithTimeList? RPAREN
		-> {$a != null}? ^(OBSERVER_EXPR $ns ^(IDENT[$a.text]) expressionWithTimeList?)
		-> ^(OBSERVER_EXPR $ns $nm expressionWithTimeList?)
	;

guardWhereExpression
	:	IDENT COLON! IDENT LPAREN! (expressionWithTimeList)? RPAREN!
	;
	
guardWhileExpression
	:	LPAREN! expression RPAREN!
	;

// syntax is [a..b]  or [..b]  or  [a..] or [a:b]   wherein a and b may be recognized as double
matchUntilRange
  @init { Boolean isopen = true; } 
	:	LBRACK (
			expression (c1=COLON (expression { isopen = false; }) ? )? 
		   |	c2=COLON expression
		   )
		RBRACK
		-> {$c1 != null && !isopen}? ^(MATCH_UNTIL_RANGE_CLOSED expression expression) 
		-> {$c1 != null && isopen}? ^(MATCH_UNTIL_RANGE_HALFOPEN expression) 
		-> {$c2 != null}? ^(MATCH_UNTIL_RANGE_HALFCLOSED expression)
		-> ^(MATCH_UNTIL_RANGE_BOUNDED expression) 
	;
	
//----------------------------------------------------------------------------
// Filter expressions
//   Operators are the usual bunch =, <, >, =<, >= 
//	 Ranges such as 'property in [a,b]' are allowed and ([ and )] distinguish open/closed range endpoints
//----------------------------------------------------------------------------
eventFilterExpression
@init  { paraphrases.push("filter specification"); }
@after { paraphrases.pop(); }
    :   (i=IDENT EQUALS)?
    	classIdentifier
       	(LPAREN expressionList? RPAREN)?
       	propertyExpression?
       	-> ^(EVENT_FILTER_EXPR $i? classIdentifier propertyExpression? expressionList?)
    ;
    
propertyExpression
	:	propertyExpressionAtomic (propertyExpressionAtomic)*
       	-> ^(EVENT_FILTER_PROPERTY_EXPR propertyExpressionAtomic+)
	;

propertyExpressionAtomic
	:	LBRACK (SELECT propertySelectionList FROM)? eventProperty (AS IDENT)? (WHERE expression)? RBRACK
       	-> ^(EVENT_FILTER_PROPERTY_EXPR_ATOM propertySelectionList? eventProperty IDENT? ^(WHERE_EXPR expression?))
       	;
	
propertySelectionList 	
	:	propertySelectionListElement (COMMA! propertySelectionListElement)*
	;

propertySelectionListElement
  @init { String identifier = null; } 
	:   	s=STAR -> PROPERTY_WILDCARD_SELECT[$s]
	|	(propertyStreamSelector) => propertyStreamSelector
	|	expression (AS i=keywordAllowedIdent { identifier = i.getTree().toString(); } )?
		-> {identifier != null}? ^(PROPERTY_SELECTION_ELEMENT_EXPR expression IDENT[identifier])
		-> ^(PROPERTY_SELECTION_ELEMENT_EXPR expression)
	;
	
propertyStreamSelector
	:	s=IDENT DOT STAR (AS i=IDENT)?
		-> ^(PROPERTY_SELECTION_STREAM $s $i?)
	;

patternFilterExpression
@init  { paraphrases.push("filter specification"); }
@after { paraphrases.pop(); }
    :   (i=IDENT EQUALS)?
    	classIdentifier
       	(LPAREN expressionList? RPAREN)?
       	propertyExpression?
       	-> ^(PATTERN_FILTER_EXPR $i? classIdentifier propertyExpression? expressionList?)
    ;
    

classIdentifier
  @init { String identifier = ""; }
	:	i1=escapableStr { identifier = $i1.result; }
	    ( 
	    	 DOT i2=escapableStr { identifier += "." + $i2.result; }
	    )* 
	    -> ^(CLASS_IDENT[identifier])
	;
	
classIdentifierNonGreedy
  @init { String identifier = ""; } 
	:	i1=escapableStr { identifier = $i1.result; }
	    ( 
	    	 options {greedy=false;} :
	    	 DOT i2=escapableStr { identifier += "." + $i2.result; }
	    )* 
	    -> ^(CLASS_IDENT[identifier])
	;
	
expressionList
    	:   	expression (COMMA! expression)*
    	;
   	
expressionWithTimeList
    	:   	expressionWithTime (COMMA! expressionWithTime)*
    	;

expressionWithTime
	:   	(lastOperand) => lastOperand
	|	(lastWeekdayOperand) => lastWeekdayOperand
	|	(timePeriod) => timePeriod
	|	(expressionQualifyable) => expressionQualifyable
	|	(rangeOperand) => rangeOperand
	| 	(frequencyOperand) => frequencyOperand
	|	(lastOperator) => lastOperator
	|	(weekDayOperator) =>  weekDayOperator
	| 	(numericParameterList) => numericParameterList
	|	numberSetStar
	;

expressionQualifyable
	:	expression (a=ASC|d=DESC|s=TIMEPERIOD_SECONDS|s=TIMEPERIOD_SECOND|s=TIMEPERIOD_SEC)?
		-> {d != null || a != null}? ^(OBJECT_PARAM_ORDERED_EXPR expression $a? $d?)
		-> {s != null}? ^(TIME_PERIOD ^(SECOND_PART expression))
		-> expression
	;
	

numberSetStar
	:	STAR
		-> ^(NUMBERSETSTAR)
	;
	
lastWeekdayOperand
	:	LW^
	;
	
lastOperand
	:	LAST^
	;

frequencyOperand
	:	STAR DIV (number|i=IDENT|substitution) 
		-> {i!= null}? ^(NUMERIC_PARAM_FREQUENCY ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(NUMERIC_PARAM_FREQUENCY number? substitution?)
	;

rangeOperand
	:	(number|i1=IDENT|substitution) COLON (number|i2=IDENT|substitution) 
		-> {i1 != null && i2 != null}? ^(NUMERIC_PARAM_RANGE ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i1)) ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i2)))
		-> {i1 != null && i2 == null}? ^(NUMERIC_PARAM_RANGE ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i1)) number? substitution?)
		-> {i1 == null && i2 != null}? ^(NUMERIC_PARAM_RANGE number? substitution? ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i2)))
		-> ^(NUMERIC_PARAM_RANGE number* substitution*)
	;

lastOperator
	:	(number|i=IDENT|substitution) LAST 
		-> {i!= null}? ^(LAST_OPERATOR ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(LAST_OPERATOR number? substitution?)
	;

weekDayOperator
	:	(number|i=IDENT|substitution) WEEKDAY 
		-> {i!= null}? ^(WEEKDAY_OPERATOR ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(WEEKDAY_OPERATOR number? substitution?)
	;

numericParameterList
	:	LBRACK numericListParameter (COMMA numericListParameter)* RBRACK
		-> ^(NUMERIC_PARAM_LIST numericListParameter+)
	;

numericListParameter
	:	rangeOperand
	| 	frequencyOperand
	|	numberconstant
	;
	    
eventProperty
	:	eventPropertyAtomic (DOT eventPropertyAtomic)* 
		-> ^(EVENT_PROP_EXPR eventPropertyAtomic+)
	;
	
eventPropertyAtomic
	:	eventPropertyIdent (
			lb=LBRACK ni=NUM_INT RBRACK (q=QUESTION)?
			|
			lp=LPAREN (s=STRING_LITERAL | s=QUOTED_STRING_LITERAL) RPAREN (q=QUESTION)?
			|
			q1=QUESTION 
			)?
		
		-> {lb!= null && $q == null}? ^(EVENT_PROP_INDEXED eventPropertyIdent $ni)
		-> {lb!= null && $q != null}? ^(EVENT_PROP_DYNAMIC_INDEXED eventPropertyIdent $ni)
		-> {lp!= null && $q == null}? ^(EVENT_PROP_MAPPED eventPropertyIdent $s)
		-> {lp!= null && $q != null}? ^(EVENT_PROP_DYNAMIC_MAPPED eventPropertyIdent $s)
		-> {q1 != null}? 	      ^(EVENT_PROP_DYNAMIC_SIMPLE eventPropertyIdent)
		-> ^(EVENT_PROP_SIMPLE eventPropertyIdent)
		;
		
eventPropertyIdent
  @init { String identifier = ""; } 
	:	ipi=keywordAllowedIdent { identifier = ipi.result; }
		(
		  ESCAPECHAR DOT ipi2=keywordAllowedIdent? { identifier += "."; if (ipi2 != null) identifier += ipi2.result; }
		)*
	    	-> ^(IDENT[identifier])
	;
	
keywordAllowedIdent returns [String result]
	:	i1=IDENT { $result = $i1.getText(); }
		|i2=TICKED_STRING_LITERAL { $result = removeTicks($i2.getText()); }
		|AT { $result = "at"; }
		|COUNT { $result = "count"; }
		|ESCAPE { $result = "escape"; }
    		|EVERY_EXPR { $result = "every"; }
		|SUM { $result = "sum"; }
		|AVG { $result = "avg"; }
		|MAX { $result = "max"; }
		|MIN { $result = "min"; }
		|COALESCE { $result = "coalesce"; }
		|MEDIAN { $result = "median"; }
		|STDDEV { $result = "stddev"; }
		|AVEDEV { $result = "avedev"; }
		|EVENTS { $result = "events"; }
		|FIRST { $result = "first"; }
		|LAST { $result = "last"; }
		|WHILE { $result = "while"; }
		|MERGE { $result = "merge"; }
		|MATCHED { $result = "matched"; }
		|UNIDIRECTIONAL { $result = "unidirectional"; }
		|RETAINUNION { $result = "retain-union"; }
		|RETAININTERSECTION { $result = "retain-intersection"; }
		|UNTIL { $result = "until"; }
		|PATTERN { $result = "pattern"; }
		|SQL { $result = "sql"; }
		|METADATASQL { $result = "metadatasql"; }
		|PREVIOUS { $result = "prev"; }
		|PREVIOUSTAIL { $result = "prevtail"; }
		|PRIOR { $result = "prior"; }
		|WEEKDAY { $result = "weekday"; }
		|LW { $result = "lastweekday"; }
		|INSTANCEOF { $result = "instanceof"; }
		|TYPEOF { $result = "typeof"; }
		|CAST { $result = "cast"; }
		|SNAPSHOT { $result = "snapshot"; }
		|VARIABLE { $result = "variable"; }		
		|INDEX { $result = "index"; }		
		|WINDOW { $result = "window"; }
		|LEFT { $result = "left"; }
		|RIGHT { $result = "right"; }
		|OUTER { $result = "outer"; }
		|FULL { $result = "full"; }
		|JOIN { $result = "join"; }
		|DEFINE { $result = "define"; }
		|PARTITION { $result = "partition"; }
		|MATCHES { $result = "matches"; }
	;
		
escapableStr returns [String result]
	:	i1=IDENT { $result = $i1.getText(); }
		|i2=TICKED_STRING_LITERAL { $result = removeTicks($i2.getText()); }
	;
	
escapableIdent
	:	IDENT 
		|t=TICKED_STRING_LITERAL -> IDENT[$t]
	;

timePeriod 	
	:	
	(	
		yearPart monthPart? weekPart? dayPart? hourPart? minutePart? secondPart? millisecondPart?
	|	monthPart weekPart? dayPart? hourPart? minutePart? secondPart? millisecondPart?
	|	weekPart dayPart? hourPart? minutePart? secondPart? millisecondPart?
	|	dayPart hourPart? minutePart? secondPart? millisecondPart?
	|	hourPart minutePart? secondPart? millisecondPart?
	|	minutePart secondPart? millisecondPart?
	|	secondPart millisecondPart?
	|	millisecondPart
	)
		-> ^(TIME_PERIOD yearPart? monthPart? weekPart? dayPart? hourPart? minutePart? secondPart? millisecondPart?)
	;

yearPart
	:	(number|i=IDENT|substitution) (TIMEPERIOD_YEARS | TIMEPERIOD_YEAR)
		-> {i!= null}? ^(YEAR_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(YEAR_PART number? substitution?)
	;

monthPart
	:	(number|i=IDENT|substitution) (TIMEPERIOD_MONTHS | TIMEPERIOD_MONTH)
		-> {i!= null}? ^(MONTH_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(MONTH_PART number? substitution?)
	;

weekPart
	:	(number|i=IDENT|substitution) (TIMEPERIOD_WEEKS | TIMEPERIOD_WEEK)
		-> {i!= null}? ^(WEEK_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(WEEK_PART number? substitution?)
	;

dayPart
	:	(number|i=IDENT|substitution) (TIMEPERIOD_DAYS | TIMEPERIOD_DAY)
		-> {i!= null}? ^(DAY_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(DAY_PART number? substitution?)
	;

hourPart 
	:	(number|i=IDENT|substitution) (TIMEPERIOD_HOURS | TIMEPERIOD_HOUR)
		-> {i!= null}? ^(HOUR_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(HOUR_PART number? substitution?)
	;

minutePart 
	:	(number|i=IDENT|substitution) (TIMEPERIOD_MINUTES | TIMEPERIOD_MINUTE | MIN)
		-> {i!= null}? ^(MINUTE_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(MINUTE_PART number? substitution?)
	;
	
secondPart 
	:	(number|i=IDENT|substitution) (TIMEPERIOD_SECONDS | TIMEPERIOD_SECOND | TIMEPERIOD_SEC)
		-> {i!= null}? ^(SECOND_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(SECOND_PART number? substitution?)
	;
	
millisecondPart 
	:	(number|i=IDENT|substitution) (TIMEPERIOD_MILLISECONDS | TIMEPERIOD_MILLISECOND | TIMEPERIOD_MILLISEC)
		-> {i!= null}? ^(MILLISECOND_PART ^(EVENT_PROP_EXPR ^(EVENT_PROP_SIMPLE $i)))
		-> ^(MILLISECOND_PART number? substitution?)
	;
	
number
    :   ni=NUM_INT -> INT_TYPE[$ni]
    |   nl=NUM_LONG -> LONG_TYPE[$nl]
    |   nf=NUM_FLOAT -> FLOAT_TYPE[$nf]
    |   nd=NUM_DOUBLE -> DOUBLE_TYPE[$nd]
    ;

substitution
	: q=QUESTION -> SUBSTITUTION[$q]
	;
	
constant
	:   numberconstant
	|   stringconstant
    	|   t=BOOLEAN_TRUE -> ^(BOOL_TYPE[$t])
    	|   f=BOOLEAN_FALSE -> ^(BOOL_TYPE[$f])
    	|   nu=VALUE_NULL -> ^(NULL_TYPE[$nu])
	;

numberconstant
	:  (m=MINUS | p=PLUS)? number
		-> {$m != null}? {adaptor.create($number.tree.getType(), "-" + $number.text)}
		-> number
	;

stringconstant
	:   sl=STRING_LITERAL -> ^(STRING_TYPE[$sl])
	|   qsl=QUOTED_STRING_LITERAL -> ^(STRING_TYPE[$qsl])
	;

//----------------------------------------------------------------------------
// LEXER
//----------------------------------------------------------------------------

// Operators
FOLLOWMAX_BEGIN : '-[';
FOLLOWMAX_END   : ']>';
FOLLOWED_BY 	: '->';
EQUALS 		: '=';
SQL_NE 		: '<>';
QUESTION 	: '?';
LPAREN 		: '(';
RPAREN 		: ')';
LBRACK 		: '[';
RBRACK 		: ']';
LCURLY 		: '{';
RCURLY 		: '}';
COLON 		: ':';
COMMA 		: ',';
EQUAL 		: '==';
LNOT 		: '!';
BNOT 		: '~';
NOT_EQUAL 	: '!=';
DIV 		: '/';
DIV_ASSIGN 	: '/=';
PLUS 		: '+';
PLUS_ASSIGN	: '+=';
INC 		: '++';
MINUS 		: '-';
MINUS_ASSIGN 	: '-=';
DEC 		: '--';
STAR 		: '*';
STAR_ASSIGN 	: '*=';
MOD 		: '%';
MOD_ASSIGN 	: '%=';
SR 		: '>>';
SR_ASSIGN 	: '>>=';
BSR 		: '>>>';
BSR_ASSIGN 	: '>>>=';
GE 		: '>=';
GT 		: '>';
SL 		: '<<';
SL_ASSIGN 	: '<<=';
LE 		: '<=';
LT 		: '<';
BXOR 		: '^';
BXOR_ASSIGN 	: '^=';
BOR		: '|';
BOR_ASSIGN 	: '|=';
LOR		: '||';
BAND 		: '&';
BAND_ASSIGN 	: '&=';
LAND 		: '&&';
SEMI 		: ';';
DOT 		: '.';
NUM_LONG	: '\u18FF';  // assign bogus unicode characters so the token exists
NUM_DOUBLE	: '\u18FE';
NUM_FLOAT	: '\u18FD';
ESCAPECHAR	: '\\';
EMAILAT		: '@';

// Whitespace -- ignored
WS	:	(	' '
		|	'\t'
		|	'\f'
			// handle newlines
		|	(
				'\r'    // Macintosh
			|	'\n'    // Unix (the right way)
			)
		)+
		{ $channel=HIDDEN; }
	;

// Single-line comments
SL_COMMENT
	:	'//'
		(~('\n'|'\r'))* ('\n'|'\r'('\n')?)?
		{$channel=HIDDEN;}
	;

// multiple-line comments
ML_COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

TICKED_STRING_LITERAL
    :   '`' ( EscapeSequence | ~('\`'|'\\') )* '`'
    ;

QUOTED_STRING_LITERAL
    :   '\'' ( EscapeSequence | ~('\''|'\\') )* '\''
    ;

STRING_LITERAL
    :  '"' ( EscapeSequence | ~('\\'|'"') )* '"'
    ;

fragment
EscapeSequence	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	UnicodeEscape
		|	OctalEscape
		|	. // unknown, leave as it is
		)
    ;    

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;

// an identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT	
	:	('a'..'z'|'_'|'$') ('a'..'z'|'_'|'0'..'9'|'$')*
	;


// a numeric literal
NUM_INT	
	@init {boolean isDecimal=false; Token t=null;}
    :   '.' {$type = DOT;}
            (	('0'..'9')+ (EXPONENT)? (f1=FLOAT_SUFFIX {t=f1;})?
                {
				if (t != null && t.getText().toUpperCase().indexOf('F')>=0) {
                	$type = NUM_FLOAT;
				}
				else {
                	$type = NUM_DOUBLE; // assume double
				}
				}
            )?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig.  ANTLR resolves
					// it correctly by matching immediately.  It
					// is therefor ok to hush warning.
					HexDigit
				)+

			|	//float or double with leading zero
				(('0'..'9')+ ('.'|EXPONENT|FLOAT_SUFFIX)) => ('0'..'9')+

			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(	('l') { $type = NUM_LONG; }

		// only check to see if it's a float if looks like decimal so far
		|	{isDecimal}?
            (   '.' ('0'..'9')* (EXPONENT)? (f2=FLOAT_SUFFIX {t=f2;})?
            |   EXPONENT (f3=FLOAT_SUFFIX {t=f3;})?
            |   f4=FLOAT_SUFFIX {t=f4;}
            )
            {
			if (t != null && t.getText().toUpperCase() .indexOf('F') >= 0) {
                $type = NUM_FLOAT;
			}
            else {
	           	$type = NUM_DOUBLE; // assume double
			}
			}
        )?
	;


// a couple protected methods to assist in matching floating point numbers
fragment
EXPONENT
	:	('e') ('+'|'-')? ('0'..'9')+
	;


fragment
FLOAT_SUFFIX
	:	'f'|'d'
	;
