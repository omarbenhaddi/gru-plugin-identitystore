CONN="$1"
QUERY="$(sed 's/;//g;/^--/ d;s/--.*//g;' export_identities.sql | tr '\n' ' ')"
LIMITED_QUERY="$QUERY LIMIT $2 OFFSET $3"
echo "\\copy ($LIMITED_QUERY) to 'idstore-jdd-$2-$3.csv' DELIMITER ';' CSV HEADER" | $CONN