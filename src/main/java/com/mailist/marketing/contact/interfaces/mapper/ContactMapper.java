package com.mailist.marketing.contact.interfaces.mapper;

import com.mailist.marketing.contact.application.usecase.CreateContactCommand;
import com.mailist.marketing.contact.domain.aggregate.Contact;
import com.mailist.marketing.contact.domain.aggregate.ContactList;
import com.mailist.marketing.contact.domain.valueobject.Tag;
import com.mailist.marketing.contact.interfaces.dto.ContactDto;
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
    
    ContactDto.Response toResponse(Contact contact);
    
    List<ContactDto.Response> toResponseList(List<Contact> contacts);
    
    CreateContactCommand toCreateCommand(ContactDto.CreateRequest request);
    
    ContactDto.TagDto toTagDto(Tag tag);
    
    Set<ContactDto.TagDto> toTagDtoSet(Set<Tag> tags);
    
    ContactDto.ContactListDto toContactListDto(ContactList contactList);
    
    Set<ContactDto.ContactListDto> toContactListDtoSet(Set<ContactList> contactLists);
}