select
m.name as cc_http_mime, count(1) as cnt
from cc_urls u
left join cc_mimes m on u.mime=m.id
--left join cc_detected_mimes dm on u.detected_mime=dm.id
group by m.name
order by cnt desc

-- note, this is a count of URLs, not unique files