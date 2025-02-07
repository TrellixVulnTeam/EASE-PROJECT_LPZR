#!/bin/bash

function sendEmail() {
    email="$2"
    name="$1"
    curl --request POST \
	--url https://api.sendgrid.com/v3/mail/send \
	--header 'authorization: Bearer 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' \
	--header 'Content-Type: application/json' \
	--data '{"personalizations": [{"to": [{"email":"'$email'", "name":"'$name'"}], "substitutions": {"username":"'$name'"}}], "from": {"email":"sergii@ease.space", "name":"Sergii"}, "template_id": "2c03ac41-648f-49c4-95dc-7057a09de38b", "reply_to": {"email":"contact@ease.space", "name":"Agathe @Ease"}}'
}

#Parenthesis to cast this list of words to array
resultSet=($(mysql -u client -pP6au23q7 ease --batch --skip-column-names -se "SELECT firstName, email FROM (SELECT firstName, email, count(apps.id) AS appCount FROM users JOIN status ON (users.status_id = status.id) JOIN profiles ON (profiles.user_id = users.id) JOIN apps ON (apps.profile_id = profiles.id) JOIN websiteApps ON (apps.id = websiteApps.app_id) WHERE DATE(last_connection) = DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND websiteApps.type <> 'websiteApp' GROUP BY firstName, email) AS t WHERE appCount > 3;"))

for i in ${!resultSet[@]};
do
    if [[ $(( $i % 2 )) == 1 ]]; then
	name=${resultSet[$i-1]}
	email=${resultSet[$i]}
	sendEmail $name $email
    fi
done