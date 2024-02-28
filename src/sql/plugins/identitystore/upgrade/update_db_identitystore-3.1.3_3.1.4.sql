-- #416 - add result limit during duplicates detection for a given rule
ALTER TABLE identitystore_duplicate_rule ADD COLUMN detection_limit INTEGER DEFAULT -1;