# CC-MAIN-2021-31-PDF-UNTRUNCATED #

**THIS IS A WORK IN PROGRESS!!!  Still much to be done!!!** 

# Overview
This corpus contains nearly 8 million PDFs gathered from the across the web in July/August of 2021. 
The files were initially gathered by [Common Crawl](https://commoncrawl.org/) as part of 
their July/August 2021 crawl, identified as `CC-MAIN-2021-31`.

For this specific crawl, the Common Crawl project [writes](https://commoncrawl.org/2021/08/july-august-2021-crawl-archive-available/):

>The data was crawled July 23 – August 6 and contains 3.15 billion web pages or 360 TiB of uncompressed content. It includes page captures of 1 billion new URLs, not visited in any of our prior crawls.

This current corpus offers four benefits over the Common Crawl dataset as stored in 
[Amazon Public Datasets](https://registry.opendata.aws/commoncrawl/).

1. Common Crawl truncates files at 1MB. For this corpus, we refetched the complete/untruncated files from the original URLs.
2. This corpus offers a tractable subset of the files, focusing on a single format: PDF.
3. We have supplemented the metadata to include geo-ip-location (where possible) and other data extracted from the files (e.g. by [pdfinfo](http://poppler.freedesktop.org)).
4. The files are extracted from their containing Web ARChive (WARC) files and packaged in the zip format.

We have not rigorously assessed how representative this corpus is of PDF files on the web.  
Preliminary analysis suggests that Common Crawl data should be viewed as a convenience sample.  
The crawls may not be representative nor complete, but they do offer a large set of data from the web.

We could not have done this work without the initial Common Crawl data.  Please note Common Crawl's [license and terms of use](https://commoncrawl.org/terms-of-use/full/).

# Packaging
Each of the 7,999 zip files contains 1,000 files -- except the last, obviously.  
We have removed duplicates -- there are 8.3 million URLs for which we have a file, and there are 7.9 unique files.
Each zip file is slightly more than 1 GB, and uncompressed, the files take up nearly 8 TB.

# Supplementary Metadata
We include tables to link the files back to the original Common Crawl records in the `CC-MAIN-2021-31` dataset and to
offer a richer view of the data via extracted metadata.

## Crawl Data
The table `cc-provenance-table.csv.gz` contains all provenance information.

* `file_name` -- name of the file as our project named it inside the zip
* `url` -- target url extracted from Common Crawl's index files. Max length in this set is 6,771 characters.
* `cc_digest` -- digest calculated by Common Crawl and extracted from the index files
* `cc_http_mime` -- mime as extracted from Common Crawl's index files -- this derives from the http header
* `cc_detected_mime` -- the detected mime as extracted from Common Crawl's index files.
* `cc_warc_file_name` -- the Common Crawl warc file where the file's individual warc file is stored
* `cc_warc_start` -- the offset within the `cc_warc_file` where the individual warc file is stored
* `cc_warc_end` -- this is the end of the individual warc file within the larger `cc_warc_file`
* `host_id` -- this is a project-internal foreign key for the url's host
* `cc_truncated` -- this is Common Crawl's code for why the file was truncated if the file was truncated.  This information was extracted from Common Crawl's indices. Values include:
  * `''` (6,383,873) -- (empty string) -- Common Crawls records this as not truncated
  * `length` (2,020,913) -- the file was truncated because of length
  * `disconnect` (5,861) -- there was a network disconnection during Common Crawl's fetch
  * `time` (56) -- there was a timeout during Common Crawl's fetch
* `fetched_status` -- records our project's status for obtaining the file. Values include:
  * `ADDED_TO_REPOSITORY` (6,377,619) -- extracted from Common Crawl
  * `REFETCHED_SUCCESS` (1,922,505) -- our project refetched content from the original target URL
  * `REFETCH_UNHAPPY_HOST` (53,038) -- we tried to refetch a URL, but the failures from that host exceeded our threshold.  (We didn't want to bother a host that had refused our refetches)
  * `REFETCHED_IO_EXCEPTION_READING_ENTITY` (45,561) -- during our refetch, there was an IOException while trying to read the contents
  * `EMPTY_PAYLOAD` (5,719) -- There was an empty payload in the Common Crawl warc file.
  * `REFETCHED_TIMEOUT` (5,157) -- timeout during our attempted refetch.
  * `REFETCHED_IO_EXCEPTION` (569) -- general IOException while trying to refetch.
  * `null` (506) -- ??
  * `FETCHED_EXCEPTION_EMITTING` (29) -- there was an exception trying to write to S3
* `fetched_digest` -- the sha256 that we calculated on the bytes that we have for the file
* `fetched_length` -- the length in bytes of the file that we extracted from Common Crawl or refetched

**Top 10 `cc_http_mime` values:**

|            mime             |   count    |
|:---------------------------:|:----------:|
|       application/pdf       | 	8,156,384 |
|  application/octet-stream   |  	145,722  |
|          text/html          |  	22,901   |
|    application/download     |  	14,011   |
| application/force-download  |  	12,740   |
|             unk             |  	11,460   |
|        content-type:        |   	7,153   |
|             pdf             |   	7,114   |
|   application/x-download    |   	6,078   |
|     binary/octet-stream     |   	2,166   |

**Top 10 `cc_detected_mime` values:**

|            mime             |   count    |
|:---------------------------:|:----------:|
|application/pdf| 	8,389,207 |
|text/html|  	16,515   |
|text/plain|   	3,049   |
|application/xhtml+xml|    	814    |
|application/pkcs7-signature|    	210    |
|application/x-tika-ooxml|    	142    |
|image/jpeg|    	117    |
|application/xml|    	96     |
|application/octet-stream|    	78     |
|application/gzip|    	76     |

## Hosts

* `id` -- primary key to be used in joins with the `host_id` column in `cc-provenance-table.csv.gz`
* `host` -- host
* `tld` -- top level domain
* `ip_address` -- as retrieved from Common Crawl or captured during refetch
* `country`, `latitude` and `longitude` -- as geolocated by MaxMind's [geolite2](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data)

Of the 8.3 million URLs for which we have a file, the counts for the top 10 countries:

| Country <br/>Code | Count      |
|----------|------------|
|US| 	3,259,209 |
|DE| 	896,990   |
|FR| 	462,215   |
|JP| 	364,303   |
|GB| 	268,950   |
|IT| 	228,065   |
|NL| 	206,389   |
|RU| 	176,947   |
|CA| 	175,853   |
|ES| 	173,619   |


## PDFInfo
TBD

## Apache Tika
TBD

# Related Work
* Allison, Timothy. "Making more sense of PDF structures in the wild at scale." 
PDF Days Europe 2022, September 12-13, 2022.
* Allison, Timothy. "Building a File Observatory: Making sense of PDFs in the Wild". 
Open Preservation Foundation Webinar, January 19, 2022.
* Allison, Timothy. "Making sense of PDF structures in the wild at scale". PDF Days Online 2021, September 29, 2021.


# Credits
This dataset was gathered by a team at NASA's Jet Propulsion Laboratory (JPL), 
California Institute of Technology while supporting the Defense Advance Research Project Agency
(DARPA)'s SafeDocs Program. The JPL team included Chris Mattmann (PI), 
Wayne Burke, Dustin Graf, Tim Allison, Ryan Stonebraker, Mike Milano, 
Philip Southam and Anastasia Menshikova.

The JPL team collaborated with Peter Wyatt, the Chief Technology Officer 
of the PDF Association, in the design and documentation of the corpus.

The JPL team would like to thank Simson Garfinkel and Digital Corpora for taking ownership of this dataset and publishing it.

Reference herein to any specific commercial product, process, or service 
by trade name, trademark, manufacturer, or otherwise, does not constitute 
or imply its endorsement by the United States Government or the 
Jet Propulsion Laboratory, California Institute of Technology.

The research was carried out at the NASA (National Aeronautics and Space Administration) Jet Propulsion Laboratory, 
California Institute of Technology under a contract with the Defense 
Advanced Research Projects Agency (DARPA) SafeDocs program. 
© 2023 California Institute of Technology. Government sponsorship acknowledged.

# Constructing the Corpus

## Types of Common Crawl Data used
This project used two types of data from Common Crawl

### Common Crawl indices
The [indices](https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/cc-index.paths.gz) are
gzipped text files, where each line is a JSON object that contains metadata about each URL.
Information includes, among other things: URL, mime, detected mime, the CC WARC (Web ARChive) file where the file's warc file exists along with the warc file's offset and length

### Common Crawl WARCs
Common Crawl concatenates gzipped WARCs into very large WARC files.  To fetch
an individual file's original WARC, users need to know the source WARC file, the offset for the individual file and the
length.  See below for a [worked example](#how-to-extract-an-individual-warc-from-common-crawl).

## File Types
Our team processed the [indices](https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/cc-index.paths.gz) for this crawl
and extracted all files where an http `Content-Type` header contained the letters `pdf` or where Common Crawl's automatic file
detection detected a PDF. We acknowledge that this choice will result in files that are not actually PDFs.

## Common Crawl or Refetched
In the indices for a crawl, Common Crawl has a flag for whether or not the file was truncated.  We extracted
roughly 6 million files directly from Common Crawl.  We then refetched from the original URLs nearly 2 million
files that Common Crawl had identified as truncated.

## Filenaming
We sorted the files by `sha-256` and then numbered them from 0 (`0000000.pdf`) to roughly
8 million (`7932877.pdf`).  We added a `.pdf` file extension to every file.

# Extras
## 1. How to extract an individual WARC from Common Crawl
First, users need the `cc_warc_file`, the `cc_warc_start` and the `cc_warc_end` from the provenance table.
We'll use `curl` and `gunzip`. Let's say we want to pull `0000000.pdf` which comes from `crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz`
starting at offset `3,724,499` and ends at offset `3,742,341` (inclusive).
1. Prepend `https://data.commoncrawl.org/` to the `cc_warc_file` to get the URL.
2. The http range will be: `3724499-3742341`
3. Fetch the gzipped WARC file: `curl -r 3724499-3742341 https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz -o 0000000.warc.gz`
4. `gunzip 0000000.warc.gz`

