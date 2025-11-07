package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.AddTagToContactCommand;
import com.mailist.mailist.contact.application.usecase.command.CreateContactCommand;
import com.mailist.mailist.contact.application.usecase.command.UpdateContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactApplicationService {

    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final AddTagToContactUseCase addTagToContactUseCase;

    public Contact create(final CreateContactCommand command) {
        return createContactUseCase.execute(command);
    }

    public Contact update(final UpdateContactCommand command) {
        return updateContactUseCase.execute(command);
    }

    public Contact addTagToContact(final AddTagToContactCommand command) {
        return addTagToContactUseCase.execute(command);
    }
}
