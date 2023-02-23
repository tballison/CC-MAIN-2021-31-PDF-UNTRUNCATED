select count(1)
from cc_urls u
left join cc_fetch f on u.id = f.id
where fetched_length is not null and fetched_length > 0