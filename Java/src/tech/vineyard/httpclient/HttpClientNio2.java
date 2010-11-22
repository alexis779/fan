package tech.vineyard.httpclient;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.nio.DefaultClientIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.protocol.AsyncNHttpClientHandler;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.SessionRequest;
import org.apache.http.nio.reactor.SessionRequestCallback;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestDate;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;

public class HttpClientNio2 {
	protected static final Log LOG = LogFactory.getLog(HttpClientNio2.class);

    private DefaultConnectingIOReactor ioReactor;
    private HttpParams params;

    private SessionRequestCallback sessionRequestCallback;
	private RequestExecutionHandler requestExecutionHandler;

	public void startClient() throws IOException {
		this.params = new SyncBasicHttpParams();
        this.ioReactor = new DefaultConnectingIOReactor(1, this.params);
		this.params
		.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 60000)
		.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000)
		.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
		.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true)
		.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
		.setParameter(CoreProtocolPNames.USER_AGENT, "TEST-CLIENT/1.1");

		this.requestExecutionHandler = createRequestExecutionHandler();
		this.requestExecutionHandler.setClient(this);

		//setExceptionHandler(new SimpleIOReactorExceptionHandler());
        this.sessionRequestCallback = new SimpleSessionRequestCallback(this.requestExecutionHandler);


		HttpProcessor clientHttpProc = new ImmutableHttpProcessor(
			new HttpRequestInterceptor[] {
				new RequestConnControl(),
				new RequestContent(),
				new RequestDate(),
				new RequestExpectContinue(),
				new RequestTargetHost(),
				new RequestUserAgent()
			});
		final AsyncNHttpClientHandler clientHandler = new AsyncNHttpClientHandler(clientHttpProc, requestExecutionHandler, new DefaultConnectionReuseStrategy(), this.params);
        clientHandler.setEventListener(new SimpleEventListener());

        Thread thread = new Thread(new Runnable() {
			public void run() {
				IOEventDispatch ioEventDispatch = new DefaultClientIOEventDispatch(clientHandler, params);
		        try {
					ioReactor.execute(ioEventDispatch);
				} catch (InterruptedIOException iioe) {
					LOG.warn(iioe);
				} catch (IOReactorException iore) {
					LOG.warn(iore);
				}
			}
        	
        });
        thread.start();
	}
	
	public void waitClient() {
		this.requestExecutionHandler.waitConnections();
		try {
			LOG.info("Shutting down reactor.");
			this.ioReactor.shutdown();
		} catch (IOException ioe) {
			LOG.warn(ioe);
		}
	}
	
    protected RequestExecutionHandler createRequestExecutionHandler() {
		return new RequestExecutionHandler();
	}
	public SessionRequest openConnection(final InetSocketAddress address, final Object attachment) {
		return this.ioReactor.connect(address, null, attachment, this.sessionRequestCallback);
    }

	/**
	 * @param jobQueue
	 */
	public void request(JobQueue jobQueue) {
		this.requestExecutionHandler.connect(jobQueue);
		
		/*
		if (! jobQueue.waitForConnection()) {
			LOG.debug("Skipping queue.");
			return;
		}
		*/
	}
	


}
