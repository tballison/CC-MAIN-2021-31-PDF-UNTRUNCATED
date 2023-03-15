select u.id as url_id,
lpad(c.id::varchar, 7, '0') || '.pdf' as file_name, parse_time_millis, exit_value, timeout, stderr,
pdf_version, creator, producer, created, modified, custom_metadata,
metadata_stream, tagged, user_properties, form, javascript,
pages, page_size, page_rotation, optimized
from cc_urls u
left join cc_fetch f on u.id=f.id
left join pdfinfo2 p on p.path=f.path
left join cc_corpus_ids c on c.digest=p.digest
order by c.id, u.id
--limit 1000