grammar Dql;


options { output=AST; }


tokens {
    TEXT_CRIT='text';
    INT_CRIT='int';
    DATE_CRIT='date';
    SET_CRIT='set';
    SET_VALUES='values';
}


@lexer::header {
package uk.gov.nationalarchives.droid.dql;
}

@parser::header {
package uk.gov.nationalarchives.droid.dql;
}

entry
    : criterion
    -> ^(criterion)
    ;

criterion
    :   (text_criterion | int_criterion | date_criterion | set_criterion)
    ;

text_criterion
    :   fieldname text_operator string_value
    -> ^('text' fieldname text_operator string_value)
    ;

int_criterion
    :   fieldname numeric_operator int_value
    -> ^('int' fieldname numeric_operator int_value)
    ;

date_criterion
    :   fieldname date_operator date_value
    -> ^('date' fieldname date_operator date_value)
    ;
    
set_criterion
    :   fieldname set_operator set_values
    -> ^('set' fieldname set_operator set_values)
    ;

fieldname
    :   ID
    ;

text_operator
    :   ( EQ | NE | BEGINS | CONTAINS | ENDS | NOT_CONTAINS | NOT_STARTS | NOT_ENDS )
    ;
    
numeric_operator
    :   ( LTE | LT | EQ | GTE | GT | NE )
    ;
    
date_operator
    :   ( LTE | LT | EQ | GTE | GT | NE )
    ;
    
set_operator
    :   ( ANY_OF | NONE_OF )
    ;

string_value    
    :   STRING 
    ;

int_value
    :   INT
    ; 
    
set_values
    :   set_value+
    -> ^('values' set_value+)
    ;

set_value
    :   ID
    ; 
    
date_value
    :   ISO8601_DATE;


ANY_OF       : 'any'      | 'ANY';
NONE_OF      : 'none'     | 'NONE';
CONTAINS     : 'contains' | 'CONTAINS';
BEGINS       : 'starts '  | 'STARTS';
ENDS         : 'ends'     | 'ENDS';
NOT_CONTAINS : 'not contains' | 'NOT CONTAINS';
NOT_STARTS   : 'not starts'   | 'NOT STARTS';
NOT_ENDS     : 'not ends'     | 'NOT ENDS';

LTE :   '<=';
LT  :   '<';
EQ  :   '=';
GTE :   '>=';
GT  :   '>';
NE  :   '<>';

ID  :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_'|'/')*
    ;

STRING
    :  '\'' ( ESC_SEQ | ~('\\'|'\''))* '\''
    ;
    
INT :   ('0'..'9')+;

ISO8601_DATE    :   YEAR '-' MONTH '-' DAY;

fragment
YEAR    :   ('0'..'9')('0'..'9')('0'..'9')('0'..'9');

fragment
MONTH   :   ('0'..'1')('1'..'9');
    
fragment
DAY :   ('0'..'3')('0'..'9');

fragment
HOUR    :   ('0'..'2')('0'..'9');

fragment
MIN :   ('0'..'5')('0'..'9');

fragment
SEC :   ('0'..'5')('0'..'9');

WS  :   (' '|'\t')+ {skip();};

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;
