# CC-MAIN-2021-31-PDF-UNTRUNCATED 

# Overview
This corpus contains nearly 8 million PDFs gathered from across the web in July/August of 2021. 
The PDF files were initially identified by [Common Crawl](https://commoncrawl.org/) as part of 
their July/August 2021 crawl (identified as `CC-MAIN-2021-31`) and subsequently updated and collated as part of the [DARPA SafeDocs program](https://www.darpa.mil/program/safe-documents).

This current corpus offers five benefits over Common Crawl datasets as stored in 
[Amazon Public Datasets](https://registry.opendata.aws/commoncrawl/):

1. Common Crawl truncates files at 1MB. For this corpus, we refetched the complete/untruncated PDF files from the original URLs without any file size limitation.
2. This corpus offers a tractable subset of the files, focusing on a single format: PDF.
3. We have supplemented the metadata to include geo-ip-location (where possible) and other metadata extracted from the PDF files (e.g. by [pdfinfo](http://poppler.freedesktop.org)).
4. All PDF files (both Common Crawl <1MB PDFs and the larger truncated PDFs that were refetched) are conveniently packaged in the zip format. This is the same as [GovDocs1](https://digitalcorpora.org/corpora/files/).
5. At the time of its creation, this is the largest single corpus of real-world (extant) PDFs that is publicly available. Many other [smaller, targeted or synthetic PDF-centric corpora](https://github.com/pdf-association/pdf-corpora) exist. 

It is not possible to rigorously assess how representative this corpus is of all PDF files on the entire web or of PDF files in general. 
It is [well known](https://www.youtube.com/watch?v=5Af3IC5WxPo) that a significant number of PDF files lie within private intranets or repositories, behind logins, and 
are not made publicly accessible due to PII or other confidential content.
This means that all corpora created by web crawling may not adequately represent every PDF feature or capability.
Even as web crawls go, preliminary analysis suggests that Common Crawl data can be viewed as a convenience sample.  
In short, the crawls (and this corpus) may not be fully representative nor complete, but they do offer a reliable large set of data from the publicly accessible web.

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
All PDF files are named using a sequential 7-digit number with a `.pdf` extension (e.g. `0000000.pdf`, `0000001.pdf` through `7932877.pdf`) - the file number is arbitrary in this corpus as it is based on the SHA-256 of the PDF.  Duplicate PDF files (based on the SHA-256 hash of the file) have been removed - there are 8.3 million URLs for which we have a PDF file, and there are 7.9 million unique PDF files. 

PDF files are then packaged into ZIP files based on their sequentially numbered filename, with each ZIP file containing up to 1,000 PDF files (less if duplicates were detected and removed). The resulting ZIP files range in size from just under 1.0 GB to about 2.8 GB. With a few exceptions, all of the 7,933 ZIP files in the `zipfiles/` subdirectory tree contains 1,000 PDF files (see the [Errata](#errata) section below).

Each ZIP is named using a sequential 4-digit number representing the high 4 digits of the 7-digit PDF files in the ZIP - so `0000.zip` contains all PDFs numbered from `0000000.pdf` to `0000999.pdf`, `0001.zip` contains PDFs numbered from `0001000.pdf` to `0001999.pdf`, etc. ZIP files are clustered into groups of 1,000 and stored in subdirectories below `zipfiles/` based on the 4-digit ZIP filename, where each subdirectory is limited to 1,000 ZIP files: `zipfiles/0000-0999/`, `zipfiles/1000-1999/`, etc.  

The entire corpus when uncompressed takes up nearly 8 TB.

# Supplementary Metadata
We include tables to link each PDF file back to the original Common Crawl record in the `CC-MAIN-2021-31` dataset and to
offer a richer view of the data via extracted metadata. These are placed in the `metadata/` subdirectory.

For each table, we include the full table as a gzipped, UTF-8 encoded, CSV (e.g. `cc-provenance-20230303.csv.gz`).

We also include an uncompressed copy of each metadata table with the data relevant to `0000.zip` so that users may easily familiarize 
themselves with a smaller portion of the data (e.g. `cc-provenance-20230324-1k.csv`). Note that there are 1,045 data rows in these `*-1k.csv` tables because these tables are URL-based -- the same PDF may have come from multiple URLs. For example, `0000374.pdf` was retrieved from five URLs, so it appears five times in these tables.

Further note that due to Unicode-encoded metadata, the `*-1k.csv` tables have a UTF-8 [Byte Order Marker (BOM)](https://en.wikipedia.org/wiki/Byte_order_mark) prepended so that they may easily be opened by spreadsheet applications (such as Microsoft Excel) by double-clicking, and not result in [mojibake](https://en.wikipedia.org/wiki/Mojibake). This is because such applications will not prompt for an encoding when opening CSV files directly - the prompts for delimiters and encoding only occur if manually importing the data into these spreadsheet applications.

The very large gzipped metadata CSV files for the entire corpus do **NOT** have UTF-8 BOMs added as these are not directly usable by office applications.

## Crawl Data
The table `cc-provenance-20230303.csv.gz` contains all provenance information from the crawl (8,410,704 rows, including the header). 

* `url_id` -- primary key for each URL fetched or refetched
* `file_name` -- name of the PDF file as our project named it inside the zip. This value is not unique in this table because a given PDF (as identified by its sha256) may have been fetched from multiple URLs.
* `url` -- target url extracted from Common Crawl's index files. Max length in this set is 6,771 characters.
* `cc_digest` -- digest calculated by Common Crawl and extracted from the index files
* `cc_http_mime` -- MIME as extracted from Common Crawl's index files -- this is derived from the http header
* `cc_detected_mime` -- the detected MIME, as extracted from Common Crawl's index files.
* `cc_warc_file_name` -- the Common Crawl warc file where the file's individual warc file is stored
* `cc_warc_start` -- the offset within the `cc_warc_file` where the individual warc file is stored
* `cc_warc_end` -- this is the end of the individual warc file within the larger `cc_warc_file`
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
  * `FETCHED_EXCEPTION_EMITTING` (29) -- there was an exception trying to write to S3
* `fetched_digest` -- the sha256 that we calculated on the bytes that we have for the file, whether fetched from CC or refetched
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
The `cc-hosts-20230303.csv.gz` contains information about the hosts and, where possible,
the geographic location of the host for each PDF (8,410,704 rows, including the header).
The columns include:

* `url_id` -- primary key for each URL fetched or refetched. This key can be joined with the `url_id` in the `cc-provenance-20230303.csv.gz` table.
* `file_name` -- name of the PDF file as our project named it inside the zip. This value is not unique in this table because a given PDF (as identified by its sha256) may have been fetched from multiple URLs.
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
The `pdfinfo-20230315.csv.gz` contains output from `pdfinfo` ([poppler](https://poppler.freedesktop.org/) version=23.03.0, data version=0.4.12).
We ran this in a Docker container based on `debian:bullseye-20230227-slim` with the `-isodates` flag and a timeout of 2 minutes.

* `url_id` -- primary key for each URL fetched or refetched. This key can be joined with the `url_id` in the `cc-provenance-20230303.csv.gz` table.
* `file_name` -- name of the PDF file as our project named it inside the zip. This value is not unique in this table because a given PDF (as identified by its sha256) may have been fetched from multiple URLs.
* `parse_time_millis` -- milliseconds to process the file
* `exit_value` -- exit value for the `pdfinfo` process
* `timeout` -- boolean for whether or not the process timed out (`exit_value`= -1 in the 2 records where this happens)
* `stderr` -- stderr stream from `pdfinfo` (limited to first 1,024 characters)
* `pdf_version` -- PDF version from the header comment line at the start of the PDF file
* `creator` -- PDF creator tool from Document Information dictionary (limited to first 1,024 characters)
* `producer` -- PDF producer from Document Information dictionary (limited to first 1,024 characters)
* `created` -- date created from Document Information dictionary in ISO-8601 format (format: 2021-06-11T17:42:51+08 or 2021-07-31T19:31:14Z)
* `modified` -- date modified from Document Information dictionary in ISO-8601 format (format: 2021-06-11T17:42:51+08 or 2021-07-31T19:31:14Z)
* `custom_metadata` -- whether or not there is custom metadata (non-standard keys in the Document Information dictionary)
* `metadata_stream` -- whether or not there is an XMP Metadata stream (Document Catalog Metadata key)
* `tagged` -- whether the PDF is a Tagged PDF (Mark Information dictionary Marked key)
* `user_properties` -- contains user properties (Mark Information dictionary UserProperties key)
* `form` -- PDF is a form: XFA, AcroForm, 'null' or '' (empty)
* `javascript` -- PDF contains JavaScript (ECMAscript)
* `pages` -- number of pages according to the PDF page tree
* `page_size` -- string representing page size of the first page (in pts, 1/72 inch)
* `page_rotation` -- the page rotation of the first page (raw, as specified by the Rotate key)
* `optimized` -- is the PDF file is Linearized (a.k.a. "Fast web view" enabled)

| Exit Value | Count  | Notes                                                                 |
|----------|------------|-----------------------------------------------------------------------|
|0| 	7,893,956 | Completed normally                                                    |
|1| 	37,692    | May not be a PDF file (21,837), Encrypted file (4,295), other problem |
|99| 	1,185     | Wrong page range given (1,095) typically page tree has 0 pages?!      |
|-1	| 2          | timeout                                                               |
|1| null       | 0 byte file                                                           |



# Related Work
* Allison, Timothy. "_Making more sense of PDF structures in the wild at scale._" PDF Days Europe 2022, September 12-13, 2022. [Video and slide deck](https://www.pdfa.org/presentation/making-more-sense-of-pdf-structures-in-the-wild-at-scale/).
* Allison, Timothy. "_Building a File Observatory: Making sense of PDFs in the Wild_". Open Preservation Foundation Webinar, January 19, 2022. [Slide deck](https://www.slideshare.net/TimAllison6/building-a-file-observatory-making-sense-of-pdfs-in-the-wild)
* Allison, Timothy. "_Making sense of PDF structures in the wild at scale_". PDF Days Online 2021, September 29, 2021. [Video and slide deck](https://www.pdfa.org/presentation/making-sense-of-pdf-structures-in-the-wild-at-scale/).
* Allison, Timothy; Burke, Wayne; Mattmann, Chris; Menshikova, Anastasia; Southam, Philip; Stonebraker, Ryan; and Timmaraju, Virisha, "_Building a Wide Reach Corpus for Secure Parser Development_", IEEE Security & Privacy LangSec Worshop, May 21, 2020.  [Slides and paper](http://spw20.langsec.org/papers.html#corpus).


# Credits
This dataset was gathered by a team at NASA's Jet Propulsion Laboratory (JPL), 
California Institute of Technology while supporting the Defense Advance Research Project Agency
(DARPA)'s SafeDocs Program. The JPL team included Chris Mattmann (PI), 
Wayne Burke, Dustin Graf, Tim Allison, Ryan Stonebraker, Mike Milano, 
Philip Southam and Anastasia Menshikova.

The JPL team collaborated with Peter Wyatt, the Chief Technology Officer 
of the PDF Association and PI on the SafeDocs program, in the design and documentation of this corpus.

The JPL team and PDF Association would like to thank Simson Garfinkel and Digital Corpora for taking ownership of this dataset and publishing it.
Our thanks are extended to the [Amazon Open Data Sponsorship Program](https://aws.amazon.com/opendata/open-data-sponsorship-program/) for enabling this large corpus to be free and publicly available as part of Digital Corpora initiative. 

Reference herein to any specific commercial product, process, or service 
by trade name, trademark, manufacturer, or otherwise, does not constitute 
or imply its endorsement by the United States Government or the 
Jet Propulsion Laboratory, California Institute of Technology.

The research was carried out at the NASA (National Aeronautics and Space Administration) Jet Propulsion Laboratory, 
California Institute of Technology under a contract with the Defense 
Advanced Research Projects Agency (DARPA) SafeDocs program. Government sponsorship acknowledged.

# Constructing the Corpus

## Types of Common Crawl Data used
This project used two types of data from Common Crawl.  For more information on the types of data available for each crawl, see [CommonCrawl's Getting Started](https://commoncrawl.org/the-data/get-started/).

### Common Crawl indices
The [indices](https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/cc-index.paths.gz) are
gzipped text files, where each line contains a key to enable easy sorting of URLs by host and domain, a timestamp and 
a JSON object that contains metadata about each URL.
Information in the JSON object includes, among other things: URL, mime, detected mime, and the location of the 
individual WARC file as specified by the path to the compound WARC and the offset and length of the individual WARC
file within the compound WARC.  For more details, see
[commoncrawl-fetcher-lite](https://github.com/tballison/commoncrawl-fetcher-lite#background) page.

### Common Crawl WARCs
Common Crawl concatenates gzipped WARCs into very large WARC files (~1GB each).  To fetch
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
We are aware that the following PDFs are missing from the corpus. Their omission was caused by rare and sporadic S3 
write exceptions during the fetching 
and refetching.

| File name  | sha256                                                           |
|------------|------------------------------------------------------------------|
| 177150.pdf | 05ba53532b7bfc15901bc1bd3371421be758bb08cc2070528a49be4c0b77c6c7 |
| 594742.pdf | 1334239e569fad2a30d11f6f90d5f75645ded13870cd9b6118b4930d297a23e9 |
| 706328.pdf | 16cd8100c6a8710d5c404ee11bfc285efee5693c6ceaa42fce2b466051b2c40a |
| 1260258.pdf | 28a410c2b3a767d618b44980be1a68335fd436e70165211d03421fcd198e4de7 |
| 1544119.pdf | 31ca2adee5ea5ac522bf02db2a9a70bdc0e220ccc242dce9b22254e9a3f7c8fa |
| 1591732.pdf | 3354af25e39f6ccfabb7833f14958512537dd019e9d4dddeb912fb5b5799158b |
| 1640603.pdf | 34eb229ecac8ddecf1632a06762a1998477c07d56249db84edfd157245b6022c |
| 1890087.pdf | 3cf45e3dc0fdf429ac894d77ea85460db744dd93c8704102b914974e7b963630 |
| 1920911.pdf | 3df2586c61b34ad857b4f13eebf1bf2fd8f1a9af71c582c26640278166ba1f7f |
| 1992331.pdf | 403f27afa6c84a5fbc512361d9929ef49ae00d399f1b1f876c26a900d056a846 |
| 2519839.pdf | 51467cf4516df4919c3b195ad67c10a668d339a705c4644ce60fd69f39f6730e |
| 2712444.pdf | 577c5f029ff827362b5a71d14f1e4a015bea3eb53960e250ffa1dde2f7ae0050 |
| 2765539.pdf | 59343aae861d86d9d360b4ccf0183f33a77e49b67696ee1f900821e7dad1f04e | 
| 3179469.pdf | 669693d161926d705d63ac8fed895857549b4b7e5d82c2ead56a07c367616fb5 |
| 4170238.pdf | 86931ce5974bff673eb48aa4159b6c215efea4ce636f8e486e9fd54c14e33e9f |
| 4414331.pdf | 8e77a888f6ac85d24ac63e55810c2b2646ba18f540037ac748b50007f7c1c8c8 |
| 4512373.pdf | 91a3d6390adceb54e0ff993f8cfd58250f1bbabfd5ef061a7659ed019897d179 |
| 4977579.pdf | a09de5d289dd95d4b4b71d13e196e05db5ab5d228c65afcd74e5900a40a11b09 |
| 5198714.pdf | a7c81076098d7e179d13ab60a8da6c8897f71315060b73b959667e0f8ff385b9 |
| 5236677.pdf | a9031fc3fbaecf9abcb906e630fdeb90e71e1f9e3d78959ff5101e0fdaa7de65 |
| 5447694.pdf | afd19ad6ca780aa7c90756e97aa20fb11bf4781cfc0ee00e5bf23f66f940f51a |
| 6318895.pdf | cbeb29136aaa7b934c2b8616dafa4b8b9213235ed1be9c818c3858c990914275 |
| 6817632.pdf | dc0840305e174825fa1471dc2ab463bdffece4ec78b496b5e6a65245f4df4cc1 |
| 6940914.pdf | e004f3c7cf38f24ed278b9d3c30c5269f625ed66c623bd6f46ecb3aed9dac3d4 |
| 7241425.pdf | e9b4ec5975d197ffc9d199a188d68cc75cb323ecca545df0668c567bc04a769a |
| 7279847.pdf | eaf2d8ba2606262e861d5e8fe0b26b9c456d1fd3290c17d7c115dc14e02a73ca |
| 7407159.pdf | ef107d1cd9224d3582a1364b012f1585a6192ef1fa3267ab18c078777083091f |
| 7635694.pdf | f670fc79401a83b67f2695666803fb8e2ef2fe05a20c2880ea9f0b7465431523 |
| 7889525.pdf | fe9b31aa4fcf115ae893ffb2937558a11ee7c80ed9dd1908c3a9451ae8d3c140 |

# Extras
## 1. How to manually extract an individual WARC from Common Crawl
First, users need the `cc_warc_file`, the `cc_warc_start` and the `cc_warc_end` from the provenance table.
We'll use `curl` and `gunzip`. Let's say we want to pull `0000000.pdf` which comes from `crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz`
starting at offset `3,724,499` and ends at offset `3,742,341` (inclusive).
1. Prepend `https://data.commoncrawl.org/` to the `cc_warc_file` to get the URL.
2. The http range will be: `3724499-3742341`
3. Fetch the gzipped WARC file: `curl -r 3724499-3742341 https://data.commoncrawl.org/crawl-data/CC-MAIN-2021-31/segments/1627046154042.23/warc/CC-MAIN-20210731011529-20210731041529-00143.warc.gz -o 0000000.warc.gz`
4. `gunzip 0000000.warc.gz`

## 2. How to run your own extraction
The original code for this crawl is available [here](https://github.com/tballison/file-observatory/tree/main/commoncrawl-fetcher).  
After this crawl, we simplified the code and extracted the submodule into its own repo.
As of March 2023, the code is still "ALPHA" grade, but we encourage people
who need a more recent set of files or different mime types to try it out.  Releases are available [here](https://github.com/tballison/commoncrawl-fetcher-lite/releases) 