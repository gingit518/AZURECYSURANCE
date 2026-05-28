
INSERT INTO public.external_analytics (id,organization_id,external_analytics_type,name,description,is_public,logo,logo_document_id) VALUES
	(41,1047,'DASHBOARD'::public.externalanalyticstype,'ROI and Payback Periods',NULL,false,NULL,NULL);

ALTER SEQUENCE external_analytics_id_seq RESTART WITH 42;

INSERT INTO public.external_analytics_parameters (id,organization_id,external_analytics_id,name,value) VALUES
(1022,1047,41,'DASHBOARD_CONFIG_JSON','{
    "sections": [
        {
            "name": "ROI",
            "dashboardItems": [
                {
                    "type": "Table",
                    "name": "ROI",
                    "gridItems": [
                        [
                            {"value": "Storage ROI"},
                            {"valueRef": "quant:192:formula"},
                            {"symbol": "$", "textAlign": "right", "valueRef": "quant:192:value", "digits": 2, "drilldown": {"view": "organization_quants", "params": {"metricId": "192", "metricDomain": "1031"}}}
                        ],
                        [
                            {"value": "Ransomware ROI"},
                            {"valueRef": "quant:193:formula"},
                            {"symbol": "$", "textAlign": "right", "valueRef": "quant:193:value", "digits": 2, "drilldown": {"view": "organization_quants", "params": {"metricId": "193", "metricDomain": "1031"}}}
                        ]
                    ]
                }
            ]
        },
        {
            "name": "Paybacks",
            "dashboardItems": [
                {
                    "type": "Table",
                    "name": "Paybacks",
                    "gridItems": [
                        [{"value": "Storage Payback Period (days)"}, {"symbol": "(days)", "textAlign": "right", "valueRef": "quant:194:value", "digits": 2}],
                        [{"value": "Storage Payback Period 22 (days)"}, {"symbol": "(days)", "textAlign": "right", "valueRef": "quant:194:value", "digits": 2}],
                        [{"value": "Ransomware Payback Period (days)"}, {"symbol": "(days)", "textAlign": "right", "valueRef": "quant:195:value", "digits": 2}]
                    ]
                }
            ]
        },
        {
            "name": "Client Data",
            "dashboardItems": [
                {
                    "type": "Table",
                    "name": "Client Data",
                    "gridItems": [
                        [{"value": "Total Data Stored (TB)"}, {"symbol": "(TB)", "textAlign": "right", "valueRef": "quant:190:value", "digits": 2}],
                        [{"value": "Cost to restore 1TB (compute + labor + IR time, in $/TB)"}, {"symbol": "($/TB)", "textAlign": "right", "valueRef": "quant:196:value", "digits": 2}],
                        [{"value": "Downtime lost per hour ($/hour)"}, {"symbol": "($/hour)", "textAlign": "right", "valueRef": "quant:189:value", "digits": 2}],
                        [{"value": "Cost of a Ransomware"}, {"symbol": "$", "textAlign": "right", "valueRef": "quant:197:value", "digits": 2}]
                    ]
                }
            ]
        }
    ]
}'),
(1021,1047,41,'DASHBOARD_SECTION_NAME','Elastio Section');

ALTER SEQUENCE external_analytics_parameters_id_seq RESTART WITH 1022;

INSERT INTO public.external_analytics_to_roles (id,external_analytic_id,role_id) VALUES
	(56,41,128);

ALTER SEQUENCE external_analytics_to_roles_id_seq RESTART WITH 57;
