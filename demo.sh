#!/bin/bash 

pcregrep -n -Mi '<gloss n="type1">副詞</gloss>(\n.*){0,7}<gloss n="lemma">必ず</gloss>' out/test-stack.xml
sed '136q;d' out/test-stack.xml | grep -o '".*"' | tr -d '"'
