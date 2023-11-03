CONN="$1"
QUERY="$(sed 's/;//g;/^--/ d;s/--.*//g;' export_identities.sql | tr '\n' ' ')"
LIMITED_QUERY="$QUERY where i.id_identity between $2 and $3"
echo "\\copy ($LIMITED_QUERY) to 'idstore-jdd-$2-$3.csv' DELIMITER ';' CSV HEADER" | $CONN