package org.kevoree.kevscript.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kevoree.ContainerRoot;
import org.kevoree.factory.DefaultKevoreeFactory;
import org.kevoree.kevscript.KevScriptEngine;
import org.kevoree.kevscript.Parser;
import org.kevoree.kevscript.Type;
import org.kevoree.log.Log;
import org.waxeye.ast.IAST;
import org.waxeye.input.InputBuffer;
import org.waxeye.parser.ParseResult;

import java.util.HashMap;
import java.util.Map;

public class ResolvingVersionsTest {
	private static final String KEVOREE_REGISTRY = "https://kevoree.braindead.fr";
	private final Parser parser = new Parser();
	private final Map<String, String> ctxVars = new HashMap<>();

	@Before
	public void beforeEach() {
		Log.set(Log.LEVEL_DEBUG);
		ctxVars.clear();
	}

	public void test1() throws Exception {
		final ParseResult<Type> parserResult = parser.parse(new InputBuffer("add javaNode:".toCharArray()));

		Assert.assertNotNull(parserResult.getError());

		/*
		 * ContainerRoot model = new
		 * DefaultKevoreeFactory().createContainerRoot(); final IAST<Type> ast =
		 * parserResult.getAST(); if (ast != null) { new
		 * KevScriptEngine().interpret(ast, model); System.out.println(model); }
		 * else { throw new Exception(parserResult.getError().toString()); }
		 */
	}

	@Test
	public void test2() throws Exception {
		final ParseResult<Type> parserResult = parser.parse(new InputBuffer("add javaNode: JavaNode".toCharArray()));
		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test3() throws Exception {
		final ParseResult<Type> parserResult = parser.parse(new InputBuffer("add javaNode: JavaNode/1".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test4() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/1.0.2".toCharArray()));

		Assert.assertNotNull(parserResult.getError());

	}

	@Test
	public void test5() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/LATEST".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test6() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/RELEASE".toCharArray()));

		Assert.assertNotNull(parserResult.getError());

	}

	@Test
	public void test7() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/1/RELEASE".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test8() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/LATEST/RELEASE".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test9() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/1/LATEST".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	public void test10() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/LATEST/LATEST".toCharArray()));

		Assert.assertNull(parserResult.getError());

	}

	@Test
	@Ignore
	public void test11() throws Exception {
		final ParseResult<Type> parserResult = parser.parse(new InputBuffer("add javaNode: JavaNode".toCharArray()));

		Assert.assertNull(parserResult.getError());

		ContainerRoot model = new DefaultKevoreeFactory().createContainerRoot();
		final IAST<Type> ast = parserResult.getAST();
		if (ast != null) {
			new KevScriptEngine(KEVOREE_REGISTRY).interpret(ast, model, ctxVars);
			System.out.println(model);
		} else {
			throw new Exception(parserResult.getError().toString());
		}

	}

	@Test
	@Ignore
	public void test12() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/1/LATEST".toCharArray()));

		Assert.assertNull(parserResult.getError());

		ContainerRoot model = new DefaultKevoreeFactory().createContainerRoot();
		final IAST<Type> ast = parserResult.getAST();
		if (ast != null) {
			new KevScriptEngine(KEVOREE_REGISTRY).interpret(ast, model, ctxVars);
			System.out.println(model);
		} else {
			throw new Exception(parserResult.getError().toString());
		}

	}

	@Test
	@Ignore
	public void test13() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add javaNode: JavaNode/LATEST/RELEASE".toCharArray()));

		Assert.assertNull(parserResult.getError());

		ContainerRoot model = new DefaultKevoreeFactory().createContainerRoot();
		final IAST<Type> ast = parserResult.getAST();
		if (ast != null) {
			new KevScriptEngine(KEVOREE_REGISTRY).interpret(ast, model, ctxVars);
			System.out.println(new DefaultKevoreeFactory().createJSONSerializer().serialize(model));
		} else {
			throw new Exception(parserResult.getError().toString());
		}
	}

	@Test
	@Ignore
	public void test14() throws Exception {
		final ParseResult<Type> parserResult = parser
				.parse(new InputBuffer("add %hello%: JavascriptNode/LATEST/LATEST".toCharArray()));

		Assert.assertNull(parserResult.getError());

		ContainerRoot model = new DefaultKevoreeFactory().createContainerRoot();
		final IAST<Type> ast = parserResult.getAST();
		if (ast != null) {
			ctxVars.put("hello", "world");
			new KevScriptEngine(KEVOREE_REGISTRY).interpret(ast, model, ctxVars);
			System.out.println(new DefaultKevoreeFactory().createJSONSerializer().serialize(model));
		} else {
			throw new Exception(parserResult.getError().toString());
		}
	}
}
