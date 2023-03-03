select i.id || '.pdf', digest
from cc_fetch f
join cc_fetch_status s on f.status_id=s.id
join cc_corpus_ids i on fetched_digest = digest
where status_id = 6
order by i.id