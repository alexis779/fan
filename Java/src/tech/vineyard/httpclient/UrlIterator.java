package tech.vineyard.httpclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlIterator implements Iterator<String> {
	private static final Log LOG = LogFactory.getLog(UrlIterator.class);
	
	private static final String FILE_PATH = "/home/alex/pougne/truveo/input";
	public static final int FILE_INPUT = 0;
	public static final int STDIN_INPUT = 0;

	private BufferedReader m_bufferedReader;
	
	private String m_line;
	
	public UrlIterator(int input) {
		if (input == FILE_INPUT) {

			File file = new File(FILE_PATH);
			try {
				FileReader fileReader = new FileReader(file);
				m_bufferedReader = new BufferedReader(fileReader);
				
			} catch (FileNotFoundException fnfe) {
				LOG.error("File Not Found", fnfe);
			}
		} else if (input == STDIN_INPUT) {
			InputStream inputStream = System.in;
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			m_bufferedReader = new BufferedReader(inputStreamReader);
		}
		
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
