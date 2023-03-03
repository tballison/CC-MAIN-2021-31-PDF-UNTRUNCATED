select
u.id as url_id,
lpad(cpr.id::varchar(12), 7, '0')||'.pdf' as file_name,
h.host, tld, ip_address, country, latitude, longitude
from cc_urls u
left join cc_fetch f on u.id=f.id
left join cc_corpus_ids cpr on cpr.digest=f.fetched_digest
left join cc_hosts h on h.id=u.host
order by cpr.id, u.id
limit 1000