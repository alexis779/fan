package tech.vineyard.httpclient;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;

import tech.vineyard.httpclient.Job2;
import tech.vineyard.httpclient.JobQueue;


public abstract class AbstractTestHttpClient {
	private static final String METHOD = "HEAD";

	protected JobQueue jobList = new JobQueue();
	
	public abstract void doRequest(JobQueue queue);

	
	/**
	 * Test a single request.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void truveo() throws IOException, InterruptedException {
		addJob("http://www.truveo.com/");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}	

	/**
	 * Test Keep-Alive policy.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void arNetlog() throws IOException, InterruptedException {
		addJob("http://ar.netlog.com/go/explore/videos/videoid=158666");
		addJob("http://ar.netlog.com/go/explore/videos/videoid=332671");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}

	/**
	 * Special characters in a non default encoding in the response.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void terraTv() throws IOException, InterruptedException {
		addJob("http://terratv.terra.com.br/Especiais/Homem/Carro-Online/4540-223046/Peugeot-2008-busca-unir-conceitos.htm");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}

	/**
	 * EOF received instead of a valid response in the second request.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void readEOF() throws IOException, InterruptedException {
		addJob("http://player.video.news.com.au/theaustralian/?WWB2Xw2hVigi5nVg5SeexyQt75YMby44");
		addJob("http://player.video.news.com.au/news/?0jVea_1AyVm3C6as4TojvlG1_ocW_D33");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());		
		addJob("http://video.tvguide.com/Date+Night+2010/Date+Night/4866938?autoplay=true%20partnerid=OVG");
		addJob("http://video.tvguide.com/Brooks++Dunn/Put+a+Girl+in+It/5445966?autoplay=true%20partnerid=OVG");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
		addJob("http://zoome.jp/type-r/diary/4");
		addJob("http://zoome.jp/tentaclelost/diary/9");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}

	/**
	 * Limit the number of redirects if stuck in infinite loop.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void bollywo() throws IOException, InterruptedException {
		addJob("http://www.bollywo.com/video/165906/rana-hits-the-dance-floormp4.html");
		addJob("http://www.bollywo.com/videos/71928/djmafijanr2010.html");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}

	/**
	 * Server error.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void liveVideo() throws IOException, InterruptedException {
		addJob("http://www.livevideo.com/video/2134E01DFB10434C82C4735B9397B885/scrapbooking-en-estados-unidos.aspx");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
		addJob("http://www.vidilife.com/video_play_1666607_Perfect_BlackBerry_8900_Forsted_Housing_Faceplate_.htm");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}	
	
	/**
	 * Connection reset by peer exception.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void timsah() throws IOException, InterruptedException {
		addJob("http://www.timsah.com/Ashlynn-Brooke-nefes-kesen-cekim/ZrrTVyxhOk-");
		addJob("http://www.timsah.com/Misirlarin-korkulu-ruyasi/dTrrT7a2LdW");
		//addJob("http://www.timsah.com/15-saniyede-kirmizi-kart-gordu/ZTQddOMBsVz");
		doRequest(jobList);
		assertTrue(jobList.isEmpty());
	}		

	
	private void addJob(String url) {
		URI uri = URI.create(url);
		
		if (jobList.isEmpty()) {
			jobList.setHost(uri.getHost());
		}
		
		String path = JobQueue.getPath(uri);
		jobList.add(new Job2(METHOD, path));
	}

}
