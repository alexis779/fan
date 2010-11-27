package tech.vineyard.httpclient;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 * @author alex
 *
 */
public class SyncDeadlinkChecker extends AbstractDeadlinkChecker {
	private static final Log LOG = LogFactory.getLog(SyncDeadlinkChecker.class);
	
	private static final int MAX_THREADS = 500;

	private HttpClient httpClient;
	
	private Object monitor = new Object();
	
	/**
	 * Number of pending connections.
	 */
	private Integer connections = new Integer(0);

	public SyncDeadlinkChecker() {
		super(null);
		setClient();
		
	}
	protected SyncDeadlinkChecker(String input) {
		super(input);
		setClient();		
	}

	public void checkUrls() {
		// queue of taks
		final Queue<JobQueue> queue = new ConcurrentLinkedQueue<JobQueue>();
		
		// Thread pool
		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
		
		Runnable runnable = new Runnable() {
			public void run() {
				// keep running till the common queue is empty
				while (queue.size() > 0) {
					JobQueue urlList = queue.poll();
					doRequests(urlList);
				}
			}				
		};

		
		Iterator<JobQueue> iterator = getIterator();
		while (iterator.hasNext()) {			
			queue.add(iterator.next());
			threadPoolExecutor.execute(runnable);
		}
		
		synchronized(monitor) {
			try {
				monitor.wait();
			} catch (InterruptedException ie) {
				LOG.warn(ie);
			}
		}

		threadPoolExecutor.shutdown();
		httpClient.getConnectionManager().shutdown();

	}
	
	/**
	 * Establish a connection to fetch a list of urls from a common host. 
	 * @see tech.vineyard.httpclient.AbstractDeadlinkChecker#doRequests(tech.vineyard.httpclient.JobQueue)
	 */
	public void doRequests(JobQueue urlList) {
		synchronized(connections) {
			++connections;
		}
      	HttpHost httpHost = new HttpHost(urlList.getHost());
      	
		while (! urlList.isEmpty()) {
			Job2 job = urlList.poll();
			
	      	// Do a HEAD request
	      	HttpRequest httpRequest = new HttpHead(job.getPath());

			String url = urlList.getUrl(job);
			LOG.info("Submitting " + url);

			try {
				HttpResponse httpResponse = httpClient.execute(httpHost, httpRequest);
				StatusLine statusLine = httpResponse.getStatusLine();
				LOG.info(statusLine.getStatusCode() + " on " + url);
				
			} catch (ClientProtocolException cpe) {
				LOG.info("CPE on " + url, cpe);
			} catch (IOException ioe) {
				LOG.info("IOE on " + url, ioe);
			}

		}
		
		synchronized(connections) {
			--connections;
			if (connections == 0) {
				synchronized(monitor) {
					monitor.notify();
				}

			}
		}
	}

	private void setClient() {
		HttpParams httpParams = new BasicHttpParams();
		httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(schemeRegistry);
		threadSafeClientConnManager.setMaxTotal(MAX_THREADS);
		threadSafeClientConnManager.setDefaultMaxPerRoute(1);
		
		httpClient = new DefaultHttpClient(threadSafeClientConnManager, httpParams);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			LOG.info("Usage: java tech.vineyard.httpclient.SyncDeadlinkChecker <inputFile>");
			LOG.info("Where inputFile is a list of tab separated key/value pairs.");
			return;
		}
		SyncDeadlinkChecker d = new SyncDeadlinkChecker(args[0]);
		d.checkUrls();
	}

}
