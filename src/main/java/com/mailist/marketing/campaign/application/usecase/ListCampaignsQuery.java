package com.mailist.marketing.campaign.application.usecase;

import lombok.Value;
import org.springframework.data.domain.Pageable;

@Value
public class ListCampaignsQuery {
    Pageable pageable;
}