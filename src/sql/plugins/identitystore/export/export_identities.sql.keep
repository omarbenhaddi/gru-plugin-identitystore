select
    '' as external_customer_id,
    i.customer_id as customer_id,
    i.connection_id as connection_id,
    gender.attribute_value as gender,
    gender_certificate.certifier_code as gender_certificate,
    gender_certificate.certificate_date as gender_certification_date,
    preferred_username.attribute_value as preferred_username,
    preferred_username_certificate.certifier_code as preferred_username_certificate,
    preferred_username_certificate.certificate_date as preferred_username_certification_date,
    family_name.attribute_value as family_name,
    family_name_certificate.certifier_code as family_name_certificate,
    family_name_certificate.certificate_date as family_name_certification_date,
    first_name.attribute_value as first_name,
    first_name_certificate.certifier_code as first_name_certificate,
    first_name_certificate.certificate_date as first_name_certification_date,
    birthdate.attribute_value as birthdate,
    birthdate_certificate.certifier_code as birthdate_certificate,
    birthdate_certificate.certificate_date as birthdate_certification_date,
    birthplace.attribute_value as birthplace,
    birthplace_certificate.certifier_code as birthplace_certificate,
    birthplace_certificate.certificate_date as birthplace_certification_date,
    birthcountry.attribute_value as birthcountry,
    birthcountry_certificate.certifier_code as birthcountry_certificate,
    birthcountry_certificate.certificate_date as birthcountry_certification_date,
    address.attribute_value as address,
    address_certificate.certifier_code as address_certificate,
    address_certificate.certificate_date as address_certification_date,
    address_detail.attribute_value as address_detail,
    address_detail_certificate.certifier_code as address_detail_certificate,
    address_detail_certificate.certificate_date as address_detail_certification_date,
    address_postal_code.attribute_value as address_postal_code,
    address_postal_code_certificate.certifier_code as address_postal_code_certificate,
    address_postal_code_certificate.certificate_date as address_postal_code_certification_date,
    address_city.attribute_value as address_city,
    address_city_certificate.certifier_code as address_city_certificate,
    address_city_certificate.certificate_date as address_city_certification_date,
    email.attribute_value as email,
    email_certificate.certifier_code as email_certificate,
    email_certificate.certificate_date as email_certification_date,
    mobile_phone.attribute_value as mobile_phone,
    mobile_phone_certificate.certifier_code as mobile_phone_certificate,
    mobile_phone_certificate.certificate_date as mobile_phone_certification_date,
    fixed_phone.attribute_value as fixed_phone,
    fixed_phone_certificate.certifier_code as fixed_phone_certificate,
    fixed_phone_certificate.certificate_date as fixed_phone_certification_date,
    login.attribute_value as login,
    login_certificate.certifier_code as login_certificate,
    login_certificate.certificate_date as login_certification_date,
    fc_key.attribute_value as fc_key,
    fc_key_certificate.certifier_code as fc_key_certificate,
    fc_key_certificate.certificate_date as fc_key_certification_date,
    birthplace_code.attribute_value as birthplace_code,
    birthplace_code_certificate.certifier_code as birthplace_code_certificate,
    birthplace_code_certificate.certificate_date as birthplace_code_certification_date,
    birthcountry_code.attribute_value as birthcountry_code,
    birthcountry_code_certificate.certifier_code as birthcountry_code_certificate,
    birthcountry_code_certificate.certificate_date as birthcountry_code_certification_date
from identitystore_identity i
         left join identitystore_identity_attribute gender on gender.id_identity = i.id_identity and gender.id_attribute = 1
         left join identitystore_identity_attribute_certificate gender_certificate on gender_certificate.id_attribute_certificate=gender.id_certification
         left join identitystore_identity_attribute preferred_username on preferred_username.id_identity = i.id_identity and preferred_username.id_attribute = 2
         left join identitystore_identity_attribute_certificate preferred_username_certificate on preferred_username_certificate.id_attribute_certificate=preferred_username.id_certification
         left join identitystore_identity_attribute family_name on family_name.id_identity = i.id_identity and family_name.id_attribute = 3
         left join identitystore_identity_attribute_certificate family_name_certificate on family_name_certificate.id_attribute_certificate=family_name.id_certification
         left join identitystore_identity_attribute first_name on first_name.id_identity = i.id_identity and first_name.id_attribute = 4
         left join identitystore_identity_attribute_certificate first_name_certificate on first_name_certificate.id_attribute_certificate=first_name.id_certification
         left join identitystore_identity_attribute birthdate on birthdate.id_identity = i.id_identity and birthdate.id_attribute = 5
         left join identitystore_identity_attribute_certificate birthdate_certificate on birthdate_certificate.id_attribute_certificate=birthdate.id_certification
         left join identitystore_identity_attribute birthplace on birthplace.id_identity = i.id_identity and birthplace.id_attribute = 6
         left join identitystore_identity_attribute_certificate birthplace_certificate on birthplace_certificate.id_attribute_certificate=birthplace.id_certification
         left join identitystore_identity_attribute birthcountry on birthcountry.id_identity = i.id_identity and birthcountry.id_attribute = 7
         left join identitystore_identity_attribute_certificate birthcountry_certificate on birthcountry_certificate.id_attribute_certificate=birthcountry.id_certification
         left join identitystore_identity_attribute address on address.id_identity = i.id_identity and address.id_attribute = 8
         left join identitystore_identity_attribute_certificate address_certificate on address_certificate.id_attribute_certificate=address.id_certification
         left join identitystore_identity_attribute address_detail on address_detail.id_identity = i.id_identity and address_detail.id_attribute = 9
         left join identitystore_identity_attribute_certificate address_detail_certificate on address_detail_certificate.id_attribute_certificate=address_detail.id_certification
         left join identitystore_identity_attribute address_postal_code on address_postal_code.id_identity = i.id_identity and address_postal_code.id_attribute = 10
         left join identitystore_identity_attribute_certificate address_postal_code_certificate on address_postal_code_certificate.id_attribute_certificate=address_postal_code.id_certification
         left join identitystore_identity_attribute address_city on address_city.id_identity = i.id_identity and address_city.id_attribute = 11
         left join identitystore_identity_attribute_certificate address_city_certificate on address_city_certificate.id_attribute_certificate=address_city.id_certification
         left join identitystore_identity_attribute email on email.id_identity = i.id_identity and email.id_attribute = 12
         left join identitystore_identity_attribute_certificate email_certificate on email_certificate.id_attribute_certificate=email.id_certification
         left join identitystore_identity_attribute mobile_phone on mobile_phone.id_identity = i.id_identity and mobile_phone.id_attribute = 13
         left join identitystore_identity_attribute_certificate mobile_phone_certificate on mobile_phone_certificate.id_attribute_certificate=mobile_phone.id_certification
         left join identitystore_identity_attribute fixed_phone on fixed_phone.id_identity = i.id_identity and fixed_phone.id_attribute = 14
         left join identitystore_identity_attribute_certificate fixed_phone_certificate on fixed_phone_certificate.id_attribute_certificate=fixed_phone.id_certification
         left join identitystore_identity_attribute login on login.id_identity = i.id_identity and login.id_attribute = 16
         left join identitystore_identity_attribute_certificate login_certificate on login_certificate.id_attribute_certificate=login.id_certification
         left join identitystore_identity_attribute fc_key on fc_key.id_identity = i.id_identity and fc_key.id_attribute = 31
         left join identitystore_identity_attribute_certificate fc_key_certificate on fc_key_certificate.id_attribute_certificate=fc_key.id_certification
         left join identitystore_identity_attribute birthplace_code on birthplace_code.id_identity = i.id_identity and birthplace_code.id_attribute = 56
         left join identitystore_identity_attribute_certificate birthplace_code_certificate on birthplace_code_certificate.id_attribute_certificate=birthplace_code.id_certification
         left join identitystore_identity_attribute birthcountry_code on birthcountry_code.id_identity = i.id_identity and birthcountry_code.id_attribute = 57
         left join identitystore_identity_attribute_certificate birthcountry_code_certificate on birthcountry_code_certificate.id_attribute_certificate=birthcountry_code.id_certification
