package gt.calzado.erp.web;

import gt.calzado.erp.domain.Complaint;
import gt.calzado.erp.domain.ComplaintStatus;
import gt.calzado.erp.repository.ComplaintRepository;
import gt.calzado.erp.web.form.ComplaintForm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ComplaintController {

    private final ComplaintRepository complaints;

    public ComplaintController(ComplaintRepository complaints) {
        this.complaints = complaints;
    }

    @GetMapping("/reclamos/nuevo")
    public String form(Model model) {
        model.addAttribute("complaintForm", new ComplaintForm());
        return "storefront/complaint-form";
    }

    @PostMapping("/reclamos")
    public String submit(@Valid @ModelAttribute("complaintForm") ComplaintForm form,
                         BindingResult result, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "storefront/complaint-form";
        }
        Complaint complaint = new Complaint();
        complaint.setOrderNumber(form.getOrderNumber() == null ? null : form.getOrderNumber().trim());
        complaint.setCustomerName(form.getCustomerName().trim());
        complaint.setCustomerEmail(form.getCustomerEmail().trim().toLowerCase());
        complaint.setMessage(form.getMessage().trim());
        complaints.save(complaint);
        redirect.addFlashAttribute("notice", "Recibimos tu solicitud. Nuestro equipo dará seguimiento.");
        return "redirect:/reclamos/nuevo";
    }

    @GetMapping("/admin/reclamos")
    public String list(Model model) {
        model.addAttribute("complaints", complaints.findAllByOrderByCreatedAtDesc());
        model.addAttribute("statuses", ComplaintStatus.values());
        return "admin/complaints";
    }

    @PostMapping("/admin/reclamos/{id}/estado")
    public String updateStatus(@PathVariable Long id, @RequestParam ComplaintStatus status,
                               RedirectAttributes redirect) {
        Complaint complaint = complaints.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        complaint.setStatus(status);
        complaints.save(complaint);
        redirect.addFlashAttribute("notice", "Estado del reclamo actualizado.");
        return "redirect:/admin/reclamos";
    }
}