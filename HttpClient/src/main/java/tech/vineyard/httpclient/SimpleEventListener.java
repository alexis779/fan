package tech.vineyard.httpclient;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.protocol.EventListener;

public class SimpleEventListener implements EventListener {
	private static final Log LOG = LogFactory.getLog(HttpClientNio2.class);

	public void connectionClosed(NHttpConnection conn) {
	}

	public void connectionOpen(NHttpConnection conn) {
	}

	public void connectionTimeout(NHttpConnection conn) {
	}

	public void fatalIOException(IOException ioe, NHttpConnection conn) {
		LOG.info("Fatal exception", ioe);
	}

	public void fatalProtocolException(HttpException ex, NHttpConnection conn) {
	}

}
