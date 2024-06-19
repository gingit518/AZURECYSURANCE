
CREATE OR REPLACE FUNCTION delete_risk_model(riskModelId bigint)
RETURNS bigint AS
$BODY$
DECLARE
    deletedRiskModelId bigint;
BEGIN
    DELETE FROM metric_result_answers WHERE risk_model_id = riskModelId;
    DELETE FROM metric_risks WHERE risk_model_id = riskModelId;

    DELETE FROM associate_models WHERE risk_model_id = riskModelId;
    DELETE FROM category_domains WHERE risk_model_id = riskModelId;
    DELETE FROM qualitative_questions WHERE risk_model_id = riskModelId;
    DELETE FROM likelihood_metrics WHERE risk_model_id = riskModelId;
    DELETE FROM risk_model_domains WHERE risk_model_id = riskModelId;
    DELETE FROM quant_metrics WHERE risk_model_id = riskModelId;
    DELETE FROM qual_metrics WHERE risk_model_id = riskModelId;
    DELETE FROM quants WHERE risk_model_id = riskModelId;

    DELETE FROM risk_models WHERE id = riskModelId;

    RETURN riskModelId;
END;
$BODY$
LANGUAGE plpgsql;
