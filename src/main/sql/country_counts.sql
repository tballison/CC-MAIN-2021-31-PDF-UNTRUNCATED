select country, count(1) cnt
from cc_urls u
left join cc_hosts h on u.host=h.id
left join cc_fetch f on u.id=f.id
where fetched_length is not null and fetched_length > 0
group by country
order by cnt desc