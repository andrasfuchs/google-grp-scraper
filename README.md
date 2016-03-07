google-grp-scraper is a crawler which takes google group URL, no-of-downloader threads, no-of-topics to be downloaded in input,

where no-of-threads depends on no.of topics exists in that group.

To run this crawler using eclipse Ide, we need to add arguments before we start it :

Run->Run Configurations->Arguments->program Arguments->Apply->Run.

example:-'https://groups.google.com/forum/#!forum/hisad' 5 10

The extra feature added to current version is downloading only limited topics by specifying the limit.

OUTPUT:- After running above you will get output as below:
	1) Topics will be downloaded at (Download/<YOUR_GROUP_NAME>/Topics)
        2) Recovery file will be created at (Download/<YOUR_GROUP_NAME>/Recovery)

