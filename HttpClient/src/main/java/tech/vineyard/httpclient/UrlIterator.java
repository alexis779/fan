package tech.vineyard.httpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlIterator implements Iterator<String> {
	private static final Log LOG = LogFactory.getLog(UrlIterator.class);
	
	private BufferedReader m_bufferedReader;
	
	private String m_line;
	
	public UrlIterator(String input) {
		File file = new File(input);
		try {
			FileReader fileReader = new FileReader(file);
			m_bufferedReader = new BufferedReader(fileReader);
			
		} catch (FileNotFoundException fnfe) {
			LOG.error("File Not Found", fnfe);
		}
/*
			InputStream inputStream = System.in;
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			m_bufferedReader = new BufferedReader(inputStreamReader);
*/
		
		// read first line
		try {
			m_line = m_bufferedReader.readLine();
		} catch (IOException ioe) {
			LOG.error("IO Exception", ioe);
		}
	}

	public boolean hasNext() {
		return m_line != null;
	}

	public String next() {
		String line = m_line;
		
		// read next line
		try {
			m_line = m_bufferedReader.readLine();
		} catch (IOException ioe) {
			LOG.error("IO Exception", ioe);
		}
		return line;
	}

	public void remove() {
		next();
	}

}
