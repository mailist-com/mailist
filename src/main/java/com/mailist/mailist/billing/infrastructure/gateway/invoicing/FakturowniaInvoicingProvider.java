package com.mailist.mailist.billing.infrastructure.gateway.invoicing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mailist.mailist.billing.domain.gateway.InvoicingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fakturownia invoicing provider implementation
 * Handles all Fakturownia API interactions for invoice management
 */
@Component
@Slf4j
public class FakturowniaInvoicingProvider implements InvoicingProvider {

    @Value("${billing.fakturownia.api-token}")
    private String apiToken;

    @Value("${billing.fakturownia.account-name}")
    private String accountName;

    @Value("${billing.fakturownia.api-url:https://ACCOUNT.fakturownia.pl}")
    private String apiUrlTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FakturowniaInvoicingProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return "fakturownia";
    }

    @Override
    public InvoiceCreationResponse createInvoice(InvoiceCreationRequest request) {
        try {
            String url = buildApiUrl("/invoices.json");

            // Build invoice payload
            Map<String, Object> invoice = new HashMap<>();
            invoice.put("kind", mapInvoiceType(request.invoiceType()));
            invoice.put("issue_date", request.issueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            invoice.put("sell_date", request.issueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            invoice.put("payment_to", request.dueDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            invoice.put("buyer_name", request.buyer().name());
            invoice.put("buyer_email", request.buyer().email());

            if (request.buyer().taxId() != null && !request.buyer().taxId().isEmpty()) {
                invoice.put("buyer_tax_no", request.buyer().taxId());
            }

            // Build address
            String address = buildAddress(request.buyer());
            if (!address.isEmpty()) {
                invoice.put("buyer_street", address);
                invoice.put("buyer_city", request.buyer().city());
                invoice.put("buyer_post_code", request.buyer().postalCode());
                invoice.put("buyer_country", request.buyer().country());
            }

            // Add invoice items
            invoice.put("positions", request.items().stream()
                .map(this::mapInvoiceItem)
                .collect(Collectors.toList()));

            // Add metadata
            if (request.metadata() != null && !request.metadata().isEmpty()) {
                invoice.put("description", request.metadata().getOrDefault("description", ""));
                invoice.put("oid", request.metadata().getOrDefault("order_id", ""));
            }

            Map<String, Object> payload = Map.of("api_token", apiToken, "invoice", invoice);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String invoiceId = String.valueOf(responseBody.get("id"));
                String invoiceNumber = (String) responseBody.get("number");
                String pdfUrl = (String) responseBody.get("pdf_url");
                String invoiceUrl = (String) responseBody.get("view_url");

                log.info("Created Fakturownia invoice: {} ({})", invoiceNumber, invoiceId);

                return new InvoiceCreationResponse(
                    true,
                    invoiceId,
                    invoiceNumber,
                    pdfUrl,
                    invoiceUrl,
                    null
                );
            } else {
                log.error("Failed to create Fakturownia invoice: {}", response.getStatusCode());
                return new InvoiceCreationResponse(
                    false,
                    null,
                    null,
                    null,
                    null,
                    "Failed to create invoice: " + response.getStatusCode()
                );
            }

        } catch (Exception e) {
            log.error("Error creating Fakturownia invoice", e);
            return new InvoiceCreationResponse(
                false,
                null,
                null,
                null,
                null,
                e.getMessage()
            );
        }
    }

    @Override
    public InvoiceDetailsResponse getInvoice(String externalInvoiceId) {
        try {
            String url = buildApiUrl("/invoices/" + externalInvoiceId + ".json?api_token=" + apiToken);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> invoice = response.getBody();

                String invoiceId = String.valueOf(invoice.get("id"));
                String invoiceNumber = (String) invoice.get("number");
                String status = mapFakturowniaStatus((String) invoice.get("status"));
                BigDecimal totalAmount = new BigDecimal(invoice.get("total_price_gross").toString());
                String currency = (String) invoice.get("currency");

                LocalDate issueDate = LocalDate.parse((String) invoice.get("issue_date"));
                LocalDate dueDate = LocalDate.parse((String) invoice.get("payment_to"));
                LocalDate paidDate = invoice.get("paid_date") != null ?
                    LocalDate.parse((String) invoice.get("paid_date")) : null;

                String pdfUrl = (String) invoice.get("pdf_url");

                return new InvoiceDetailsResponse(
                    invoiceId,
                    invoiceNumber,
                    status,
                    totalAmount,
                    currency,
                    issueDate,
                    dueDate,
                    paidDate,
                    pdfUrl
                );
            } else {
                log.error("Failed to get Fakturownia invoice: {}", externalInvoiceId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error getting Fakturownia invoice: {}", externalInvoiceId, e);
            return null;
        }
    }

    @Override
    public boolean markInvoiceAsPaid(String externalInvoiceId, LocalDate paidDate) {
        try {
            String url = buildApiUrl("/invoices/" + externalInvoiceId + "/change_paid_status.json");

            Map<String, Object> payload = Map.of(
                "api_token", apiToken,
                "paid", true,
                "paid_date", paidDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Marked Fakturownia invoice as paid: {}", externalInvoiceId);
            }
            return success;

        } catch (Exception e) {
            log.error("Error marking Fakturownia invoice as paid: {}", externalInvoiceId, e);
            return false;
        }
    }

    @Override
    public boolean cancelInvoice(String externalInvoiceId) {
        try {
            String url = buildApiUrl("/invoices/" + externalInvoiceId + ".json");

            Map<String, Object> invoice = Map.of("status", "cancelled");
            Map<String, Object> payload = Map.of(
                "api_token", apiToken,
                "invoice", invoice
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Cancelled Fakturownia invoice: {}", externalInvoiceId);
            }
            return success;

        } catch (Exception e) {
            log.error("Error cancelling Fakturownia invoice: {}", externalInvoiceId, e);
            return false;
        }
    }

    @Override
    public boolean sendInvoiceByEmail(String externalInvoiceId) {
        try {
            String url = buildApiUrl("/invoices/" + externalInvoiceId + "/send_by_email.json");

            Map<String, Object> payload = Map.of("api_token", apiToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                log.info("Sent Fakturownia invoice by email: {}", externalInvoiceId);
            }
            return success;

        } catch (Exception e) {
            log.error("Error sending Fakturownia invoice by email: {}", externalInvoiceId, e);
            return false;
        }
    }

    @Override
    public String getInvoicePdfUrl(String externalInvoiceId) {
        InvoiceDetailsResponse invoice = getInvoice(externalInvoiceId);
        return invoice != null ? invoice.pdfUrl() : null;
    }

    @Override
    public String createOrUpdateCustomer(CustomerCreationRequest request) {
        try {
            String url = buildApiUrl("/clients.json");

            Map<String, Object> client = new HashMap<>();
            client.put("name", request.name());
            client.put("email", request.email());

            if (request.taxId() != null && !request.taxId().isEmpty()) {
                client.put("tax_no", request.taxId());
            }

            if (request.street() != null) {
                client.put("street", request.street());
                client.put("city", request.city());
                client.put("post_code", request.postalCode());
                client.put("country", request.country());
            }

            Map<String, Object> payload = Map.of(
                "api_token", apiToken,
                "client", client
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String clientId = String.valueOf(response.getBody().get("id"));
                log.info("Created/updated Fakturownia client: {}", clientId);
                return clientId;
            }

            return null;

        } catch (Exception e) {
            log.error("Error creating/updating Fakturownia customer", e);
            return null;
        }
    }

    /**
     * Build API URL with account name
     */
    private String buildApiUrl(String endpoint) {
        String baseUrl = apiUrlTemplate.replace("ACCOUNT", accountName);
        return baseUrl + endpoint;
    }

    /**
     * Map invoice type to Fakturownia kind
     */
    private String mapInvoiceType(String invoiceType) {
        return switch (invoiceType.toLowerCase()) {
            case "proforma" -> "proforma";
            case "vat" -> "vat";
            case "receipt" -> "receipt";
            case "bill" -> "bill";
            default -> "vat";
        };
    }

    /**
     * Map Fakturownia status to our status
     */
    private String mapFakturowniaStatus(String status) {
        return switch (status) {
            case "issued" -> "SENT";
            case "sent" -> "SENT";
            case "paid" -> "PAID";
            case "partial" -> "PARTIALLY_PAID";
            case "rejected" -> "CANCELLED";
            default -> "OPEN";
        };
    }

    /**
     * Map invoice item to Fakturownia position
     */
    private Map<String, Object> mapInvoiceItem(InvoiceItem item) {
        Map<String, Object> position = new HashMap<>();
        position.put("name", item.name());
        position.put("description", item.description());
        position.put("quantity", item.quantity());
        position.put("total_price_gross", item.totalPrice());
        position.put("tax", item.taxRate().multiply(BigDecimal.valueOf(100)).intValue());

        return position;
    }

    /**
     * Build address string
     */
    private String buildAddress(BuyerInfo buyer) {
        if (buyer.street() != null && !buyer.street().isEmpty()) {
            return buyer.street();
        }
        return "";
    }
}
