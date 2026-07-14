ALTER TABLE IF EXISTS tb_accounts
    ADD COLUMN IF NOT EXISTS account_password_hash varchar(255);

UPDATE tb_accounts
SET account_password_hash = 'legacy-password-not-usable'
WHERE account_password_hash IS NULL;

ALTER TABLE IF EXISTS tb_accounts
    ALTER COLUMN account_password_hash SET NOT NULL;
