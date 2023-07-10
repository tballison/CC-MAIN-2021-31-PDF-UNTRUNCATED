select u.id as url_id,
lpad(cpr.id::varchar(12), 7, '0')||'.pdf' as file_name,
case
	when ts.status is not null
	then ts.status
	when container_exception is not null
	then 'PARSE_EXCEPTION'
	else 'OK'
end as parse_status,
parse_time_millis, ti.mime,
tm.cnt as macro_count, ta.cnt as attachment_count,
created, modified, encrypted, has_xfa, has_xmp, has_collection, has_marked_content,
num_pages, xmp_creator_tool, pdf_producer, pdf_version, pdfa_version, pdfuaid_part,
pdfx_conformance, pdfx_version, pdfxid_version, pdfvt_version, pdf_num_3d_annotations,
pdf_has_acroform_fields, pdf_incremental_updates, pdf_overall_unmapped_unicode_chars,
pdf_contains_damaged_font, pdf_contains_non_embedded_font, has_signature, location,
tika_eval_num_tokens, tika_eval_num_alpha_tokens, tika_eval_lang, tika_eval_oov,
container_exception
from cc_urls u
left join cc_fetch f on u.id=f.id
left join tika_info ti on ti.path=f.path
left join cc_corpus_ids cpr on cpr.digest=f.fetched_digest
left join tika_macros tm on tm.digest=ti.digest
left join tika_attachments ta on ta.digest = ti.digest
left join tika_status ts on ts.path=ti.path
--optional 1k file limit\n" +
where attachment_num = 0
AND cpr.id < 1000
order by cpr.id, u.id