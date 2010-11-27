package tech.vineyard.httpclient.test;

import tech.vineyard.httpclient.JobQueue;
import tech.vineyard.httpclient.SyncDeadlinkChecker;


public class HttpClientTest extends AbstractHttpClientTest {
	
	private SyncDeadlinkChecker syncDeadlinkChecker = new SyncDeadlinkChecker();

	public void doRequest(JobQueue queue) {
		syncDeadlinkChecker.doRequests(queue);
	}


}
