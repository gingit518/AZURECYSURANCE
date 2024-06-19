

INSERT INTO organization_email_templates (id, organization_id, type, subject, content) VALUES
    (1, 1047, 'VENDOR_EMPLOYEE_INVITATION', 'User registration on vRisk', '
        <!DOCTYPE html>
        <html xmlns:th="http://www.thymeleaf.org">
            <head>
                <title th:remove="all">Template for HTML email for Vendor employees User invitation Email </title>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
            </head>
            <body>
                <p>
                    Hello, <span th:text="${name}">EK</span>!
                </p>

                <p>
                    <span th:text="${organizationName}"></span> is using the VRisk product to manage their vendor cyber risk.
                    You have had a new account created in vRisk with the user id <span th:text="${accountName}"></span>.
                    Matt Harrison at SFBC is your contact and you may verify this with him.  His email is mharrison@sfbcic.com.
                    If you think this is some mistake, please notify us at info@cyberinnovativetech.com and disregard this email.
                </p>

                <p>
                    Click <a th:href="${resetPasswordLink}">here</a> to create a new password
                    or copy and paste this URL to browser <span th:text="${resetPasswordLink}"></span>
                </p>

                <p>
                    Regards, <br />
                    <em>The vRisk Team</em>
                </p>
            </body>
        </html>
    ');

ALTER SEQUENCE organization_email_templates_id_seq RESTART WITH 2;
