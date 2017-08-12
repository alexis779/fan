package tech.vineyard.httpclient;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AsyncDeadlinkChecker extends AbstractDeadLinkChecker {
	private static final Log LOG = LogFactory.getLog(AsyncDeadlinkChecker.class);
	
	private HttpClientNio2 client = new HttpClientNio2();

	public AsyncDeadlinkChecker(String input) {
		super(input);
	}

	public void checkUrls() {
		
		try {
			this.client.startClient();
		} catch (IOException ioe) {
			LOG.warn(ioe);
		}
		
		Iterator<JobQueue> iterator = getIterator();
		
		while (iterator.hasNext()) {
			JobQueue queue = iterator.next();
			doRequests(queue);

		}
		
		this.client.waitClient();

	}

	protected void doRequests(JobQueue queue) {
		this.client.request(queue);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			LOG.info("Usage: java tech.vineyard.httpclient.AsyncDeadlinkChecker <inputFile>");
			LOG.info("Where inputFile is a list of tab separated key/value pairs.");
			return;
		}
		AsyncDeadlinkChecker d = new AsyncDeadlinkChecker(args[0]);
		d.checkUrls();
	}

}
