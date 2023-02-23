select
--u.id as url_id,
lpad(cpr.id::varchar(12), 7, '0')||'.pdf' as file_name,
u.url, u.digest as cc_digest,
m.name as cc_http_mime, dm.name as cc_detected_mime,
w.name as cc_warc_file_name,
u.warc_offset as cc_warc_start,
(u.warc_offset + u.warc_length-1) as cc_warc_end,
t.name as cc_truncated,
fs.status as fetched_status,
f.fetched_digest, f.fetched_length
from cc_urls u
left join cc_mimes m on u.mime=m.id
left join cc_detected_mimes dm on u.detected_mime=dm.id
left join cc_truncated t on u.truncated=t.id
left join cc_fetch f on u.id=f.id
left join cc_fetch_status fs on f.status_id=fs.id
left join cc_warc_file_name w on u.warc_file_name=w.id
left join cc_corpus_ids cpr on cpr.digest=f.fetched_digest
order by cpr.id
limit 4000