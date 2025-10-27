package com.example.bankcards.security;

import com.example.bankcards.controller.AuthController;
import com.example.bankcards.controller.BankCardController;
import com.example.bankcards.controller.TransferController;
import com.example.bankcards.controller.UserController;
import com.example.bankcards.dto.*;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private BankCardService bankCardService;

    @MockBean
    private TransferService transferService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BankCardRepository bankCardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User adminUser;
    private BankCard testCard;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(User.Role.USER);

        // Setup admin user
        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@test.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(User.Role.ADMIN);

        // Setup test card
        testCard = new BankCard();
        testCard.setId(1L);
        testCard.setMaskedNumber("**** **** **** 1234");
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setStatus(BankCard.Status.ACTIVE);
        testCard.setExpiryDate(LocalDate.now().plusYears(2));
        testCard.setOwner(testUser);
    }

    @Test
    void testUnauthenticatedAccess_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_OwnCards_ShouldReturn200() throws Exception {
        when(bankCardService.findByOwner(any(User.class), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_OwnTransfers_ShouldReturn200() throws Exception {
        when(transferService.getTransferHistory(any(User.class), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/transfers"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_AdminEndpoints_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateBankCardRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_AllEndpoints_ShouldReturn200() throws Exception {
        when(userService.findAllWithPagination(any())).thenReturn(org.springframework.data.domain.Page.empty());
        when(bankCardService.findAllWithFilters(any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_OtherUserCard_ShouldReturn403() throws Exception {
        // Create a card owned by another user
        User otherUser = new User();
        otherUser.setId(999L);
        otherUser.setEmail("other@test.com");
        otherUser.setRole(User.Role.USER);

        BankCard otherCard = new BankCard();
        otherCard.setId(999L);
        otherCard.setOwner(otherUser);

        when(bankCardRepository.findById(999L)).thenReturn(Optional.of(otherCard));

        mockMvc.perform(put("/api/cards/999/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Test reason\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_OwnCard_ShouldReturn200() throws Exception {
        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardService.blockCard(anyLong(), anyString()))
                .thenReturn(BankCardDto.fromEntity(testCard));

        mockMvc.perform(put("/api/cards/1/block")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Test reason\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserTransfer_OwnCards_ShouldReturn200() throws Exception {
        BankCard fromCard = new BankCard();
        fromCard.setId(1L);
        fromCard.setOwner(testUser);
        fromCard.setBalance(BigDecimal.valueOf(1000.0));
        fromCard.setStatus(BankCard.Status.ACTIVE);

        BankCard toCard = new BankCard();
        toCard.setId(2L);
        toCard.setOwner(testUser);
        toCard.setBalance(BigDecimal.valueOf(500.0));
        toCard.setStatus(BankCard.Status.ACTIVE);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.0));
        transferRequest.setDescription("Test transfer");

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setId(1L);
        transferResponse.setFromCardId(1L);
        transferResponse.setToCardId(2L);
        transferResponse.setAmount(BigDecimal.valueOf(100.0));
        transferResponse.setDescription("Test transfer");
        transferResponse.setStatus("COMPLETED");

        when(transferService.transfer(any(TransferRequest.class), any(User.class)))
                .thenReturn(transferResponse);

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserTransfer_OtherUserCard_ShouldReturn403() throws Exception {
        // Create a card owned by another user
        User otherUser = new User();
        otherUser.setId(999L);
        otherUser.setEmail("other@test.com");
        otherUser.setRole(User.Role.USER);

        BankCard otherCard = new BankCard();
        otherCard.setId(999L);
        otherCard.setOwner(otherUser);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(999L); // Card owned by another user
        transferRequest.setAmount(BigDecimal.valueOf(100.0));
        transferRequest.setDescription("Test transfer");

        when(bankCardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(bankCardRepository.findById(999L)).thenReturn(Optional.of(otherCard));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_CreateCard_ShouldReturn200() throws Exception {
        CreateBankCardRequest createRequest = new CreateBankCardRequest();
        createRequest.setOwnerEmail("user@test.com");
        createRequest.setExpiryDate("12/26");

        BankCardDto cardDto = new BankCardDto();
        cardDto.setId(1L);
        cardDto.setMaskedNumber("**** **** **** 1234");
        cardDto.setBalance(BigDecimal.ZERO);
        cardDto.setStatus(BankCard.Status.ACTIVE);

        when(bankCardService.createCard(any(CreateBankCardRequest.class)))
                .thenReturn(cardDto);

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 1234"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_CreateCard_ShouldReturn403() throws Exception {
        CreateBankCardRequest createRequest = new CreateBankCardRequest();
        createRequest.setOwnerEmail("user@test.com");
        createRequest.setExpiryDate("12/26");

        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_UserManagement_ShouldReturn200() throws Exception {
        when(userService.findAllWithPagination(any())).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_UserManagement_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testPublicEndpoints_ShouldBeAccessible() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@test.com");
        loginRequest.setPassword("password");

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@test.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("New");
        registerRequest.setLastName("User");

        // These endpoints should be accessible without authentication
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_ExportEndpoints_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/export/cards/csv"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/export/users/pdf"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_ExportEndpoints_ShouldReturn200() throws Exception {
        // Mock the export service to return empty data
        when(bankCardService.findAllWithFilters(any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/export/cards/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));

        mockMvc.perform(get("/admin/export/cards/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUserAccess_AdminNotifications_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAdminAccess_Notifications_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk());
    }
}
