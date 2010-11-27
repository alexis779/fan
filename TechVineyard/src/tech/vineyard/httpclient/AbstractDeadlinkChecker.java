package tech.vineyard.httpclient;

import java.util.Iterator;

public abstract class AbstractDeadlinkChecker {

	private Iterator<JobQueue> iterator;

	protected AbstractDeadlinkChecker(String input) {
		if (input != null) {
			setIterator(new UrlListIterator(input));
		}
		
	}
	protected Iterator<JobQueue> getIterator() {
		return iterator;
	}
	
	private void setIterator(Iterator<JobQueue> iterator) {
		this.iterator = iterator;
	}

	public abstract void checkUrls();
	protected abstract void doRequests(JobQueue queue);

}
