
CREATE OR REPLACE FUNCTION reassign_technology_category(source_id int, target_id int)
RETURNS character varying
LANGUAGE plpgsql
AS $BODY$
BEGIN
	UPDATE assessment_to_technology_category SET technology_category_id = target_id WHERE technology_category_id = source_id;
	UPDATE findings SET technology_category_id = target_id WHERE technology_category_id = source_id;
	UPDATE quant_metrics_to_technology_categories SET technology_category_id = target_id WHERE technology_category_id = source_id;
	UPDATE technologies SET technology_category_id = target_id WHERE technology_category_id = source_id;
	UPDATE vulnerability_to_technology_category SET technology_category_id = target_id WHERE technology_category_id = source_id;

	SELECT 'OK';
END;
$BODY$;

CREATE OR REPLACE FUNCTION delete_technology_category(source_id int)
RETURNS character varying
LANGUAGE plpgsql
AS $BODY$
BEGIN
	DELETE FROM assessment_to_technology_category WHERE technology_category_id = source_id;
	DELETE FROM findings WHERE technology_category_id = source_id;
	DELETE FROM quant_metrics_to_technology_categories WHERE technology_category_id = source_id;
	DELETE FROM technologies WHERE technology_category_id = source_id;
	DELETE FROM vulnerability_to_technology_category WHERE technology_category_id = source_id;
	DELETE FROM technology_categories WHERE id = source_id;

	SELECT 'OK';
END;
$BODY$;
