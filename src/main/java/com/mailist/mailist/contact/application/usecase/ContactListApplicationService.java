package com.mailist.mailist.contact.application.usecase;

import com.mailist.mailist.contact.application.usecase.command.CreateContactListCommand;
import com.mailist.mailist.contact.application.usecase.command.SubscribeContactsCommand;
import com.mailist.mailist.contact.application.usecase.command.UpdateContactListCommand;
import com.mailist.mailist.contact.application.usecase.dto.GlobalStatistics;
import com.mailist.mailist.contact.application.usecase.dto.SubscribeResult;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ContactListApplicationService {

    private final CreateContactListUseCase createContactListUseCase;
    private final UpdateContactListUseCase updateContactListUseCase;
    private final SubscribeContactsToListUseCase subscribeContactsToListUseCase;
    private final UnsubscribeContactsFromListUseCase unsubscribeContactsFromListUseCase;
    private final GetGlobalListStatisticsUseCase getGlobalListStatisticsUseCase;

    public GlobalStatistics globalListStatistics() {
        return getGlobalListStatisticsUseCase.execute();
    }

    public ContactList createContactList(final CreateContactListCommand command) {
        return createContactListUseCase.execute(command);
    }

    public ContactList updateContactList(final UpdateContactListCommand command) {
        return updateContactListUseCase.execute(command);
    }

    public SubscribeResult subscribeContactToList(final SubscribeContactsCommand command) {
        return subscribeContactsToListUseCase.execute(command);
    }

    public SubscribeResult unsubscribeContactFromList(final SubscribeContactsCommand command) {
        return unsubscribeContactsFromListUseCase.execute(command);
    }
}
