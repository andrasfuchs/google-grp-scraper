package com.pramati.scraper.google_grp_scraper;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.interactions.Actions;

import com.pramati.scraper.util.RecoveryUtil;

public class CollectLink {
	// private Set<String> collectedLinkSet = new HashSet<String>();
	private BlockingQueue<String> linksSharedQueueForDownload = new ArrayBlockingQueue<String>(
			100000);
	private URL urlOfGrp;
	private String groupName;
	private int noOfWorkers;
	private int noOfTopics;
	private String downloadDirectory = "Download";
	private String directorySeparator = "/";
	private RecoveryUtil recoveryUtil = new RecoveryUtil();
	private Set<String> recoveredLinks;

	public void init(URL url, int noOfWorker, int noOfTopics) throws Exception {
		this.urlOfGrp = url;
		this.noOfWorkers = noOfWorker;
		this.noOfTopics = noOfTopics;
		setGroupName(urlOfGrp);
	}

	public void scrap() throws InterruptedException {
		// download the driver from https://www.selenium.dev/documentation/webdriver/getting_started/install_drivers/
//		ChromeDriverService src = new ChromeDriverService.Builder()
//				.usingDriverExecutable(
//						new File("c:\\Work\\google-grp-scraper\\drivers\\chromedriver.exe"))
//				.usingAnyFreePort().build();
//		WebDriver groupBrowser = new ChromeDriver(src);

		FirefoxBinary firefoxBinary = new FirefoxBinary();
		GeckoDriverService src = new GeckoDriverService.Builder()
				.usingDriverExecutable(
						new File("c:\\Work\\google-grp-scraper\\drivers\\geckodriver.exe"))
				.usingFirefoxBinary(firefoxBinary)
				.usingAnyFreePort().build();
		WebDriver groupBrowser = new FirefoxDriver(src);

		this.startCrawl(groupBrowser);
		this.startDownloader(groupBrowser);

	}

	@SuppressWarnings("static-access")
	private void startCrawl(WebDriver groupBrowser) throws InterruptedException {
		groupBrowser.navigate().to(urlOfGrp);
		this.performFailureRecovery();
		Actions clickAction = new Actions(groupBrowser);
		WebElement scrollablePane = groupBrowser.findElement(By
		// .className("G3J0AAD-b-F"));
				.className("IVILX2C-b-D"));
		clickAction.moveToElement(scrollablePane).click().build().perform();
		Actions scrollAction = new Actions(groupBrowser);

		Thread.currentThread().sleep(5000);
		Set<String> links = new HashSet<String>();
		List<WebElement> list = null;
		for (int second = 0;; second++) {
			if (second >= 60) {
				break;
			}
			((JavascriptExecutor) groupBrowser).executeScript(
					"window.scrollBy(0,200)", "");
			list = groupBrowser.findElements(By.cssSelector("a"));

			Thread.sleep(1000);
		}
		int noOfLinks = 1;
		for (WebElement str : list) {
			String completeUrl = str.getAttribute("href");
			if (completeUrl != null && completeUrl.contains("topic")) {
				links.add(completeUrl);
				noOfLinks++;
				if (noOfLinks == noOfTopics)
					break;
			}
		}
		System.out.println("Total no.of links :" + noOfLinks);
		linksSharedQueueForDownload.addAll(links);
	}

	private void performFailureRecovery() {
		recoveredLinks = recoveryUtil.getDownloadedLinks(downloadDirectory
				+ directorySeparator + groupName);

	}

	private void setGroupName(URL urlOfGrp) throws Exception {
		String grpNameRegex = "https://groups.google.com/g/(.*?)";
		Pattern pattern = Pattern.compile(grpNameRegex);
		Matcher matcher = pattern.matcher(urlOfGrp.toString());
		if (matcher.matches()) {
			groupName = matcher.group(1);
		} else {
			throw new Exception("INVALID GROUP URL");
		}
	}

	private void startDownloader(WebDriver gBrowser) {
		DownloadWorker downloadWorker = null;
		for (int i = 0; i < noOfWorkers; i++) {
			downloadWorker = new DownloadWorker(linksSharedQueueForDownload,
					groupName, gBrowser);
			Thread dowloaderThread = new Thread(downloadWorker);
			dowloaderThread.start();
		}
	}
}