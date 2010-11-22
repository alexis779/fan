package tech.vineyard.httpclient;


public class Job2 {
	private String path;
	private String method;
	private int redirects;

	private String content;
	

	public Job2(String method, String path) {
		this.method = method;
		this.path = path;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    public int getRedirects() {
		return redirects;
	}

	public void setRedirects(int redirects) {
		this.redirects = redirects;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}



}
