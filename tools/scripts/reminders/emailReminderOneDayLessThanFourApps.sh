#!/bin/bash

function sendEmail() {
    email="$2"
    name="$1"
    curl --request POST \
	--url https://api.sendgrid.com/v3/mail/send \
	--header 'authorization: Bearer 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' \
	--header 'Content-Type: application/json' \
	--data '{"personalizations": [{"to": [{"email":"'$email'", "name":"'$name'"}], "substitutions": {"#username": "'$name'", "#linkUrl": "http://localhost:8080/index.jsp?importAccounts=true"}}], "from": {"email":"contact@ease.space", "name":"Agathe @Ease"}, "template_id": "1a65dae4-89a1-454a-9604-3d3730d8237f"}'
}

#Parenthesis to cast this list of words to array
resultSet=($(mysql -u client -pP6au23q7 ease --batch --skip-column-names -se "SELECT firstName, email FROM (SELECT firstName, email, count(websiteApps.id) AS appCount FROM users JOIN profiles ON (profiles.user_id = users.id) JOIN apps ON (apps.profile_id = profiles.id) LEFT JOIN websiteApps ON (apps.id = websiteApps.app_id AND websiteApps.type <> 'websiteApp') WHERE DATE(registration_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) GROUP BY firstName, email) AS t WHERE appCount <= 3;"))

for i in ${!resultSet[@]};
do
    if [[ $(( $i % 2 )) == 1 ]]; then
	name=${resultSet[$i-1]}
  echo $name
	email=${resultSet[$i]}
	sendEmail $name $email
    fi
done