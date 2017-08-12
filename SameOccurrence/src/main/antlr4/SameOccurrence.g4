grammar SameOccurrence ;
r: header array queries ;
header: pair ;
pair: INT INT NL ;
array: INT+ NL ;
queries: pair+ ;
INT : [0-9]+ ;
NL : [\r\n]+ ;
WS : [ \t]+ -> skip ;