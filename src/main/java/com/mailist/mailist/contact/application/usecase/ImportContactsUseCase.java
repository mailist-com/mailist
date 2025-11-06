package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.port.out.ContactListRepository;
import com.mailist.mailist.contact.application.port.out.ContactRepository;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.service.ListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImportContactsUseCase {

    private final ContactListRepository contactListRepository;
    private final ContactRepository contactRepository;
    private final ListService listService;

    public ImportContactsResult execute(ImportContactsCommand command) {
        log.info("Importing contacts from CSV to list ID: {}", command.getListId());

        ContactList contactList = contactListRepository.findById(command.getListId())
                .orElseThrow(() -> new IllegalArgumentException("Contact list not found"));

        if (contactList.getIsDynamic()) {
            throw new IllegalStateException("Cannot import contacts to a smart list");
        }

        int imported = 0;
        int skipped = 0;
        int errors = 0;
        List<ImportContactsResult.ErrorDetail> errorDetails = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(command.getFile().getInputStream()))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String[] headers = parseCSVLine(headerLine);
            Map<String, Integer> columnIndexMap = buildColumnIndexMap(headers, command.getMapping());

            String line;
            int rowNumber = 1; // Start from 1 (header is 0)

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] values = parseCSVLine(line);

                    String email = getValueFromColumn(values, columnIndexMap, "email");
                    String firstName = getValueFromColumn(values, columnIndexMap, "firstName");
                    String lastName = getValueFromColumn(values, columnIndexMap, "lastName");

                    if (email == null || email.trim().isEmpty()) {
                        errors++;
                        errorDetails.add(ImportContactsResult.ErrorDetail.builder()
                                .row(rowNumber)
                                .email(email)
                                .error("Email is required")
                                .build());
                        continue;
                    }

                    // Validate email format
                    if (!isValidEmail(email)) {
                        errors++;
                        errorDetails.add(ImportContactsResult.ErrorDetail.builder()
                                .row(rowNumber)
                                .email(email)
                                .error("Invalid email format")
                                .build());
                        continue;
                    }

                    // Check if contact already exists
                    Optional<Contact> existingContact = contactRepository.findByEmail(email);

                    if (existingContact.isPresent()) {
                        if (Boolean.TRUE.equals(command.getSkipDuplicates())) {
                            skipped++;
                            continue;
                        }

                        // Add existing contact to list
                        Contact contact = existingContact.get();
                        if (!contactList.getContacts().contains(contact)) {
                            listService.addContactToList(contactList, contact);
                            imported++;
                        } else {
                            skipped++;
                        }
                    } else {
                        // Create new contact
                        Contact newContact = Contact.builder()
                                .email(email)
                                .firstName(firstName != null ? firstName : "")
                                .lastName(lastName != null ? lastName : "")
                                .leadScore(0)
                                .build();

                        Contact savedContact = contactRepository.save(newContact);
                        listService.addContactToList(contactList, savedContact);
                        imported++;
                    }

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", rowNumber, e.getMessage());
                    errors++;
                    errorDetails.add(ImportContactsResult.ErrorDetail.builder()
                            .row(rowNumber)
                            .email("unknown")
                            .error(e.getMessage())
                            .build());
                }
            }

            contactListRepository.save(contactList);

        } catch (Exception e) {
            log.error("Error importing contacts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to import contacts: " + e.getMessage(), e);
        }

        log.info("Import completed: {} imported, {} skipped, {} errors", imported, skipped, errors);

        return ImportContactsResult.builder()
                .imported(imported)
                .skipped(skipped)
                .errors(errors)
                .errorDetails(errorDetails)
                .build();
    }

    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());

        return values.toArray(new String[0]);
    }

    private Map<String, Integer> buildColumnIndexMap(String[] headers, Map<String, String> mapping) {
        Map<String, Integer> indexMap = new HashMap<>();

        if (mapping == null || mapping.isEmpty()) {
            // Default mapping - assume headers match field names
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].toLowerCase().trim();
                if (header.equals("email") || header.equals("firstname") || header.equals("lastname")) {
                    indexMap.put(header, i);
                }
            }
        } else {
            // Use provided mapping
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String fieldName = entry.getKey();
                String columnName = entry.getValue();

                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].equalsIgnoreCase(columnName)) {
                        indexMap.put(fieldName, i);
                        break;
                    }
                }
            }
        }

        return indexMap;
    }

    private String getValueFromColumn(String[] values, Map<String, Integer> columnIndexMap, String fieldName) {
        Integer index = columnIndexMap.get(fieldName);
        if (index == null || index >= values.length) {
            return null;
        }
        String value = values[index];
        return value.replace("\"", "").trim();
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Simple email validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
