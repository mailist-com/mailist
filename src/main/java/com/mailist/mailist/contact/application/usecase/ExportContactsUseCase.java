package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.valueobject.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ExportContactsUseCase {

    private final ContactListRepository contactListRepository;

    public String execute(ExportContactsCommand command) {
        log.info("Exporting contacts from list ID: {}", command.getListId());

        ContactList contactList = contactListRepository.findById(command.getListId())
                .orElseThrow(() -> new IllegalArgumentException("Contact list not found"));

        List<String> fields = command.getFields();
        if (fields == null || fields.isEmpty()) {
            fields = List.of("email", "firstName", "lastName", "tags", "leadScore");
        }

        StringBuilder csv = new StringBuilder();

        // Add header
        csv.append(String.join(",", fields)).append("\n");

        // Add data rows
        for (Contact contact : contactList.getContacts()) {
            List<String> values = new ArrayList<>();

            for (String field : fields) {
                String value = getContactFieldValue(contact, field);
                values.add(escapeCSVValue(value));
            }

            csv.append(String.join(",", values)).append("\n");
        }

        log.info("Exported {} contacts from list {}", contactList.getContacts().size(), command.getListId());

        return csv.toString();
    }

    private String getContactFieldValue(Contact contact, String field) {
        switch (field.toLowerCase()) {
            case "email":
                return contact.getEmail() != null ? contact.getEmail() : "";
            case "firstname":
                return contact.getFirstName() != null ? contact.getFirstName() : "";
            case "lastname":
                return contact.getLastName() != null ? contact.getLastName() : "";
            case "phone":
                return contact.getPhone() != null ? contact.getPhone() : "";
            case "tags":
                if (contact.getTags() != null && !contact.getTags().isEmpty()) {
                    return contact.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.joining(";"));
                }
                return "";
            case "leadscore":
                return contact.getLeadScore() != null ? contact.getLeadScore().toString() : "0";
            case "createdat":
                return contact.getCreatedAt() != null ? contact.getCreatedAt().toString() : "";
            case "lastactivityat":
                return contact.getLastActivityAt() != null ? contact.getLastActivityAt().toString() : "";
            default:
                return "";
        }
    }

    private String escapeCSVValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, newline, or quote, wrap in quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            // Escape quotes by doubling them
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }

        return value;
    }
}
