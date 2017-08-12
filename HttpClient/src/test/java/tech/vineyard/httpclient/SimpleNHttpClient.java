package tech.vineyard.httpclient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleNHttpClient {
	private static final Log LOG = LogFactory.getLog(SimpleNHttpClient.class);
	private static final String CRLF = "\r\n";
	private static final int BUFFER_SIZE = 1024;
	
	private Selector selector;
	private ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
	private CharBuffer charBuffer = CharBuffer.allocate(BUFFER_SIZE);
	
	// TODO choose an encoding according to charset value in Content-type header?
	private CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();
	private CharsetDecoder charsetDecoder = Charset.forName("UTF-8").newDecoder();
	
	private String host;
	private int port;
	private Queue<String> requestQueue = new LinkedList<String>();
	
	/**
	 * Counter to keep track of the number of responses that were received.
	 */
	private int responseReceived;
	
	SimpleNHttpClient() {
		this.port = 80;
		String path1;
		String path2;
		
		this.host = "video.tvguide.com";
		path1 = "/Date+Night+2010/Date+Night/4866938?autoplay=true%20partnerid=OVG";
		path2 = "/Brooks++Dunn/Put+a+Girl+in+It/5445966?autoplay=true%20partnerid=OVG";
		
		requestQueue.add(path1);
		requestQueue.add(path2);
		
		responseReceived = requestQueue.size();
		
		try {
			this.selector = Selector.open();
			connect();
			
			while (true) {
				this.selector.select();
				
				postSelect();
				
				if (responseReceived == 0) {
					LOG.debug("All responses were received");
					break;
				}
			}
		} catch (IOException ioe) {
			LOG.debug(ioe);
		}
	}
	
	private void postSelect() throws IOException {
		Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
		for (Iterator<SelectionKey> it = selectedKeys.iterator(); it.hasNext(); ) {
    			SelectionKey key = it.next();
    			processKey(key);
		}
		selectedKeys.clear();
        
	}

	
	/**
	 * Writing in the socket via the writable key does not mean we will receive the corresponding response through next readable key.
	 * We attach the job to the key when polling from the queue, so that we can add it again if we see an EOF instead of a valid response.
	 * @param key The selected key.
	 * @throws IOException
	 */
	private void processKey(SelectionKey key) throws IOException {
		LOG.debug("Selected key's ready operations: " + key.readyOps());
		if (key.isConnectable()) {
			LOG.debug("Key is connectable");
			SocketChannel socketChannel = (SocketChannel) key.channel();
			
			boolean connected = socketChannel.finishConnect();
			LOG.debug("Connected: " + connected);
			if (! connected) {
				key.cancel();
				return;
			}
			
			socketChannel.register(this.selector, SelectionKey.OP_WRITE);
			
		}
		
		if (key.isWritable()) {
			LOG.debug("Key is writable");
			
			String path = requestQueue.poll();
			if (path == null) {
				LOG.debug("The queue is empty.");
				return;
			}

			SocketChannel socketChannel = (SocketChannel) key.channel();

			this.charBuffer.clear();
			loadRequest(path);

			this.charBuffer.flip();
			String request = charBuffer.toString(); 
			LOG.debug(request);
			
			this.byteBuffer.clear();
			this.charsetEncoder.encode(charBuffer, this.byteBuffer, true);
			
			this.byteBuffer.flip();
						
			int bytesWritten = socketChannel.write(this.byteBuffer);
			LOG.debug("Number of bytes written: " + bytesWritten);

			SelectionKey readableKey = socketChannel.register(this.selector, SelectionKey.OP_READ);
			readableKey.attach(path);
		}

		if (key.isReadable()) {
			LOG.debug("Key is readable");
			SocketChannel socketChannel = (SocketChannel) key.channel();
						
			this.byteBuffer.clear();
			int bytesRead = socketChannel.read(this.byteBuffer);
			LOG.debug("Number of bytes read: " + bytesRead);
			
			// Detect if the first read returned EOF
			if (bytesRead == -1) {
				LOG.debug("EOF was reached");
				String requestedPath = (String) key.attachment();
				
				key.cancel();
				socketChannel.close();
				
				LOG.debug("Adding again " + requestedPath);
				this.requestQueue.add(requestedPath);
				
				connect();
				return;
			}

			// TODO check that another read returns either -1(EOF) or 0
			
			this.byteBuffer.flip();
			this.charBuffer.clear();
			this.charsetDecoder.decode(this.byteBuffer, this.charBuffer, false);
			
			this.charBuffer.flip();			
			String response = this.charBuffer.toString();
			LOG.debug(response);
			
			--responseReceived;
			
			socketChannel.register(this.selector, SelectionKey.OP_WRITE);
			
		}
		
		
		this.selector.wakeup();
		
	}

	private void loadRequest(String path) {
		writeLine("HEAD " + path + " HTTP/1.1");
		writeLine("Connection: Keep-Alive");
		writeLine("Host: " + this.host);
		writeLine("User-Agent: TEST-CLIENT/1.1");
		writeLine("");
	}

	private void writeLine(String line) {
		this.charBuffer.append(line);
		this.charBuffer.append(CRLF);
	}

	private void connect() {
		try {
			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			
			SocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
			boolean connected = socketChannel.connect(socketAddress);
			LOG.debug("Connected non blocking: " + connected);
			if (! connected) {
				socketChannel.register(this.selector, SelectionKey.OP_CONNECT);
			}
			
		} catch (IOException ioe) {
			LOG.debug(ioe);
		}
		
	}
	
	public static void main(String[] args) {
		new SimpleNHttpClient();
	}

}
