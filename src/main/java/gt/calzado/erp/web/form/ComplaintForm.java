package gt.calzado.erp.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ComplaintForm {

    @Size(max = 24)
    private String orderNumber;

    @NotBlank
    @Size(max = 120)
    private String customerName;

    @NotBlank
    @Email
    @Size(max = 120)
    private String customerEmail;

    @NotBlank
    @Size(max = 2000)
    private String message;

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}