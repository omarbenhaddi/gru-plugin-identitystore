CONN="psql -h 172.17.0.3 -U idstore -d idstore"
QUERY="$(sed 's/;//g;/^--/ d;s/--.*//g;' export_identities.sql | tr '\n' ' ')"
LIMITED_QUERY="$QUERY LIMIT $1"
echo "\\copy ($LIMITED_QUERY) to 'out.csv' DELIMITER ';' CSV HEADER" | $CONN