package tech.vineyard.httpclient;

import java.net.URI;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class UrlListIterator implements Iterator<JobQueue> {
	private static final Log LOG = LogFactory.getLog(UrlListIterator.class);
	private UrlIterator m_urlIterator;
	private String m_host;
	private String m_url;

	public UrlListIterator(String input) {
		m_urlIterator = new UrlIterator(input);
	}

	public boolean hasNext() {
		return m_urlIterator.hasNext();
	}

	public JobQueue next() {
		JobQueue urlList = new JobQueue();
		if (m_url != null) {
			add(urlList, m_url);
			urlList.setHost(m_host);
		}
		
		while (m_urlIterator.hasNext()) {
			String line = m_urlIterator.next();
			String[] tokens = line.split("\t");
			String host2 = tokens[0];
			String url = tokens[1];
			if (m_host == null) {
				m_host = host2;
				urlList.setHost(m_host);
			}
			if (m_host.equals(host2)) {
				add(urlList, url);
			} else {
				m_host = host2;
				m_url = url;
				break;
			}
		}
		return urlList;
	}

	private void add(JobQueue urlList, String url) {
		URI uri;
		try {
			uri = URI.create(url);
		} catch(IllegalArgumentException ie) {
			LOG.warn("Unable to parse url " + url);
			return ;
		}

		String path = JobQueue.getPath(uri);
		urlList.add(new Job2("HEAD", path));
	}

	public void remove() {
		next();
	}

}
