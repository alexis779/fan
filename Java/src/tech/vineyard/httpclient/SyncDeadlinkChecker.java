package tech.vineyard.httpclient;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

public class SyncDeadlinkChecker {
	private static final Log LOG = LogFactory.getLog(SyncDeadlinkChecker.class);
	
	private static final int MAX_THREADS = 10000;

	private HttpClient m_httpClient;
	
	public void checkUrls() {
		setClient();
		// Queue of tasks
		final Queue<JobQueue> queue = new ConcurrentLinkedQueue<JobQueue>();
		
		// Thread pool
		BlockingQueue<Runnable> blockingQueue = new LinkedBlockingDeque<Runnable>();
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 5, TimeUnit.SECONDS, blockingQueue );
		
		Iterator<JobQueue> iterator = getIterator();
		while (iterator.hasNext()) {
			
			queue.add(iterator.next());
			Runnable runnable = new Runnable() {

				public void run() {
					// keep running till the common queue is empty
					while (queue.size() > 0) {
						JobQueue urlList = queue.poll();
						doRequests(urlList);
						
					}
				}				
			};
			threadPoolExecutor.execute(runnable);
		}
		
		/*
		// TODO terminate properly when all the urls are downloaded
		threadPoolExecutor.shutdown();
		m_httpClient.getConnectionManager().shutdown();
		*/
	}
	
	private void doRequests(JobQueue urlList) {
		for (Job2 job: urlList) {
			doRequest(urlList, job);
		}
	}

	public void doRequest(JobQueue urlList, Job2 job) {
		String url = "http://" + urlList.getHost() + job.getPath();
      	URI uri = URI.create(url);
      	
      	HttpUriRequest httpUriRequest = null;
      	
      	String method = job.getMethod(); 
      	if (method.equals("HEAD")) {
          	httpUriRequest = new HttpHead(uri);
      	} else if (method.equals("GET")) {
          	httpUriRequest = new HttpGet(uri);
      	} else {
      		return;
      	}

		LOG.debug(url);
      	try {
			HttpResponse httpResponse = m_httpClient.execute(httpUriRequest);
			StatusLine statusLine = httpResponse.getStatusLine();
			LOG.debug(statusLine.getStatusCode() + " on " + url);
			
		} catch (ClientProtocolException cpe) {
			LOG.info("CPE on " + url);
			LOG.warn(cpe);
		} catch (IOException ioe) {
			LOG.info("IOE on " + url);
			LOG.warn(ioe);
		}

	}

	private void setClient() {
		HttpParams httpParams = new BasicHttpParams();
		httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(schemeRegistry);
		threadSafeClientConnManager.setMaxTotalConnections(MAX_THREADS);
		threadSafeClientConnManager.setDefaultMaxPerRoute(1);
		
		m_httpClient = new DefaultHttpClient(threadSafeClientConnManager, httpParams);
	}
	
	private Iterator<JobQueue> getIterator() {
		return new UrlListIterator();
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SyncDeadlinkChecker d = new SyncDeadlinkChecker();
		d.checkUrls();
	}

}
