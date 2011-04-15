/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.antlr;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CharStream;

import java.io.IOException;
import java.io.Reader;

/**
 * For use with ANTLR to create a case-insensitive token stream.
 */
public class NoCaseSensitiveStream extends ANTLRReaderStream
{
    /**
     * Ctor.
     * @param reader is the reader providing the characters to inspect
     * @throws IOException to indicate IO errors
     */
    public NoCaseSensitiveStream(Reader reader)
            throws IOException
    {
        super(reader);
    }

	public int LA(int i) {
		if ( i==0 ) {
			return 0; // undefined
		}
		if ( i<0 ) {
			i++; // e.g., translate LA(-1) to use offset 0
		}
		if ( (p+i-1) >= n ) {
            return CharStream.EOF;
        }
        return Character.toLowerCase(data[p+i-1]);
    }
}
