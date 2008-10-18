package org.xidea.jsel.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsel.ConstantsToken;
import org.xidea.jsel.ExpressionToken;
import org.xidea.jsel.ExpressionTokenizer;
import org.xidea.jsel.LazyToken;
import org.xidea.jsel.OperatorToken;
import org.xidea.jsel.VarToken;

public class ExpressionTokenizerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test3op() {
		ExpressionTokenizer tokenizer = new ExpressionTokenizer("a?b:c");
		assertArrayEquals(new ExpressionToken[] { new VarToken("a"),
		//
				new LazyToken(), new VarToken("b"), LazyToken.LAZY_TOKEN_END,
				//
				OperatorToken.getToken(ExpressionToken.TYPE_QUESTION),
				//
				new LazyToken(), new VarToken("c"), LazyToken.LAZY_TOKEN_END,
				//
				OperatorToken.getToken(ExpressionToken.TYPE_QUESTION_SELECT) },
				tokenizer.toArray());
		//tokenizer = new ExpressionTokenizer("a?b:c+d?e:f");//(a?(b:c+d))?(e:f)
		tokenizer = new ExpressionTokenizer("a?b?c:d+e:f");//a?(b?c:d+e):f
		assertArrayEquals(new ExpressionToken[] { new VarToken("a"),
		//
				new LazyToken(), new VarToken("b"), LazyToken.LAZY_TOKEN_END,
				//
				OperatorToken.getToken(ExpressionToken.TYPE_QUESTION),
				//
				new LazyToken(), new VarToken("c"), LazyToken.LAZY_TOKEN_END,
				//
				OperatorToken.getToken(ExpressionToken.TYPE_QUESTION_SELECT) },
				tokenizer.toArray());
	}

	@Test
	public void testSimple() {
		ExpressionTokenizer tokenizer = new ExpressionTokenizer("1+1+2");
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new ConstantsToken(1),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD),
				new ConstantsToken(2),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD) }, tokenizer
				.toArray());

		tokenizer = new ExpressionTokenizer("1+(1+2)");//112++
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new ConstantsToken(1), new ConstantsToken(2),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD) }, tokenizer
				.toArray());

		tokenizer = new ExpressionTokenizer("1+1*2");
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new ConstantsToken(1), new ConstantsToken(2),
				OperatorToken.getToken(ExpressionToken.TYPE_MUL),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD) }, tokenizer
				.toArray());

		tokenizer = new ExpressionTokenizer("1+1*2*3");
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new ConstantsToken(1), new ConstantsToken(2),
				OperatorToken.getToken(ExpressionToken.TYPE_MUL),
				new ConstantsToken(3),
				OperatorToken.getToken(ExpressionToken.TYPE_MUL),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD) 
				}, tokenizer
				.toArray());
		tokenizer = new ExpressionTokenizer("1+1*2+3");//112*+3+
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new ConstantsToken(1), new ConstantsToken(2),
				OperatorToken.getToken(ExpressionToken.TYPE_MUL),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD),
				new ConstantsToken(3),
				OperatorToken.getToken(ExpressionToken.TYPE_ADD) 
				}, tokenizer
				.toArray());

		tokenizer = new ExpressionTokenizer("1||2");
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new LazyToken(), new ConstantsToken(2),
				LazyToken.LAZY_TOKEN_END,
				OperatorToken.getToken(ExpressionToken.TYPE_OR) }, tokenizer
				.toArray());

		tokenizer = new ExpressionTokenizer("1||1&&2");
		assertArrayEquals(new ExpressionToken[] { new ConstantsToken(1),
				new LazyToken(), new ConstantsToken(1), new LazyToken(),
				new ConstantsToken(2), LazyToken.LAZY_TOKEN_END,
				OperatorToken.getToken(ExpressionToken.TYPE_AND),
				LazyToken.LAZY_TOKEN_END,
				OperatorToken.getToken(ExpressionToken.TYPE_OR)

		}, tokenizer.toArray());
	}
}
