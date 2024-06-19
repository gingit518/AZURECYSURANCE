
ALTER TABLE policies ALTER COLUMN created_by_id DROP NOT NULL;
ALTER TABLE risk_model_domains ALTER COLUMN created_by_id DROP NOT NULL;
ALTER TABLE risk_model_domains ALTER COLUMN risk_management_owner_id DROP NOT NULL;


CREATE OR REPLACE FUNCTION public.delete_organization(organizationId bigint)
 RETURNS bigint
 LANGUAGE plpgsql
AS $function$
DECLARE
deletedRiskModelId bigint;
   riskModel record;
begin

	RAISE NOTICE 'Removing Organization: %', organizationId;

delete from api_keys where organization_id = organizationId;
RAISE NOTICE 'Removing Organization API Keys: %', organizationId;

delete from assessment_to_process where process_id in (select id from process where organization_id = organizationId);
RAISE NOTICE 'Removing Assessment Process links for Organization: %', organizationId;

delete from process_to_data_type where process_id in (select id from process where organization_id = organizationId);
RAISE NOTICE 'Removing Data Types Process links for Organization: %', organizationId;

delete from process_to_data_asset where process_id in (select id from process where organization_id = organizationId);
RAISE NOTICE 'Removing Data Asset Process links for Organization: %', organizationId;

delete from process_to_business_unit_used where process_id in (select id from process where organization_id = organizationId);
RAISE NOTICE 'Removing Business Units Process links for Organization: %', organizationId;

delete from process_to_system where process_id in (select id from process where organization_id = organizationId);
RAISE NOTICE 'Removing System - Process links for Organization: %', organizationId;

delete from assessment_to_system where system_id in (select id from systems where organization_id = organizationId);
RAISE NOTICE 'Removing Assessment System links for Organization: %', organizationId;

delete from associate_systems where system_id in (select id from systems where organization_id = organizationId);
RAISE NOTICE 'Removing Associate System links for Organization: %', organizationId;

delete from regulations_to_technology_categories where technology_category_id in (select id from technology_categories where organization_id = organizationId);
delete from assessment_to_technology_category where technology_category_id in (select id from technology_categories where organization_id = organizationId);
RAISE NOTICE 'Removing Assessment System links for Organization: %', organizationId;

FOR riskModel IN SELECT r.* FROM risk_models r WHERE r.organization_id = organizationId
	loop
    	RAISE NOTICE 'Removing Risk Model: %', riskModel.id;
perform public.delete_risk_model(riskModel.id);
END loop;

delete from finding_to_security_requirement where finding_id in (select id from findings where organization_id = organizationId);
delete from findings_to_tasks where finding_id in (select id from findings where organization_id = organizationId);
delete from finding_to_assessment where finding_id in (select id from findings where organization_id = organizationId);
delete from findings where organization_id = organizationId;
RAISE NOTICE 'Removing Finding links for Organization: %', organizationId;

delete from systems where organization_id = organizationId;
RAISE NOTICE 'Removing Systems for Organization: %', organizationId;

delete from process where organization_id = organizationId;
RAISE NOTICE 'Removing Processes for Organization: %', organizationId;

	RAISE NOTICE 'Removing Technology links for Organization: %', organizationId;
delete from regulations_to_technologies where technology_id in (select id from technologies where organization_id = organizationId);
delete from vendor_to_technology where technology_id in (select id from technologies where organization_id = organizationId);
delete from vulnerability_to_technology where technology_id in (select id from technologies where organization_id = organizationId);
delete from technologies where organization_id = organizationId;
RAISE NOTICE 'Removing Technologies for Organization: %', organizationId;

delete from technology_categories where organization_id = organizationId;
RAISE NOTICE 'Removing Technology Categories for Organization: %', organizationId;

delete from assessments_to_tasks where assessment_id in (select id  from assessments where organization_id = organizationId);
delete from assessments_to_assessment_types where assessment_id in (select id  from assessments where organization_id = organizationId);
delete from assessments where organization_id = organizationId;

delete from control_tests where organization_id = organizationId;
delete from control_tests where control_subcategory_id in (select id from control_subcategories  where organization_id = organizationId);
delete from assessment_weights where organization_id = organizationId;
delete from assessment_weights where control_subcategory_id in (select id from control_subcategories  where organization_id = organizationId);
delete from findings where control_subcategory_id in (select id from control_subcategories  where organization_id = organizationId);
delete from control_subcategories  where organization_id = organizationId;
delete from control_categories where organization_id = organizationId;
delete from control_functions  where organization_id = organizationId;
delete from assessment_types_to_policies where assessment_type_id in (select id from assessment_types where organization_id = organizationId);
delete from assessment_types where organization_id = organizationId;
delete from security_requirements where organization_id = organizationId;
delete from security_control_families where organization_id = organizationId;

delete from associate_vendors where organization_id = organizationId;
delete from assessment_types_to_policies where policy_id in (select id from policies where organization_id = organizationId);
delete from policies where organization_id = organizationId;
delete from data_type_classifications where organization_id = organizationId;
delete from data_asset_classifications where organization_id = organizationId;
delete from data_domains where organization_id = organizationId;
delete from security_audit_comments where organization_id = organizationId;

delete from vulnerabilities_to_tasks where task_id in (select id from tasks where organization_id = organizationId);
delete from tasks where organization_id = organizationId;
delete from projects where organization_id = organizationId;
delete from formula_items where formula_id in (select id from formulas where organization_id = organizationId);
delete from formulas where organization_id = organizationId;
delete from vulnerabilities where organization_id = organizationId;

delete from gdpr_article_status_log where organization_id = organizationId;
delete from gdpr_article_status where organization_id = organizationId;
delete from gdpr_article_paragraph where organization_id = organizationId;
delete from gdpr_article_chapter_section where organization_id = organizationId;
delete from gdpr_article_chapter where organization_id = organizationId;
delete from gdpr_article where organization_id = organizationId;

delete from user_assigned_systems where user_id in (select id from users where organization_id = organizationId);
delete from user_assigned_vendors where user_id in (select id from users where organization_id = organizationId);
delete from document_access_tokens where user_id in (select id from users where organization_id = organizationId);
delete from users_to_vendors where user_id in (select id from users where organization_id = organizationId);
delete from idp_users where user_id in (select id from users where organization_id = organizationId);
delete from user_password_reset_links where user_id in (select id from users where organization_id = organizationId);
update organizations set owner_id = null where owner_id in (select id from users where organization_id = organizationId);
update users set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update users set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update technology_categories set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update technology_categories set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update data_type_classifications set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update data_type_classifications set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update quants set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update quants set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update regulations set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update regulations set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update security_audit_comments set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update security_audit_comments set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update data_asset_classifications set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update data_asset_classifications set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update organizations_agreements set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update organizations_agreements set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update question_answers_to_systems set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update question_answers_to_systems set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update question_answers_to_vendors set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update question_answers_to_vendors set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update assessments set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update assessments set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update associate_vendors set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update associate_vendors set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update associate_models set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update associate_models set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update associate_models set owner_id = null where owner_id in (select id from users where organization_id = organizationId);
update findings set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update findings set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update business_units set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update business_units set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update business_units set created_by_id = null, updated_by_id = null, infosec_focal_person_id = null, owner_id = null where organization_id = organizationId;
update policies set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update policies set approved_by_id = null where approved_by_id in (select id from users where organization_id = organizationId);
update process set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update process set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update qual_metrics set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update qual_metrics set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update qualitative_questions set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update qualitative_questions set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update quant_metrics set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update quant_metrics set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update risk_metrics set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update risk_metrics set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update risk_model_constants set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update risk_model_constants set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update risk_model_domains set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update risk_model_domains set risk_management_owner_id = null where risk_management_owner_id in (select id from users where organization_id = organizationId);
update systems set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update systems set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update systems set owner_id = null where owner_id in (select id from users where organization_id = organizationId);
update technologies set created_by_id = null where created_by_id in (select id from users where organization_id = organizationId);
update technologies set updated_by_id = null where updated_by_id in (select id from users where organization_id = organizationId);
update tasks set task_manager_id = null where task_manager_id in (select id from users where organization_id = organizationId);
update tasks set task_assignee_id = null where task_assignee_id in (select id from users where organization_id = organizationId);
delete from users where organization_id = organizationId;
delete from business_unit_levels where child_id in (select id from business_units where organization_id = organizationId);
delete from business_units where organization_id = organizationId;

delete from idp_users where user_id in (select u.id  from users u join organizations o on u.organization_id = o.id where o.root_parent_id = organizationId);
delete from user_password_reset_links where user_id in (select u.id  from users u join organizations o on u.organization_id = o.id where o.root_parent_id = organizationId);
delete from user_assigned_vendors where vendor_id in (select id from organizations where root_parent_id = organizationId);
delete from users where organization_id in (select id from organizations where root_parent_id = organizationId);
delete from organizations where root_parent_id = organizationId;
RAISE NOTICE 'Removing Vendors and Subsidiaries for Organization: %', organizationId;

delete from organizations_to_agreements where organization_id = organizationId;
delete from organization_to_language where organization_id = organizationId;
delete from audit_log_item_ids where organization_id = organizationId;
delete from audit_logs where organization_id = organizationId;
delete from organizations where id = organizationId;
RAISE NOTICE 'Removing Organization: %', organizationId;

RETURN organizationId;
END;
$function$
;


CREATE OR REPLACE FUNCTION public.delete_risk_model(riskmodelid bigint)
 RETURNS bigint
 LANGUAGE plpgsql
AS $function$
DECLARE
deletedRiskModelId bigint;
BEGIN
DELETE FROM metric_result_answers WHERE risk_model_id = riskModelId;
DELETE FROM metric_risks WHERE risk_model_id = riskModelId;

DELETE FROM associate_models WHERE risk_model_id = riskModelId;
DELETE FROM questions_to_risk_types where risk_type_id in (select rt.id from risk_types rt join category_domains cd on cd.id=rt.category_domain_id where cd.risk_model_id = riskModelId);
DELETE FROM category_domains WHERE risk_model_id = riskModelId;
DELETE FROM qualitative_questions WHERE risk_model_id = riskModelId;
DELETE FROM likelihood_metrics WHERE risk_model_id = riskModelId;
DELETE FROM risk_model_domains WHERE risk_model_id = riskModelId;
delete from quant_metrics_to_data_type_classifications where quant_metric_id in (select qm.id from quant_metrics qm where qm.risk_model_id = riskModelId);
DELETE FROM metric_formula_items WHERE quant_metric_id in (select qm.id from quant_metrics qm where qm.risk_model_id = riskModelId);
DELETE FROM quant_metrics WHERE risk_model_id = riskModelId;
DELETE FROM qual_metrics WHERE risk_model_id = riskModelId;
DELETE FROM metric_formula_items WHERE risk_model_constant_ref_id in (select id FROM risk_model_constants WHERE risk_model_id = riskModelId);
DELETE FROM risk_model_constants WHERE risk_model_id = riskModelId;
DELETE FROM risk_metrics WHERE risk_model_id = riskModelId;
-- DELETE FROM quants WHERE risk_model_id = riskModelId;

DELETE FROM risk_models WHERE id = riskModelId;

RETURN riskModelId;
END;
$function$
;
