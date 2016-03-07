package com.pramati.scraper.google_grp_scraper;

import java.net.URL;

public class ScraperStartup {
	public static void main(String[] args) throws Exception {

		CollectLink collectLink = new CollectLink();
		URL url = new URL(args[0]);
		int noOfWorker;
		int noOfTopics;
		try {
			noOfWorker = Integer.parseInt(args[1]);
			noOfTopics = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			noOfWorker = 10;
			noOfTopics = 10;
		}

		collectLink.init(url, noOfWorker, noOfTopics);
		collectLink.scrap();
	}
}
