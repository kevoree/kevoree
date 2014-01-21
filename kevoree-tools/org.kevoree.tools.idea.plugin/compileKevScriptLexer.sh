#!/bin/sh

if [ -z "$IDEA_HOME" ]; then
    IDEA_HOME=/Users/duke/Documents/dev/sandbox/idea/
fi

${IDEA_HOME}/tools/lexer/jflex-1.4/bin/jflex \
    --table \
    --skel ${IDEA_HOME}/tools/lexer/idea-flex.skeleton \
    --charat --nobak \
    -d src/org/kevoree/tools/kevscript/idea/lexer \
    src/org/kevoree/tools/kevscript/idea/lexer/KevScript.flex

