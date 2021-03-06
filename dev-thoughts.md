# Dev Thoughts

## CORS
Always a pleasure and everything you think you know how it works something new happens.

**Browsers**   
Browsers want proper CORS handling. In a standard configuration they expect CORS to be working. Preflight via OPTIONS wants to be supported, too.

Rule of thumb: Browser expect an `Access-Control-Allow-Origin` in the response header. This is generated by the backend (this tool). We define some global configuration in `WebConfig`.

Through `WebConfig` we only allow those domains configured via application.yml
```
istint:
  cors:
    allowedOrigins: 
      - 'http://host.docker.internal:3000'
      - 'http://localhost:3000'
    allowCredentials: false  
```

Different parts of the application require different configuration actions.
Consider this two requests:

```
GET   http://host.docker.internal:8080/hello
Origin: http://bla.de
```
The `/hello` endpoint is just a dummy and a standard `@RestController` (no other annotations). CORS is now checking the `Origin` value against the allowedOrigins and simply denies the request since http://bla.de is not in that list.



The `/raceevents` endpoint is generated by SpringData. SpringData by default does no CORS handling and therefore may make the browser very unhappy because of the missing `Access-Control-Allow-Origin` header attribute in the response.
We have to manually add a `@CrossOrigin` at the repository or controller to make that work. But be aware: These annotations are merged with the global CORS config. By adding `@CrossOrigin` (without further properties) to the repository we would add all origins ('*') to whatever endpoint is generated by SpringData for that source location.

Given the following code snippet
```
@RestResource(path = "raceevents")
@CrossOrigin
public interface RaceEventRepository extends MongoRepository<RaceEvent, String> { ... }
```
the request 

```
GET  http://host.docker.internal:8080/raceevents
Origin: http://bla.de
```

will succeed with the following response headers
```
HTTP/1.1 200 
Vary: Origin, Access-Control-Request-Method, Access-Control-Request-Headers
Access-Control-Allow-Origin: *
```




## Security

For this backend we decided to use Keycloak for AuthN. AuthZ is still to be defined.

## Discussion 
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



