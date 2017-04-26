## Spring Security with JWT

1) Add dependencies for JJWT (Java JWT build/verification tool) + Spring Security

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
		
2) Define your own Security configuration class

	@Configuration
	@EnableWebSecurity
	public class SecurityConfig extends WebSecurityConfigurerAdapter
	
3) Implement custom AuthenticationEntryPoint class 
	- this class is invoked when unauthenticated user tries to access secured resource
	- by default, this class return either /login form or some other response depending on the authentication type configured
	
	a) @Component
		public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint 
		- one method needs to be implemented
		
	b) register AuthenticationEntryPoint in config. file
	
		@Autowired
		private JwtAuthenticationEntryPoint unauthenticatedHandler;
		
4) Implement custom UserDetailsService interface

	a) 	@Service
		public class JwtUserDetailsServiceImpl implements UserDetailsService
		
	b) the method returns an instance of the UserDetails inteface
	- this interface holds User details and Authorities
	- you can put additional data into the instance of the UserDetails interface (like email etc.)
	- AuthenticationProviders use UserDetailsService to get the data about user trying to authenticate
	
	c) register UserDetailsService in config. file
	
	    @Autowired
		private JwtUserDetailsService userDetailsService;
	
5) Configure authentication mechanisms (which UserDetailsService is used etc.)

	@Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }
	
	NOTE : this AuthenticationManager will ONLY be used when User is requesting JWT with username/password.
		For JWT authentication/validation we'll bypass the manager (which is probably a hack :)
	
6) Define custom filter. This filter will read JWT token and validate it. If token is valid, it will populate SecurityContextHolder with Authentication info
	- thiw filter will be placed before UsernamePasswordAuthenticationFilter in the fitler chain
	
	public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
	....
	
	// If JWT valid, populate SecurityContext with the data
	UsernamePasswordAuthenticationToken
			authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	SecurityContextHolder.getContext().setAuthentication(authentication);
	
	}

 	IMPORTANT : you can see in the last line of code that we are setting the security context (we're setting authenticated user info)
	ALSO : This filter is a little bit of a "hack". Not in a real sense of the word, but still...
		If we wanted to implement a "real" authenticattion filter, we would have to extend AbstractAuthenticationProcessingFilter class.
		This class works by retrieving the authentication info from the request (like username/password or token) and pushing that info down to AuthenticationManager.
		Manager together with it's AuthenticationProviders is in charge of authenticating the data and setting the SecurityContext (authenticated user's info)
		
		Here we're bypassing AuthenticationManager and just setting the SecurityContext ourselves
	
	
7) Configure security rules for your resources (which paths are secured, roles, etc.)
	- no CSRF, no Session, special unauthenticatedHandler, custom JWT token filter

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
	
	- NOTE : you can see where to insert filter in the filter chain by observing SpringSecurity logs when for example
		form login auth. type is being used. The idea is to place your own filter where form-login's filter is usually present 

9) In order to actually get the JWT, user needs to authenticate via username/password.
	This is done by invoking the AuthenticationManager explicitely via this block of code : 
	
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
	
	NOTE : AuthenticationRequest is just our Java class (nothing special or related to Spring)
		And also, SecurityContext is being set explicitely, which might not be the best practice
	
	In case authentication fails, Manager will call AuthenticationEntryPoint (which is our bean again), and we'll just return 401
