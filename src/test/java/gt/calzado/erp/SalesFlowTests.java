package gt.calzado.erp;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gt.calzado.erp.domain.Complaint;
import gt.calzado.erp.domain.Product;
import gt.calzado.erp.domain.UserRole;
import gt.calzado.erp.repository.AppUserRepository;
import gt.calzado.erp.repository.ComplaintRepository;
import gt.calzado.erp.repository.CustomerRepository;
import gt.calzado.erp.repository.ProductRepository;
import gt.calzado.erp.repository.SalesOrderRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = "app.initial-admin.password=Usuario1")
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class SalesFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository products;

    @Autowired
    private SalesOrderRepository orders;

    @Autowired
    private CustomerRepository customers;

    @Autowired
    private ComplaintRepository complaints;
        @Autowired
        private AppUserRepository appUsers;

        @Autowired
        private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearData() {
        orders.deleteAll();
        complaints.deleteAll();
        customers.deleteAll();
        products.deleteAll();
    }

        @Test
        void initialAdminIsStoredWithEncodedPasswordAndCanOpenUserManagement() throws Exception {
                var admin = appUsers.findByUsernameIgnoreCase("user1").orElseThrow();
                assertEquals(UserRole.ADMIN, admin.getRole());
                assertNotEquals("Usuario1", admin.getPassword());
                assertTrue(passwordEncoder.matches("Usuario1", admin.getPassword()));

                MvcResult login = mockMvc.perform(post("/login")
                                                .param("username", "user1")
                                                .param("password", "Usuario1")
                                                .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andReturn();
                MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
                assertNotNull(session);

                mockMvc.perform(get("/admin/usuarios").session(session))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("user1@calzado.local")));
        }

                    @Test
                    void adminCanCreateStaffAndSupportRoleCannotManageInventory() throws Exception {
                        mockMvc.perform(post("/admin/usuarios/nuevo")
                                        .with(user("user1").roles("ADMIN"))
                                        .param("username", "support-test")
                                        .param("email", "support-test@calzado.local")
                                        .param("fullName", "Soporte de prueba")
                                        .param("password", "SupportPass9")
                                        .param("role", "SUPPORT")
                                        .with(csrf()))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/admin/usuarios"));

                        var support = appUsers.findByUsernameIgnoreCase("support-test").orElseThrow();
                        assertTrue(passwordEncoder.matches("SupportPass9", support.getPassword()));
                        try {
                            mockMvc.perform(get("/admin/reclamos").with(user("support-test").roles("SUPPORT")))
                                    .andExpect(status().isOk());
                            mockMvc.perform(get("/admin/productos").with(user("support-test").roles("SUPPORT")))
                                    .andExpect(status().isForbidden());
                            mockMvc.perform(get("/admin/usuarios").with(user("support-test").roles("SUPPORT")))
                                    .andExpect(status().isForbidden());
                        } finally {
                            appUsers.delete(support);
                        }
                    }

    @Test
    @Transactional
    void storefrontOrderUpdatesStockAndAdminCanResolveFlows() throws Exception {
        Product product = new Product();
        product.setSku("TEST-URBANO");
        product.setName("Tenis urbano de prueba");
        product.setCategory("Tenis");
        product.setPrice(new BigDecimal("120.00"));
        product.setStock(5);
        product = products.saveAndFlush(product);

        mockMvc.perform(get("/tienda").param("q", "urbano"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tenis urbano de prueba")))
                .andExpect(content().string(containsString("value=\"Tenis\"")));

        mockMvc.perform(get("/tienda/productos/{id}", product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tenis urbano de prueba")));

        MvcResult cartResult = mockMvc.perform(get("/carrito"))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession session = (MockHttpSession) cartResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(post("/carrito/agregar/{id}", product.getId())
                        .session(session)
                        .param("quantity", "2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/carrito"));

        mockMvc.perform(get("/carrito").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tenis urbano de prueba")));

        mockMvc.perform(post("/checkout")
                        .session(session)
                        .param("fullName", "Cliente de prueba")
                        .param("email", "cliente@example.test")
                        .param("phone", "5555-0101")
                        .param("shippingAddress", "Zona 1, Ciudad de Guatemala")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        var order = orders.findAllByOrderByCreatedAtDesc().getFirst();
        assertEquals(1, customers.count());
        assertEquals(3, products.findById(product.getId()).orElseThrow().getStock());

        mockMvc.perform(get("/pedido/confirmacion/{orderNumber}", order.getOrderNumber()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(order.getOrderNumber())));

        mockMvc.perform(post("/admin/pedidos/{id}/estado", order.getId())
                        .with(user("user1").roles("ADMIN"))
                        .param("status", "CANCELLED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos"));
        assertEquals(5, products.findById(product.getId()).orElseThrow().getStock());

        mockMvc.perform(post("/reclamos")
                        .param("customerName", "Cliente de prueba")
                        .param("customerEmail", "cliente@example.test")
                        .param("orderNumber", order.getOrderNumber())
                        .param("message", "Consulta de prueba")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reclamos/nuevo"));
        assertEquals(1, complaints.count());

        Complaint complaint = complaints.findAllByOrderByCreatedAtDesc().getFirst();
        mockMvc.perform(post("/admin/reclamos/{id}/estado", complaint.getId())
                        .with(user("user1").roles("ADMIN"))
                        .param("status", "RESOLVED")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reclamos"));
    }
}