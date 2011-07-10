package org.jside.jsi.tools.util.test;

import java.io.IOException;

import org.mozilla.javascript.ScriptOrFnNode;

class ScriptRuntime{

	/**
	 * @see  org.mozilla.javascript.ScriptRuntime#isJSLineTerminator(int)
	 * @param c
	 * @return
	 */
	public static boolean isJSLineTerminator(int c) {
        // Optimization for faster check for eol character:
        // they do not have 0xDFD0 bits set
        if ((c & 0xDFD0) != 0) {
            return false;
        }
        return c == '\n' || c == '\r' || c == 0x2028 || c == 0x2029;
	}

	/**
	 * 
	 * @see  org.mozilla.javascript.ScriptRuntime#stringToNumber
	 * @param numString
	 * @param i
	 * @param base
	 * @return
	 */
	public static double stringToNumber(String numString, int i, int base) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
class Parser{
	public Parser compilerEnv = this;
	private String sourceURI;
	private TokenStream ts;
    /*
     * Build a parse tree from the given sourceString.
     *
     * @return an Object representing the parsed
     * program.  If the parse fails, null will be returned.  (The
     * parse failure will result in a call to the ErrorReporter from
     * CompilerEnvirons.)
     */

	public void addError(String string) {
		
	}

	public void addWarning(String string, String str) {
		// TODO Auto-generated method stub
		
	}

	public RuntimeException reportError(String string) {
		return new RuntimeException(string);
	}

	public boolean isReservedKeywordAsIdentifier() {
		return false;
	}
}
class ObjToIntMap{

	public ObjToIntMap(int i) {
	}
	/**
	 * @see org.mozilla.javascript.ObjToIntMap#intern(Object)
	 * @param str
	 * @return
	 */
	public String intern(String str) {
		return str.intern();
	}
	
}

class Kit{
    /**
     * Throws RuntimeException to indicate failed assertion.
     * The function never returns and its return type is RuntimeException
     * only to be able to write <tt>throw Kit.codeBug()</tt> if plain
     * <tt>Kit.codeBug()</tt> triggers unreachable code error.
     */
    public static RuntimeException codeBug()
        throws RuntimeException
    {
        RuntimeException ex = new IllegalStateException("FAILED ASSERTION");
        // Print stack trace ASAP
        ex.printStackTrace(System.err);
        throw ex;
    }
    /**
     * If character <tt>c</tt> is a hexadecimal digit, return
     * <tt>accumulator</tt> * 16 plus corresponding
     * number. Otherise return -1.
     */
    public static int xDigitToInt(int c, int accumulator)
    {
        check: {
            // Use 0..9 < A..Z < a..z
            if (c <= '9') {
                c -= '0';
                if (0 <= c) { break check; }
            } else if (c <= 'F') {
                if ('A' <= c) {
                    c -= ('A' - 10);
                    break check;
                }
            } else if (c <= 'f') {
                if ('a' <= c) {
                    c -= ('a' - 10);
                    break check;
                }
            }
            return -1;
        }
        return (accumulator << 4) | c;
    }
}