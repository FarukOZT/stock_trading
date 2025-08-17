# stock_trading

You have already downloaded Intellij Idea or Eclipse and docker to run this project before. If you dont have docker you have to install docker.

1- Open project location and run cmd or another terminal.

2- run this command cd src/main/resources

3- run this command docker-compose up -d

4- go to localhost:8180

5- login with username and password (admin - admin)

6- create client

a- that name is brokerage-api click next

b- click toggle button of Client Authentication click next

c- write * in Valid redirect URIs and save

7- create realm role that name is customer (lowercase)

8- click Clients -> brokerage-api -> credentials . You can see Client Secret. Copy it and paste application.yml line 29.

9- Now, configuration is ready. We need to token. You can take admin token if you send request this url. Dont forget change your secret

curl --location 'http://localhost:8180/realms/master/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Cookie: JSESSIONID=D8CB5A4A227A4C01B043A939044AC600' \
--data-urlencode 'client_id=brokerage-api' \
--data-urlencode 'username=admin' \
--data-urlencode 'password=admin' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_secret={YOUR SECRET}'

10- You can create customer using this curl. You can change name and password.

curl --location 'http://localhost:8081/customers' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer token' \
--header 'Cookie: JSESSIONID=D8CB5A4A227A4C01B043A939044AC600' \
--data-raw '{
"username": "warren",
"email": "warren@gmail.com",
"password": "asasdd123"
}'

11- If you take token successfully hereafter control all endpoints.

12- If you want to use postgresql change active profile from application.yml. Default is h2. Dont forget change JDBC URL as jdbc:h2:mem:brokerage to use h2 

13- Other curls are here;

getCustomerToken:

curl --location 'http://localhost:8180/realms/master/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--header 'Cookie: JSESSIONID=130DFFDC6D3E5DC7DB46296DA328B602' \
--data-urlencode 'client_id=brokerage-api' \
--data-urlencode 'username=warren' \
--data-urlencode 'password=buffet' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_secret={YOUR SECRET}'

createOrder:

curl --location 'http://localhost:8081/api/orders' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer token' \
--header 'Cookie: JSESSIONID=130DFFDC6D3E5DC7DB46296DA328B602' \
--data '{
"customerId": 1,
"assetName": "ING",
"orderSide": "SELL",
"size": 1,
"price": 1
}'

matchOrder:

curl --location 'http://localhost:8080/api/orders/match' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer token' \
--header 'Cookie: JSESSIONID=D8CB5A4A227A4C01B043A939044AC600' \
--data '{
"id": 123,
"customerId": 456
}'

getOrder:

curl --location 'http://localhost:8081/api/orders?customerId=1&startDate=2025-08-01T00:00:00&endDate=2025-08-17T23:59:59' \
--header 'Authorization: Bearer token
--header 'Cookie: JSESSIONID=130DFFDC6D3E5DC7DB46296DA328B602'

cancelOrder:

curl --location --request DELETE 'http://localhost:8081/api/orders?customerId=1&orderId=1' \
--header 'Authorization: Bearer token
--header 'Cookie: JSESSIONID=130DFFDC6D3E5DC7DB46296DA328B602'


