package com.mailist.mailist.shared.domain.gateway;

import com.mailist.mailist.shared.domain.model.EmailMessage;

public interface EmailGateway {
    void sendEmail(EmailMessage emailMessage);
    boolean isHealthy();
}