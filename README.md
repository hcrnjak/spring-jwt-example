# Spring Security with JWT

## Intro

The purpose of this project was to demonstrate how to use JWTs with Spring Security.
In order to keep things simple and make it easier to focus only on Security with JWTs this project uses : 
1. Spring Boot - general project setup
2. H2 - in-memory database
3. Spring Data JPA - repositories through which we can access data in DB
4. Spring Security

H2 **web console** is enabled on path `/h2`.
Through this console you can see the contents of the database and change them as you wish.

## Implementing Spring security with JWT - Step-by-step

In my research I found a couple of Github projects which demonstrated how to incorporate JWT into Spring Security, but in my opinion they were too cumbersome and they lacked explanations on how/why some of the things were implemented.

So I set out to do my own implementation with proper explanations of each critical section of the code.
You can find **step-by-step** explanations below: 


### 1) Add dependencies for JJWT (Java JWT build/verification tool) + Spring Security

JJWT is a small library used for creating and validating signed JWTs.

```
	<!--    Security setup  -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!--    JWT Token handler   -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>0.7.0</version>
        </dependency>
```

### 2) Define your own Security configuration class

This class is needed to configure Spring Security with JWTs.

```
	@Configuration
	@EnableWebSecurity
	public class SecurityConfig extends WebSecurityConfigurerAdapter
```

### 3) Implement custom AuthenticationEntryPoint class 

This class is invoked when **unauthenticated** user tries to access secured resource.
By default, this class returns either */login* form or some other response depending on the authentication type configured.
In our case it will just return **HTTP 401**
	
1. Implement custom `AuthenticationEntryPoint`	
	```
	@Component
	public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint 
	```
There's only one method to be implemented.
	
		
2. Register `JwtAuthenticationEntryPoint` in custom security config file
	```
	@Autowired
	private JwtAuthenticationEntryPoint unauthenticatedHandler;
	```	

### 4) Implement custom UserDetailsService interface

This service is responsible for retrieveing user information based on User's username (roles, etc.).
AuthenticationProviders use UserDetailsService to get the data about user trying to authenticate.
By default it usually contacts DB (or it can be an in-memory one).

1. Implement custom `UserDetailsService` 	
	```
	@Service
	public class JwtUserDetailsServiceImpl implements UserDetailsService
	```
2. The method returns instance of the `UserDetails` interface.

This interface holds User details and Authorities (roles).
You can put additional data into the instance of the UserDetails interface (like email etc.)
This was done in the project through custom `AuthenticatedUser` class which implements the interface.
	
3.  Register custom `UserDetailsService` in custom security config. file
	```
	@Autowired
	private JwtUserDetailsService userDetailsService;
	```

### 5) Configure authentication mechanisms (which UserDetailsService is used etc.)

**IMPORTANT** : This AuthenticationManager will ONLY be used when User is requesting JWT with username/password.
		For JWT authentication/validation we'll bypass the manager.

```
	@Autowired
    	public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        	authenticationManagerBuilder
                	.userDetailsService(userDetailsService)
                	.passwordEncoder(new BCryptPasswordEncoder());
    	}
```
	
### 6) Define custom filter 

This filter will read JWT token and validate it. 
If token is valid, it will populate `SecurityContextHolder` with Authentication info
The filter will be placed after `LogoutFilter` in the fitler chain

**NOTE** : you can see where to insert filter in the filter chain by observing SpringSecurity logs when for example form login auth. type is being used. The idea is to place your own filter where form-login's filter is usually present.

	```
	public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	....
	
	// If JWT valid, populate SecurityContext with the data
	UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	SecurityContextHolder.getContext().setAuthentication(authentication);
	```

**IMPORTANT** : you can see in the last line of code that we are setting the security context (we're setting authenticated user info)
**ALSO** : This filter is a little bit of a "hack". Not in a real sense of the word, but still...

If we wanted to implement a "real" authenticattion filter, we would have to extend `AbstractAuthenticationProcessingFilter` class.
This class works by retrieving the authentication info from the request (like username/password or token) and pushing that info 	down to AuthenticationManager.
Manager together with its AuthenticationProviders is in charge of authenticating the data and setting the SecurityContext 	(authenticated user's info)
		
Here we're **bypassing** AuthenticationManager and just setting the SecurityContext ourselves
	
	
### 7) Configure security rules for your resources (which paths are secured, roles, etc.)

No CSRF, no Session, special unauthenticatedHandler, custom JWT token filter

```
	@Override
    	protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthenticatedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js"
                ).permitAll()
                .antMatchers("/auth/**").permitAll()
                .anyRequest().authenticated();

        // Custom JWT based security filter
        httpSecurity.addFilterAfter(authenticationTokenFilterBean(), LogoutFilter.class);

        // Disable page caching
        httpSecurity.headers().cacheControl();
    }
```	

### 9) In order to actually get the JWT, user needs to authenticate via username/password

This is done by invoking the AuthenticationManager explicitely via the following block of code.
This is done in the @Controller servicing `/auth/` path.

```
	@Autowired
    private AuthenticationManager authenticationManager;

    public void authenticateUser(AuthenticationRequest authenticationRequest) {
        // Authenticate User
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
```	

**NOTE** : AuthenticationRequest is just our Java class (nothing special or related to Spring). We use it to capture User data 
that comes to us in JSON format.

And also, SecurityContext is being set explicitely, which might not be the best practice
In case authentication fails, Manager will call AuthenticationEntryPoint (defined in step 3), which will just return 401.

## Final considerations

The code presented here shows one way of implementing support for JWT  tokens.
The main idea behind this approach is to basically bypass the authentication part of the Spring Security filter chain.
We add our own JwtAuthenticationTokenFilter which checks for presence of JWT token and if the token is there it just populates SecurityConfig and that's it.
User authenticated, AuthenticationManager bypassed completely and off we go to the authorization part.

In this case AuthenticationManager is used only explicitely (outside the filter chain) to authenticate users when they try to acquire JWT.
We just call AuthenticationManager and that's it.

This approach is kind of a hack because it doesn't use AuthenticationManager properly, but on the other side it supports both JWTs and authentication via username/password.


The other possible approach is to implement/configure our own AuthenticationManager to properly support JWTs.
This means setting up JwtAuthenticationTokenFilter differently (different class would be extended, the one that uses AuthenticationManager) + custom AuthenticationProvider +  custom success handler
And in the end we wouldn't be able to use (at least not just like that) AuthenticationManager to authenticate our user's username/password when he wants to get JWT.

This code is not perfect, but it's a good start for someone who wants to understand how to use JWTs in their app.
Hell, I wish I had something like this when I started. :)

Happy coding!

