package com.mailist.mailist.contact.interfaces.mapper;

import com.mailist.mailist.contact.application.usecase.command.CreateContactCommand;
import com.mailist.mailist.contact.application.usecase.command.UpdateContactCommand;
import com.mailist.mailist.contact.domain.aggregate.Contact;
import com.mailist.mailist.contact.domain.aggregate.ContactList;
import com.mailist.mailist.contact.domain.valueobject.Tag;
import com.mailist.mailist.contact.interfaces.dto.ContactDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ContactMapper {

    @Mapping(target = "lists", ignore = true)
    ContactDto.Response toResponse(Contact contact);

    List<ContactDto.Response> toResponseList(List<Contact> contacts);

    CreateContactCommand toCreateCommand(ContactDto.CreateRequest request);

    @Mapping(target = "id", source = "id")
    UpdateContactCommand toUpdateCommand(Long id, ContactDto.UpdateRequest request);

    ContactDto.TagDto toTagDto(Tag tag);

    Set<ContactDto.TagDto> toTagDtoSet(Set<Tag> tags);

    ContactDto.ContactListDto toContactListDto(ContactList contactList);

    Set<ContactDto.ContactListDto> toContactListDtoSet(Set<ContactList> contactLists);
}