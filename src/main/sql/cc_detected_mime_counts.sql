select
dm.name as cc_detected_mime, count(1) as cnt
from cc_urls u
left join cc_detected_mimes dm on u.detected_mime=dm.id
group by cc_detected_mime
order by cnt desc

-- note, this is a count of URLs, not unique files