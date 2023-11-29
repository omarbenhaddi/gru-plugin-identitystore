select i.customer_id               as customer_id,
       i.connection_id             as connection_id,
       family_name.attribute_value as family_name,
       first_name.attribute_value  as first_name,
       birthdate.attribute_value   as birthdate,
       email.attribute_value       as email
from identitystore_identity i
         left join identitystore_identity_attribute family_name
                   on family_name.id_identity = i.id_identity and family_name.id_attribute = 3
         left join identitystore_identity_attribute first_name
                   on first_name.id_identity = i.id_identity and first_name.id_attribute = 4
         left join identitystore_identity_attribute birthdate
                   on birthdate.id_identity = i.id_identity and birthdate.id_attribute = 5
         left join identitystore_identity_attribute email
                   on email.id_identity = i.id_identity and email.id_attribute = 12
where i.customer_id is not null and i.customer_id != ''
  and i.connection_id is not null and i.connection_id != ''
  and family_name.attribute_value is not null and family_name.attribute_value != ''
  and first_name.attribute_value is not null and first_name.attribute_value != ''
  and birthdate.attribute_value is not null and birthdate.attribute_value != ''
  and email.attribute_value is not null and email.attribute_value != ''