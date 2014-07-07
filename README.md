Example of restful web service using OAuth2 and Play! framework
===========

This example application integrates spring-security-oauth2 with Play to secure its restful APIs.

Run the tests to make sure everything is working.

``` bash
$ activator test
```

Start the application.

``` bash
$ activator run
```

Once started, the application listens to HTTP requests at port 9000. In order to interact with it, one must first obtain an access token for a predefined client using client id "secure-client" and secret "client_password".

``` bash
$ curl -X POST -H "Content-type: application/json" -d '{"clientId": "secure-client", "clientSecret": "client_password"}' http://localhost:9000/client/authenticate
```

The response contains the client access token.

``` json
{"accessToken":"d1e2cd6d-2171-4c6d-86e8-bbabc3fcdcb5","clientId":"secure-client","expiration":1405561925380}
```

The client access token grants access to the user authentication API. By using it as "Bearer" authorization, one can obtain a user access token with username and password. In this example, all non-empty usernames are accepted, and the password is always "password".

``` bash
$ curl -X POST -H "Content-type: application/json" -H "Authorization: Bearer d1e2cd6d-2171-4c6d-86e8-bbabc3fcdcb5" -d '{"username": "test", "password": "password"}' http://localhost:9000/user/authenticate
```

This request returns a user access token, which can be used to access other restful APIs that require user permission. There is also a refresh token that can be used to get a new user access token when the old one is expired.

``` json
{"accessToken":"4fb09f30-470d-48de-b038-8e2f73f7afa8","username":"test","expiration":1405562230945,"refreshToken":"31ea8263-9876-478b-a3a6-c81a7186528a"}
```

To access the API that returns user information, the user access token must be provided as "Bearer" authorization.

``` bash
$ curl -H "Authorization: Bearer 4fb09f30-470d-48de-b038-8e2f73f7afa8" http://localhost:9000/user/get
```

The response shows the username and some additional information.

``` json
{"username":"test","isActive":true}
```

Finally, to refresh the user access token, one may send a request using a valid client access token as the "Bearer" authorization, and the refresh token as in request body.

``` bash
$ curl -X POST -H "Content-type: application/json" -H "Authorization: Bearer d1e2cd6d-2171-4c6d-86e8-bbabc3fcdcb5" -d '{"refreshToken": "31ea8263-9876-478b-a3a6-c81a7186528a"}' http://localhost:9000/user/refresh
```

The response contains a new user access token and the same refresh token. The refresh token can be used again and again. The new user access token can be used from now on to grant access to user APIs. The old user access token is invalidated.

``` json
{"accessToken":"51a4e27b-07f0-46fc-acd3-91d55ae8cf3d","expiration":1405562714483,"refreshToken":"31ea8263-9876-478b-a3a6-c81a7186528a"}
```
