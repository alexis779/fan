package tech.vineyard.httpclient;

import org.apache.http.nio.reactor.IOReactorException;

import tech.vineyard.httpclient.HttpClientNio2;
import tech.vineyard.httpclient.Job2;
import tech.vineyard.httpclient.JobQueue;
import tech.vineyard.httpclient.RequestExecutionHandler;

public class TestHttpClientNio2 extends HttpClientNio2 {

	public TestHttpClientNio2() throws IOReactorException {
		super();
	}
	
	@Override
	public void request(JobQueue jobQueue) {
		super.request(jobQueue);
		
		while(! jobQueue.isEmpty()) {
			Job2 job = jobQueue.peek();
			synchronized(job) {
				try {
					job.wait();
				} catch (InterruptedException ie) {
					LOG.warn(ie);
				}
			}
		}
		
	}
	
	@Override
    protected RequestExecutionHandler createRequestExecutionHandler() {
		return new RequestExecutionHandler() {
			@Override
			protected void finalizeJob(Job2 job) {
		        synchronized(job) {
		        	job.notify();
		        }
			}
			
		};
	}

}
