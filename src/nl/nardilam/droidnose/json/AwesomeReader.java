package nl.nardilam.droidnose.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class AwesomeReader
{
    private final Reader reader;
    
	public AwesomeReader(Reader reader)
	{
		if (!reader.markSupported())
			reader = new BufferedReader(reader);
		this.reader = reader;
	}
    
    /*
     * These methods read until they find one of the characters in 'chars' and then return the matching char,
     * leaving the reader either just before or just after the matched character.
     */    
    public char readPast(char... chars) throws IOException, MissingCharException
	{
		return readPast(null, chars);
	}
	
	public char readPast(StringBuffer buffer, char... chars) throws IOException, MissingCharException
	{
		char c = readUntil(buffer, chars);
		reader.skip(1);
		reader.mark(1);
		return c;
	}
	
	public char readUntil(char... chars) throws IOException, MissingCharException
	{
		return readUntil(null, chars);
	}
	
	public char readUntil(StringBuffer buffer, char... chars) throws IOException, MissingCharException
	{
		int readInt;
        char readChar;
        
        /*
         * Mark current position in case the first character matches.
         */
        reader.mark(1);
        
        /* -1 means EOF */
		while ((readInt = reader.read()) != -1)
        {
            readChar = (char)readInt;
            
            /*
             * Check if read char matches one of the required chars. If
             * so, go back to just before it and return the matched char.
             */
            for (char c : chars)
            {
            	if (readChar == c)
            	{
            		reader.reset();
            		return c;
            	}
            }
            
            /* If a buffer was provided, store the non-matching data there for possible later use. */
            if (buffer != null)
            	buffer.append(readChar);
            
            reader.mark(1);
        }
		
		throw new MissingCharException("EOF reached before char found");
	}
}
