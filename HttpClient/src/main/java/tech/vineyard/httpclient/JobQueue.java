package tech.vineyard.httpclient;

import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.IOSession;



public class JobQueue extends ConcurrentLinkedQueue<Job2> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7969636540228940190L;
	private static final Log LOG = LogFactory.getLog(JobQueue.class);
	/**
	 * Depth allowed before pruning the redirect following.
	 */
	private static final int MAX_REDIRECTS = 5;
	private String host;	
	private boolean connected;
	
	/**
	 * Sessions associated to the connection requests that successfully completed.
	 */
	private Queue<IOSession> sessions = new LinkedList<IOSession>();
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	
	public String getUrl(Job2 job) {
		return "http://" + getHost() + job.getPath(); 
	}
	
	public static String getPath(URI uri) {
		String path = uri.getRawPath();
		if (path.equals("")) {
			path = "/";
		}
		String query = uri.getRawQuery();
		if (query != null) {
			path = path + "?" + query;
		}
		return path;
	}

	public boolean isConnected() {
		return connected;
	}
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public void followRedirect(Job2 job, String location) {
		if (job.getRedirects() == MAX_REDIRECTS-1) {
			LOG.debug("Max redirects reached, stop following");
			return;
		}
		
		URI uri = null;
		try {
			uri = URI.create(location);
		} catch(IllegalArgumentException ie) {
			LOG.warn("Unable to parse url " + location);
			return;
		}
		String path = location;
		
		if (uri.isAbsolute()) {
			if (! uri.getHost().equals(this.host)) {
				// TODO connect to a different server
				LOG.info("Absolute URL to a different host: " + location);
				return;
			}
			path = getPath(uri);
		}
		Job2 followJob = new Job2(job.getMethod(), path);
		followJob.setRedirects(job.getRedirects()+1);
		add(followJob);
	}
	
	public void close() {
		while (! this.sessions.isEmpty()) {
			IOSession ioSession = this.sessions.poll();
			ioSession.close();
		}
		
		setConnected(false);
	}
	public void addSession(IOSession session) {
		this.sessions.add(session);		
	}
}
