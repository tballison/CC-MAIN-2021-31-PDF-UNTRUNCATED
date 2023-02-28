# CC-MAIN-2021-31-PDF-UNTRUNCATED #

# Overview
This corpus contains nearly 8 million PDFs gathered from the across the web in July/August of 2021. 
The PDF files were initially identified by [Common Crawl](https://commoncrawl.org/) as part of 
their July/August 2021 crawl (identified as `CC-MAIN-2021-31`) and subsequently updated and collated as part of the [DARPA SafeDocs program](https://www.darpa.mil/program/safe-documents).

This current corpus offers five benefits over Common Crawl datasets as stored in 
[Amazon Public Datasets](https://registry.opendata.aws/commoncrawl/).

1. Common Crawl truncates files at 1MB. For this corpus, we refetched the complete/untruncated PDF files from the original URLs without any file size limitation.
2. This corpus offers a tractable subset of the files, focusing on a single format: PDF.
3. We have supplemented the metadata to include geo-ip-location (where possible) and other metadata extracted from the PDF files (e.g. by [pdfinfo](http://poppler.freedesktop.org)).
4. All PDF files (both Common Crawl <1MB PDFs and the larger truncated PDFs that were refetched) are conveniently packaged in the zip format. This is the same as [GovDocs1](https://digitalcorpora.org/corpora/files/).
5. At the time of its creation, this is the largest single corpus of real-world (extant) PDFs that is publicly available. Many other [smaller, targeted or synthetic PDF-centric corpora](https://github.com/pdf-association/pdf-corpora) exist. 

It is not possible to rigorously assess how representative this corpus is of PDF files across the entire the web or of PDF files in general. 
It is [well known](https://www.youtube.com/watch?v=5Af3IC5WxPo) that a significant number of PDF files lie within private intranets or repositories, behind log ins, and 
are not made publicly accessible due to PII or other confidential content.
This means that all corpora created by web crawling may not adequately represent every PDF feature or capability.
Even as web crawls go, preliminary analysis suggests that Common Crawl data should be viewed as a convenience sample.  
In short, the crawls (and this corpus) may not be representative nor complete, but they do offer a large set of data from the publicly accessible web.

For the specific `CC-MAIN-2021-31` crawl, the Common Crawl project [writes](https://commoncrawl.org/2021/08/july-august-2021-crawl-archive-available/):

>The data was crawled July 23 – August 6 and contains 3.15 billion web pages or 360 TiB of uncompressed content. It includes page captures of 1 billion new URLs, not visited in any of our prior crawls.

We could not have done this work without the initial Common Crawl data.  Please note Common Crawl's [license and terms of use](https://commoncrawl.org/terms-of-use/full/).

## Application

PDF is a ubiquitous format and used across many industrial and research domains.
Many existing corpora focusing on extant data (such as GovDocs1) are now quite old and no longer reflect current changes and trends in both PDF itself (as a file format) or 
in PDF-creating and authoring applications. With advances in machine learning technology the need for larger data sets is also in high demand. 
This corpus is thus useful for:

*	PDF technology and software testing, assessment, and evaluation
*	Information privacy research
*	Document understanding, text extraction, table identification, OCR/ICR, formula identification, document recognition and analysis, and related document engineering domains
*	Malware and cyber-security research
*	ML/AI applications and research (document classification, document content, text extraction, etc)
*	Preservation and archival research
*	Usability and accessibility research
*	Software engineering research (parsing, formal methods, etc.)


# Packaging
Each of the 7,932 zip files contains 1,000 PDF files -- except the last, obviously.
We have removed duplicates (based on SHA-256 hash of the PDF file) -- there are 8.3 million URLs for which we have a PDF file, and there are 7.9 million unique PDF files.
All files are named using a 7-digit number with a `.pdf` extension (e.g. `0000000.pdf`, `0000001.pdf`, etc.) -- the file number is arbitrary in this corpus.  
Each zip file is slightly more than 1 GB, and uncompressed, the entire corpus takes up nearly 8 TB.

# Supplementary Metadata
We include tables to link each PDF file back to the original Common Crawl record in the `CC-MAIN-2021-31` dataset and to
offer a richer view of the data via extracted metadata.

## Crawl Data
The table `cc-provenance-table.csv.gz` contains all provenance information:

* `file_name` -- name of the PDF file as our project named it inside the zip
* `url` -- target url extracted from Common Crawl's index files. Max length in this set is 6,771 characters.
* `cc_digest` -- digest calculated by Common Crawl and extracted from the index files
* `cc_http_mime` -- MIME as extracted from Common Crawl's index files -- this derives from the http header
* `cc_detected_mime` -- the detected MIME as extracted from Common Crawl's index files.
* `cc_warc_file_name` -- the Common Crawl warc file where the file's individual warc file is stored
* `cc_warc_start` -- the offset within the `cc_warc_file` where the individual warc file is stored
* `cc_warc_end` -- this is the end of the individual warc file within the larger `cc_warc_file`
* `host_id` -- this is a project-internal foreign key for the url's host
* `cc_truncated` -- this is Common Crawl's code for why the file was truncated if the file was truncated.  This information was extracted from Common Crawl's indices. Values include:
  * `''` (6,383,873) -- (empty string) -- Common Crawls records this as not truncated
  * `length` (2,020,913) -- the file was truncated because of length
  * `disconnect` (5,861) -- there was a network disconnection during Common Crawl's original fetch
  * `time` (56) -- there was a timeout during Common Crawl's original fetch
* `fetched_status` -- records our project's status for obtaining the file. Values include:
  * `ADDED_TO_REPOSITORY` (6,377,619) -- extracted directly from the Common Crawl data
  * `REFETCHED_SUCCESS` (1,922,505) -- our project refetched content from the original target URL
  * `REFETCH_UNHAPPY_HOST` (53,038) -- we tried to refetch a URL, but the failures from that host exceeded our threshold.  (We didn't want to bother a host that had refused our refetches)
  * `REFETCHED_IO_EXCEPTION_READING_ENTITY` (45,561) -- during our refetch, there was an IOException while trying to read the contents
  * `EMPTY_PAYLOAD` (5,719) -- There was an empty payload in the Common Crawl warc file.
  * `REFETCHED_TIMEOUT` (5,157) -- timeout during our attempted refetch.
  * `REFETCHED_IO_EXCEPTION` (569) -- general IOException while we were trying to refetch.
  * `null` (506) -- ??
  * `FETCHED_EXCEPTION_EMITTING` (29) -- there was an exception when we tried to write a refetched PDF to S3
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
of the PDF Association and PI on the SafeDocs program, in the design and documentation of the corpus.

The JPL team and PDF Association would like to thank Simson Garfinkel and Digital Corpora for taking ownership of this dataset and publishing it.
Our thanks is extended to the [Amazaon Open Data Sponsorship Program](https://aws.amazon.com/opendata/open-data-sponsorship-program/) for enabling this large corpus to be free and publicly available as part of Digitial Corpora initiative. 

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

# Errata 
We are aware that the following files are missing from the corpus. There were sporadic S3 write exceptions during the fetching 
and refetching.

| File name | sha256 |
|-----------|------------|
|177150.pdf | 05ba53532b7bfc15901bc1bd3371421be758bb08cc2070528a49be4c0b77c6c7|
|594742.pdf | 1334239e569fad2a30d11f6f90d5f75645ded13870cd9b6118b4930d297a23e9|
|706328.pdf | 16cd8100c6a8710d5c404ee11bfc285efee5693c6ceaa42fce2b466051b2c40a|
|1260258.pdf | 28a410c2b3a767d618b44980be1a68335fd436e70165211d03421fcd198e4de7|
|1544119.pdf | 31ca2adee5ea5ac522bf02db2a9a70bdc0e220ccc242dce9b22254e9a3f7c8fa|
|1591732.pdf | 3354af25e39f6ccfabb7833f14958512537dd019e9d4dddeb912fb5b5799158b|
|1640603.pdf | 34eb229ecac8ddecf1632a06762a1998477c07d56249db84edfd157245b6022c|
|1890087.pdf | 3cf45e3dc0fdf429ac894d77ea85460db744dd93c8704102b914974e7b963630|
|1920911.pdf | 3df2586c61b34ad857b4f13eebf1bf2fd8f1a9af71c582c26640278166ba1f7f|
|1992331.pdf | 403f27afa6c84a5fbc512361d9929ef49ae00d399f1b1f876c26a900d056a846|
|2519839.pdf | 51467cf4516df4919c3b195ad67c10a668d339a705c4644ce60fd69f39f6730e|
|2712444.pdf | 577c5f029ff827362b5a71d14f1e4a015bea3eb53960e250ffa1dde2f7ae0050|
|2765539.pdf | 59343aae861d86d9d360b4ccf0183f33a77e49b67696ee1f900821e7dad1f04e|
|3179469.pdf | 669693d161926d705d63ac8fed895857549b4b7e5d82c2ead56a07c367616fb5|
|4170238.pdf | 86931ce5974bff673eb48aa4159b6c215efea4ce636f8e486e9fd54c14e33e9f|
|4414331.pdf | 8e77a888f6ac85d24ac63e55810c2b2646ba18f540037ac748b50007f7c1c8c8|
|4512373.pdf | 91a3d6390adceb54e0ff993f8cfd58250f1bbabfd5ef061a7659ed019897d179|
|4977579.pdf | a09de5d289dd95d4b4b71d13e196e05db5ab5d228c65afcd74e5900a40a11b09|
|5198714.pdf | a7c81076098d7e179d13ab60a8da6c8897f71315060b73b959667e0f8ff385b9|
|5236677.pdf | a9031fc3fbaecf9abcb906e630fdeb90e71e1f9e3d78959ff5101e0fdaa7de65|
|5447694.pdf | afd19ad6ca780aa7c90756e97aa20fb11bf4781cfc0ee00e5bf23f66f940f51a|
|6318895.pdf | cbeb29136aaa7b934c2b8616dafa4b8b9213235ed1be9c818c3858c990914275|
|6817632.pdf | dc0840305e174825fa1471dc2ab463bdffece4ec78b496b5e6a65245f4df4cc1|
|6940914.pdf | e004f3c7cf38f24ed278b9d3c30c5269f625ed66c623bd6f46ecb3aed9dac3d4|
|7241425.pdf | e9b4ec5975d197ffc9d199a188d68cc75cb323ecca545df0668c567bc04a769a|
|7279847.pdf | eaf2d8ba2606262e861d5e8fe0b26b9c456d1fd3290c17d7c115dc14e02a73ca|
|7407159.pdf | ef107d1cd9224d3582a1364b012f1585a6192ef1fa3267ab18c078777083091f|
|7635694.pdf | f670fc79401a83b67f2695666803fb8e2ef2fe05a20c2880ea9f0b7465431523|
|7889525.pdf | fe9b31aa4fcf115ae893ffb2937558a11ee7c80ed9dd1908c3a9451ae8d3c140|

# Extras
## 1. How to extract an individual WARC from Common Crawl
First, users need the `cc_warc_file`, the `cc_warc_start` and the `cc_warc_end` from the provenance table.
We'll use `curl` and `gunzip`. Let's say we want to pull `0000000.pdf` which comes from `crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz`
starting at offset `3,724,499` and ends at offset `3,742,341` (inclusive).
1. Prepend `https://data.commoncrawl.org/` to the `cc_warc_file` to get the URL.
2. The http range will be: `3724499-3742341`
3. Fetch the gzipped WARC file: `curl -r 3724499-3742341 https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz -o 0000000.warc.gz`
4. `gunzip 0000000.warc.gz`

