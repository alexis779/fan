package tech.vineyard.parser;// Generated from grammar/hackerrank/input/SameOccurrence.g4 by ANTLR 4.7

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides an empty implementation of {@link SameOccurrenceListener},
 * which can be extended to create a listener which only needs to handle a subset
 * of the available methods.
 */
public class SameOccurrenceParseListener implements SameOccurrenceListener {
	private static final Logger LOG = LogManager.getLogger(SameOccurrenceParseListener.class);
	private static final int TWO = 2;

	int n, q;
	List<Integer> a;
	long start = 0;

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterR(SameOccurrenceParser.RContext ctx) {
		start = System.currentTimeMillis();
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitR(SameOccurrenceParser.RContext ctx) {
		long end = System.currentTimeMillis();
		LOG.info(" Rule r in {} ms", (end-start));
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterHeader(SameOccurrenceParser.HeaderContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitHeader(SameOccurrenceParser.HeaderContext ctx) {
		List<Integer> pair = toIntList(ctx.pair().INT());
		assert pair.size() == TWO;
		n = pair.get(0);
		q = pair.get(1);
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterPair(SameOccurrenceParser.PairContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitPair(SameOccurrenceParser.PairContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterArray(SameOccurrenceParser.ArrayContext ctx) {}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitArray(SameOccurrenceParser.ArrayContext ctx) {
		a = toIntList(ctx.INT());
		assert a.size() == n;
	}
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterQueries(SameOccurrenceParser.QueriesContext ctx) {}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitQueries(SameOccurrenceParser.QueriesContext ctx) {
		List<SameOccurrenceParser.PairContext> pairs = ctx.pair();
		assert pairs.size() == q;
		pairs.forEach(
				pair -> queryPair(toIntList(pair.INT())));
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void enterEveryRule(ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void exitEveryRule(ParserRuleContext ctx) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitTerminal(TerminalNode node) { }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation does nothing.</p>
	 */
	@Override public void visitErrorNode(ErrorNode node) { }

	private List<Integer> toIntList(List<TerminalNode> ctx) {
		return ctx.stream()
				.map(TerminalNode::getText)
				.map(Integer::parseInt)
				.collect(Collectors.toList());
	}

	private void queryPair(List<Integer> pair) {
		assert pair.size() == TWO;
		int i = pair.get(0);
		int j = pair.get(1);
		query(i, j);
	}

	private void query(int i, int j) {
	}

}
