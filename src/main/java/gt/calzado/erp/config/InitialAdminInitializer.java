package gt.calzado.erp.config;

import gt.calzado.erp.domain.AppUser;
import gt.calzado.erp.domain.UserRole;
import gt.calzado.erp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements ApplicationRunner {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;
    private final String email;
    private final String fullName;

    public InitialAdminInitializer(AppUserRepository users, PasswordEncoder passwordEncoder,
                                   @Value("${app.initial-admin.username}") String username,
                                   @Value("${app.initial-admin.password}") String password,
                                   @Value("${app.initial-admin.email}") String email,
                                   @Value("${app.initial-admin.full-name}") String fullName) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.existsByUsernameIgnoreCase(username)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setRole(UserRole.ADMIN);
        users.save(admin);
    }
}