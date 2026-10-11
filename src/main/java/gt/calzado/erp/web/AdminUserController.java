package gt.calzado.erp.web;

import gt.calzado.erp.domain.AppUser;
import gt.calzado.erp.domain.UserRole;
import gt.calzado.erp.repository.AppUserRepository;
import gt.calzado.erp.web.form.StaffUserForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminUserController {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AdminUserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/usuarios")
    public String list(Model model) {
        model.addAttribute("users", users.findAllByOrderByFullNameAsc());
        return "admin/users";
    }

    @GetMapping("/admin/usuarios/nuevo")
    public String createForm(Model model) {
        model.addAttribute("userForm", new StaffUserForm());
        model.addAttribute("formTitle", "Nuevo usuario");
        model.addAttribute("formAction", "/admin/usuarios/nuevo");
        model.addAttribute("roles", staffRoles());
        return "admin/user-form";
    }

    @PostMapping("/admin/usuarios/nuevo")
    public String create(@Valid @ModelAttribute("userForm") StaffUserForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        rejectDuplicateFields(form, result, null);
        if (form.getPassword() == null || form.getPassword().isBlank() || form.getPassword().length() < 8) {
            result.rejectValue("password", "invalid", "La contraseña inicial debe tener al menos 8 caracteres.");
        }
        if (result.hasErrors()) {
            setFormModel(model, "Nuevo usuario", "/admin/usuarios/nuevo");
            return "admin/user-form";
        }
        AppUser user = new AppUser();
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setFullName(form.getFullName().trim());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(form.getRole());
        users.save(user);
        redirect.addFlashAttribute("notice", "Usuario creado.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/usuarios/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        AppUser user = getUser(id);
        StaffUserForm form = new StaffUserForm();
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setFullName(user.getFullName());
        form.setRole(user.getRole());
        model.addAttribute("userForm", form);
        setFormModel(model, "Editar usuario", "/admin/usuarios/" + id + "/editar");
        model.addAttribute("userId", id);
        return "admin/user-form";
    }

    @PostMapping("/admin/usuarios/{id}/editar")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("userForm") StaffUserForm form,
                         BindingResult result, Model model, Authentication authentication,
                         RedirectAttributes redirect) {
        AppUser user = getUser(id);
        rejectDuplicateFields(form, result, id);
        if (form.getPassword() != null && !form.getPassword().isBlank() && form.getPassword().length() < 8) {
            result.rejectValue("password", "invalid", "La contraseña debe tener al menos 8 caracteres.");
        }
        if (authentication.getName().equalsIgnoreCase(user.getUsername()) && form.getRole() != UserRole.ADMIN) {
            result.rejectValue("role", "invalid", "No puedes quitarte tu propio rol de administrador.");
        }
        if (result.hasErrors()) {
            setFormModel(model, "Editar usuario", "/admin/usuarios/" + id + "/editar");
            model.addAttribute("userId", id);
            return "admin/user-form";
        }
        user.setUsername(form.getUsername().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setFullName(form.getFullName().trim());
        user.setRole(form.getRole());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        users.save(user);
        redirect.addFlashAttribute("notice", "Cambios guardados.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/desactivar")
    public String deactivate(@PathVariable Long id, Authentication authentication,
                             RedirectAttributes redirect) {
        AppUser user = getUser(id);
        if (authentication.getName().equalsIgnoreCase(user.getUsername())) {
            redirect.addFlashAttribute("error", "No puedes desactivar tu propia cuenta.");
            return "redirect:/admin/usuarios";
        }
        if (user.isEnabled() && user.getRole() == UserRole.ADMIN
                && users.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1) {
            redirect.addFlashAttribute("error", "Debe permanecer al menos un administrador activo.");
            return "redirect:/admin/usuarios";
        }
        user.setEnabled(false);
        users.save(user);
        redirect.addFlashAttribute("notice", "Usuario desactivado.");
        return "redirect:/admin/usuarios";
    }

    private void rejectDuplicateFields(StaffUserForm form, BindingResult result, Long exceptId) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        String email = form.getEmail() == null ? "" : form.getEmail().trim();
        boolean duplicateUsername = exceptId == null
                ? users.existsByUsernameIgnoreCase(username)
                : users.existsByUsernameIgnoreCaseAndIdNot(username, exceptId);
        boolean duplicateEmail = exceptId == null
                ? users.existsByEmailIgnoreCase(email)
                : users.existsByEmailIgnoreCaseAndIdNot(email, exceptId);
        if (duplicateUsername) {
            result.rejectValue("username", "duplicate", "Ese nombre de usuario ya existe.");
        }
        if (duplicateEmail) {
            result.rejectValue("email", "duplicate", "Ese correo ya está registrado.");
        }
    }

    private void setFormModel(Model model, String title, String action) {
        model.addAttribute("formTitle", title);
        model.addAttribute("formAction", action);
        model.addAttribute("roles", staffRoles());
    }

    private UserRole[] staffRoles() {
        return new UserRole[]{UserRole.ADMIN, UserRole.SALES, UserRole.WAREHOUSE,
                UserRole.SUPPORT, UserRole.ANALYST};
    }

    private AppUser getUser(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}