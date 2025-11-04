package com.mailist.marketing.shared.domain.gateway;

import com.mailist.marketing.shared.domain.model.EmailMessage;

public interface EmailGateway {
    void sendEmail(EmailMessage emailMessage);
    boolean isHealthy();
}