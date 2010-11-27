package tech.vineyard.httpclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;

public class SimpleSessionRequestCallback implements SessionRequestCallback {
	private static final Log LOG = LogFactory.getLog(SimpleSessionRequestCallback.class);

	private RequestExecutionHandler requestExecutionHandler;

	public SimpleSessionRequestCallback(RequestExecutionHandler requestExecutionHandler) {
		this.requestExecutionHandler = requestExecutionHandler;
	}

	public void timeout(SessionRequest request) {
		LOG.info("Session request timed-out");
		decrementRequests();
	}
	
	public void failed(SessionRequest request) {
		LOG.info("Session request failed", request.getException());
		
		decrementRequests();				
	}
	
	public void completed(SessionRequest request) {
		JobQueue queue = (JobQueue) request.getAttachment();
		queue.addSession(request.getSession());
		queue.setConnected(true);
		decrementRequests();				
	}
	
	public void cancelled(SessionRequest request) {
		LOG.info("Session request was cancelled");
		decrementRequests();
	}
	
	private void decrementRequests() {
		this.requestExecutionHandler.setRequests(this.requestExecutionHandler.getRequests() - 1);
	}

}
