package tech.vineyard.httpclient;

import java.io.IOException;

import org.junit.Before;

import tech.vineyard.httpclient.JobQueue;

public class TestNHttpClient extends AbstractTestHttpClient {
	private TestHttpClientNio2 client;	

	@Before
	public void start() throws IOException {
		this.client = new TestHttpClientNio2();		
		this.client.startClient();		
	}

	public void doRequest(JobQueue queue) {
		this.client.request(jobList);		
	}

}
