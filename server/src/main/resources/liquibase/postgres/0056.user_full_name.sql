
CREATE FUNCTION f_user_update_firt_last_name()
	RETURNS trigger
	LANGUAGE 'plpgsql'
	COST 100
	VOLATILE NOT LEAKPROOF
AS $BODY$
	DECLARE user_id int;
BEGIN
	NEW.full_name = TRIM(CONCAT(NEW.first_name, ' ', NEW.last_name));

	RETURN NEW;
END;
$BODY$;

-- Creating trigger to update full name if user info changed
CREATE TRIGGER t_user_firt_last_name_update BEFORE INSERT OR UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE f_user_update_firt_last_name();
