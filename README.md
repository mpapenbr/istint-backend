# istint-backend
Backend for istint

## Security

### What Spring Security does (OAuth2)

At first sight it looks you don't have to do much by yourself. Just provide the `client-id` und `client-secret` for your selected providers and let that magic happen.

The default magic works like this (example based on google)
- Client calls /oauth2/authorization/google
- The <enterClassHere> will issue a request to https://accounts.google.com/o/oauth2/v2/auth
  - response_type=code
  - client_id=....
  - scope: openid,profile,email
  - redirect_uri: http://localhost:8080/login/oauth2/code/google
  - nounce=...
  - state=...
- first time: 
  Also called the client registration process.  
  This is when the user has to grant access to requested scope data. if he does, we get a code. 
- next time: due to authorized client-id google returns the code directly
- code is sent to /login/oauth2/code/google
- RequestAwareAuthenticationSuccessHandler redirects "somehow" to "/" (which is some default)
  - open: how and where and when to configure this?

### What our backend does
We provide 2 different kind of logins
- a direct login where we store the user credentials in our database
- we act as a OAuth2 client and request profile data from some OAuth2 provider, e.g. google, facebook, github.

  We do this to identify the user. The user name, email and some identifier specific to the OAuth2 provider is stored in our database and gets its own user id. This function is supposed to be used by users who don't want to have a username/password on every system they want to use.

So our internal user info looks similar to this
```
class User {
  UUID id;
  String name;
  String email;
  String oauth2Provider; // (internal,google,facebook,github)
  String providerId; // id of user at the oauth2Provider
  String username; // when oauth2Provider == internal
  String password; // SHA-512 hash
```

### Login process

The base idea is to work with Json Web Token (JWT). 

- manual login  
  user gets an JWT in exchange for his credentials.  
- social login  
  user needs to be logged in at his favorite OAuth2 provider. If he is already logged in we get his profile information. If we already know him (identified by the tuple (oauth2Provider,providerId)), everything is fine and we provide  the user data via `UserDetails`.  
  If he is unkown we create a new entry and return it via `UserDetails`

At the end of the login process we create a JWT token which is used for authentication of the further requests.

### Discussion 
_Do we need JWT?_
There is some discussion about JWT. What is the additional benefit? Why not simply use a token which is put into some cookie. [Here][3] and [here][4] is some discussion on what not to do with JWT. Another [blog][2] describes how JWT should be stored in the client (browser). 

At the end of the day we need to answer the question: What are our needs?  
As of now we just need to identify the user. We need some good reason why not using some established mechanism. For example: why not using a HttpOnly-Cookie where we put some user identifier?

# Links
## JWT 
https://hasura.io/blog/best-practices-of-using-jwt-with-graphql/

When we need to verify the signature created with asymetric keys there is convention on how to provide the public key for verifying the signature.  
See [here][1]


[1]:https://auth0.com/blog/navigating-rs256-and-jwks/
[2]:https://hasura.io/blog/best-practices-of-using-jwt-with-graphql/
[3]:http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/
[4]:http://cryto.net/~joepie91/blog/2016/06/19/stop-using-jwt-for-sessions-part-2-why-your-solution-doesnt-work/



