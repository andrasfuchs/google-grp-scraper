package com.pramati.scraper.google_grp_scraper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.openqa.selenium.WebDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.pramati.scraper.util.FileUtil;
import com.pramati.scraper.util.RecoveryUtil;

public class DownloadWorker implements Runnable {

	private BlockingQueue<String> linksSharedQueueForDownload;
	private WebClient client;
	private WebDriver gBrowser;
	private String downloadDirectory = "Download";
	private String failureRecoveryDirectory = "Recovery";
	private String topicDirectory = "Topics";
	private String directorySeparator = "/";
	private FileUtil fileUtil = new FileUtil();
	private String groupName;
	private RecoveryUtil recoveryUtil = new RecoveryUtil();

	public DownloadWorker(
			BlockingQueue<String> topicLinksSharedQueueForDownload,
			String groupName, WebDriver gBrowser) {
		this.linksSharedQueueForDownload = topicLinksSharedQueueForDownload;
		this.groupName = groupName;
		this.gBrowser = gBrowser;
	}

	public void run() {
		createClient();
		while (true) {
			String linkForDownload = null;
			try {
				linkForDownload = linksSharedQueueForDownload.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (linkForDownload.equalsIgnoreCase("POISON")) {
				break;
			}
			if (linkForDownload != null) {
				download(linkForDownload, gBrowser);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void download(String linkForDownload, WebDriver gBrowser) {
		WebRequest webReq;
		String titleOfTopic = "";
		String contentOfTopic = "";
		List<HtmlSpan> spanElement = null;
		List<HtmlDivision> divElement = null;
		try {
			java.util.logging.Logger.getLogger("com.gargoylesoftware")
					.setLevel(java.util.logging.Level.OFF);
			webReq = new WebRequest(new URL(linkForDownload));
			gBrowser.navigate().to(linkForDownload);
			gBrowser.navigate().forward();
			HtmlPage page = client.getPage(webReq);
			spanElement = (List<HtmlSpan>) page
					.getByXPath("//span[@id=\"t-t\"]");
			divElement = (List<HtmlDivision>) page
					.getByXPath("//div[@class=\"IVILX2C-b-D\"]");

			if (!spanElement.isEmpty()) {

				HtmlSpan span = (HtmlSpan) spanElement.get(0);
				titleOfTopic = span.asText();
				System.out.println(titleOfTopic + " is created");
				contentOfTopic = "SUBJECT IS :" + titleOfTopic + "\n\n";
			}

			for (HtmlDivision div : divElement) {
				if (div != null && !div.asText().equals("")
						&& div.asText() != null) {
					contentOfTopic = contentOfTopic + div.asText() + "\n";
					contentOfTopic = contentOfTopic
							+ "----------------------------------------------------------------------------\n";
				}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (FailingHttpStatusCodeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (!titleOfTopic.equals("")) {
				if (titleOfTopic.length() > 25) {
					titleOfTopic = titleOfTopic.substring(0, 20);
				}
				fileUtil.createFileAndWriteTxt(titleOfTopic, downloadDirectory
						+ directorySeparator + groupName + directorySeparator
						+ topicDirectory, contentOfTopic);
				gBrowser.navigate().back();
				recoveryUtil.maintainRecoveryList(downloadDirectory
						+ directorySeparator + groupName, linkForDownload);
			} else {
				System.out.println("Nothing to download");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.closeAllWindows();
	}

	private void createClient() {
		client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setJavaScriptEnabled(true);
		client.getOptions().setRedirectEnabled(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setCssEnabled(false);
		client.getOptions().setUseInsecureSSL(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.setAjaxController(new NicelyResynchronizingAjaxController());
		client.setJavaScriptTimeout(36000);
	}

}
