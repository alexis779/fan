package tech.vineyard.httpclient;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AsyncDeadlinkChecker {
	private static final Log LOG = LogFactory.getLog(AsyncDeadlinkChecker.class);
	
	private HttpClientNio2 client = new HttpClientNio2();
	
	public void checkUrls() {
		
		try {
			this.client.startClient();
		} catch (IOException ioe) {
			LOG.warn(ioe);
		}
		
		Iterator<JobQueue> iterator = getIterator();
		
		while (iterator.hasNext()) {
			JobQueue queue = iterator.next();

			this.client.request(queue);
		}
		
		this.client.waitClient();

	}

	private Iterator<JobQueue> getIterator() {
		return new UrlListIterator();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AsyncDeadlinkChecker d = new AsyncDeadlinkChecker();
		d.checkUrls();
	}

}
