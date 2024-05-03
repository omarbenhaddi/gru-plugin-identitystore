UPDATE identitystore_ref_attribute
SET validation_regex='^[A-Za-zÀ-Üà-ü\d\s''-]+$',
    validation_error_message='uniquement caractères alphanumériques, apostrophe, espace et tirets.',
    validation_error_message_key='identitystore.attribute.status.validation.error.only.alphanum.apostrophe.space.dash'
WHERE key_name='birthplace';