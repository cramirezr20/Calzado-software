package gt.calzado.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain applicationSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/tienda/**", "/css/**", "/img/**", "/login").permitAll()
                .requestMatchers("/admin/usuarios", "/admin/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/admin/reclamos", "/admin/reclamos/**").hasAnyRole("ADMIN", "SUPPORT")
                .requestMatchers("/admin/pedidos", "/admin/pedidos/**")
                .hasAnyRole("ADMIN", "SALES", "WAREHOUSE", "ANALYST")
                .requestMatchers("/admin/clientes", "/admin/clientes/**")
                .hasAnyRole("ADMIN", "SALES", "SUPPORT", "ANALYST")
                .requestMatchers("/admin/productos", "/admin/productos/**")
                .hasAnyRole("ADMIN", "WAREHOUSE")
                .requestMatchers("/admin", "/admin/").hasAnyRole("ADMIN", "ANALYST")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll())
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/tienda"));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}