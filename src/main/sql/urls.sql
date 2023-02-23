-- we don't appear to have unicode characters in the urls field. This returns 0 results
select url from cc_urls where url ~ '[^\u0020-\u007E]' limit 10

select max(length(url)) from cc_urls