package com.mailist.mailist.campaign.application.usecase;

import com.mailist.mailist.campaign.application.usecase.command.CreateCampaignCommand;
import com.mailist.mailist.campaign.application.usecase.command.SendCampaignCommand;
import com.mailist.mailist.campaign.domain.aggregate.Campaign;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class CampaignApplicationService {

    private final CreateCampaignUseCase createCampaignUseCase;
    private final SendCampaignUseCase sendCampaignUseCase;

    public Campaign create(final CreateCampaignCommand command) {
        return createCampaignUseCase.execute(command);
    }

    public Campaign sendCampaing(final SendCampaignCommand command) {
        return sendCampaignUseCase.execute(command);
    }
}
