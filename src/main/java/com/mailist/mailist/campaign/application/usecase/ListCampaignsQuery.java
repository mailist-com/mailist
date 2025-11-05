package com.mailist.mailist.campaign.application.usecase;

import lombok.Value;
import org.springframework.data.domain.Pageable;

@Value
public class ListCampaignsQuery {
    Pageable pageable;
}