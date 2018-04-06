package eu.kielczewski.example.config;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	
    	String[] patterns = new String[] {
    	        "/",
    	        "/login",
    	  //      "/bower_components/**/*",
    	   //     "/app/**/*",
    	     //   "/index.html",
    	      //  "/home.html"
    	    };
    	
    	 http.authorizeRequests()
         .antMatchers("/", "/public/**").permitAll()
         .antMatchers("/login").permitAll()
         .antMatchers("/users/**").hasAuthority("ADMIN")
         .anyRequest().authenticated()
         .and().csrf()
         .csrfTokenRepository(csrfTokenRepository())
         .and() //.disable()
         .formLogin()
         .loginPage("/login")
         .failureUrl("/login?error")
         .usernameParameter("email")
         .passwordParameter("password")
         .permitAll()
         .and()
         .addFilterAfter(csrfFilter(patterns), FilterSecurityInterceptor.class)
         .addFilterAfter(new CsrfGrantingFilter(), CsrfFilter.class)
        // .addFilterAfter(new CsrfGrantingFilter(), SessionManagementFilter.class)
         .logout()
         .logoutUrl("/logout")
         .deleteCookies("remember-me")
         .logoutSuccessUrl("/")
         .permitAll()
         .and()
         .rememberMe();
    }
    
   
    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
      }
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }
    
    private Filter csrfFilter(String[] patterns) {
        CsrfFilter csrfFilter = new CsrfFilter(csrfTokenRepository());
        csrfFilter.setRequireCsrfProtectionMatcher(csrfProtectionMatcher(patterns));
        return csrfFilter;
      }

      private NoAntPathRequestMatcher csrfProtectionMatcher(String[] patterns) {
        return new NoAntPathRequestMatcher(patterns);
      }

}