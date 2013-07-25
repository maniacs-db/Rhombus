package com.pardot.rhombus.cobject.async;

import com.datastax.driver.core.*;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.pardot.rhombus.cobject.BoundedCQLStatementIterator;
import com.pardot.rhombus.cobject.CQLExecutor;
import com.pardot.rhombus.cobject.CQLStatement;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static com.google.common.util.concurrent.Uninterruptibles.awaitUninterruptibly;

/**
 * Pardot, an ExactTarget company
 * User: Michael Frank
 * Date: 6/21/13
 */
public class StatementIteratorConsumer {

	private static Logger logger = LoggerFactory.getLogger(StatementIteratorConsumer.class);
	private static ExecutorService executorService = Executors.newFixedThreadPool(400);

	private final BoundedCQLStatementIterator statementIterator;
	private CQLExecutor cqlExecutor;
	private final CountDownLatch shutdownLatch;
	private final long timeout;

	public StatementIteratorConsumer(BoundedCQLStatementIterator statementIterator, CQLExecutor cqlExecutor, long timeout) {
		this.statementIterator = statementIterator;
		this.cqlExecutor = cqlExecutor;
		this.timeout = timeout;
		this.shutdownLatch = new CountDownLatch((new Long(statementIterator.size())).intValue());

	}

	public void start() {
		while(statementIterator.hasNext()) {
			final CQLStatement next = statementIterator.next();
			Runnable r = new Runnable() {
				@Override
				public void run() {
					handle(next);
				}
			};
			executorService.execute(r);
		}
	}

	public void join() {
		//logger.debug("awaitUninterruptibly with timeout {}ms", statementTimeout);
		awaitUninterruptibly(shutdownLatch, timeout, TimeUnit.MILLISECONDS);
	}

	protected void handle(CQLStatement statement) {
		ResultSetFuture future = this.cqlExecutor.executeAsync(statement);
		Futures.addCallback(future, new FutureCallback<ResultSet>() {
			@Override
			public void onSuccess(final ResultSet result) {
				Host queriedHost = result.getExecutionInfo().getQueriedHost();
				//logger.debug("queried host: {} in datacenter {}", queriedHost, queriedHost.getDatacenter());
				Metrics.defaultRegistry().newMeter(StatementIteratorConsumer.class, "queriedhost." + queriedHost.getDatacenter(), queriedHost.getDatacenter(), TimeUnit.SECONDS).mark();
				shutdownLatch.countDown();
			}
			@Override
			public void onFailure(final Throwable t) {
				//TODO Stop processing and return error
				logger.error("Error during async request: {}", t);
				shutdownLatch.countDown();
			}
		});
	}
}
