package tech.vineyard.httpclient;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.entity.BufferingNHttpEntity;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.protocol.NHttpRequestExecutionHandler;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class RequestExecutionHandler implements NHttpRequestExecutionHandler {
	private static final Log LOG = LogFactory.getLog(RequestExecutionHandler.class);

	/**
	 * Number of connections per host.
	 */
	private static final int CONNECTION_NUMBER = 1;
	
	private HttpClientNio2 client;
	
	private int connections;
	private int requests;
	private Object connectionMonitor = new Object();
	
	public void finalizeContext(HttpContext context) {
    	--connections;
    	
        Job2 job = (Job2) context.removeAttribute("job");
        JobQueue queue = (JobQueue) context.getAttribute("queue");
        
        if (job != null) {
            finalizeJob(job);
            if (job.getRedirects() == 0) {
            	LOG.info("E on " + queue.getUrl(job));
            }
        }
        
    	if (! queue.isEmpty()) {
    		LOG.debug("Reconnecting to " + queue.getHost());
    		connect(queue);
    	} else if (this.connections == 0 && this.requests == 0) {
			notifyConnections();
    	}
	}

	public int getConnections() {
		return connections;
	}

	public void handleResponse(HttpResponse response, HttpContext context) throws IOException {
        JobQueue queue = (JobQueue) context.getAttribute("queue");       
        Job2 job = (Job2) context.removeAttribute("job");

        int statusCode = response.getStatusLine().getStatusCode();
        if (job.getRedirects() == 0) {
        	LOG.info(statusCode + " on " + queue.getUrl(job));
        }
		
        String content = null;

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                content = EntityUtils.toString(entity);
                job.setContent(content);
            } catch (IOException ioe) {
                LOG.warn(ioe);
            }
        }
        
        Header header;
	   
        header = response.getFirstHeader("Location");
        if (header != null) {
        	String location = header.getValue();
        	queue.followRedirect(job, location);
        }
	   
        header = response.getFirstHeader("Connection");
        if (header != null) {
        	String connection = header.getValue();
	        if (connection.equals("close")) {
	    	    queue.close();
	        }
        }

        finalizeJob(job);
	}

	protected void finalizeJob(Job2 job) {
	}

	public void initalizeContext(HttpContext context, Object attachment) {
    	++connections;
        context.setAttribute("queue", attachment);
	}

	public ConsumingNHttpEntity responseEntity(HttpResponse response, HttpContext context) throws IOException {
        return new BufferingNHttpEntity(response.getEntity(), new HeapByteBufferAllocator());
	}

	public HttpRequest submitRequest(HttpContext context) {
        JobQueue queue = (JobQueue) context.getAttribute("queue");
        
        Job2 job = queue.poll();
        
        if (job != null) {
            context.setAttribute("job", job);
			LOG.info("Submitting " + queue.getUrl(job));
            return new BasicHttpRequest(job.getMethod(), job.getPath());
        } else {
        	LOG.debug("Queue is empty.");        	
        	queue.close();
            return null;
        }
	}

	public void waitConnections() {
		if (connections == 0 && requests == 0) {
			return;
		}
		synchronized(connectionMonitor) {
			try {
				LOG.info("Waiting monitor");
				connectionMonitor.wait();
			} catch (InterruptedException ie) {
				LOG.warn(ie);
			}
		}
	}
	
	private void notifyConnections() {
		synchronized(connectionMonitor) {
			LOG.info("Notifying monitor");
			connectionMonitor.notify();
		}
	}
	
	public void connect(JobQueue queue) {
		// TODO manage HTTPS
		int port = 80;
		for (int i = 0; i < CONNECTION_NUMBER; i++) {
			++requests;
			LOG.info("Opening connection to " + queue.getHost());
			// would return a SessionRequest
			this.client.openConnection(new InetSocketAddress(queue.getHost(), port), queue);
		}
	}

	public int getRequests() {
		return requests;
	}

	public void setRequests(int requests) {
		this.requests = requests;
	}

	public void setClient(HttpClientNio2 httpClientNio) {
		this.client = httpClientNio;
	}

}
