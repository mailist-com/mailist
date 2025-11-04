package com.mailist.marketing.analytics.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportData {
    
    @Column(name = "total_sent")
    private Long totalSent;
    
    @Column(name = "total_delivered")
    private Long totalDelivered;
    
    @Column(name = "total_opened")
    private Long totalOpened;
    
    @Column(name = "total_clicked")
    private Long totalClicked;
    
    @Column(name = "total_bounced")
    private Long totalBounced;
    
    @Column(name = "total_unsubscribed")
    private Long totalUnsubscribed;
    
    @Column(name = "total_contacts")
    private Long totalContacts;
    
    @Column(name = "active_campaigns")
    private Long activeCampaigns;
    
    @Column(name = "active_automations")
    private Long activeAutomations;
    
    @Column(name = "delivery_rate", precision = 5, scale = 2)
    private BigDecimal deliveryRate;
    
    @Column(name = "open_rate", precision = 5, scale = 2)
    private BigDecimal openRate;
    
    @Column(name = "click_rate", precision = 5, scale = 2)
    private BigDecimal clickRate;
    
    @Column(name = "bounce_rate", precision = 5, scale = 2)
    private BigDecimal bounceRate;
    
    @Column(name = "unsubscribe_rate", precision = 5, scale = 2)
    private BigDecimal unsubscribeRate;
    
    @Column(name = "period_start")
    private LocalDateTime periodStart;
    
    @Column(name = "period_end")
    private LocalDateTime periodEnd;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON format for custom metrics
    
    public static ReportData createCampaignData(Long sent, Long delivered, Long opened, Long clicked, 
                                              Long bounced, Long unsubscribed, LocalDateTime periodStart, 
                                              LocalDateTime periodEnd) {
        return new ReportData(
                sent,
                delivered,
                opened,
                clicked,
                bounced,
                unsubscribed,
                null,
                null,
                null,
                calculateDeliveryRate(sent, delivered),
                calculateOpenRate(delivered, opened),
                calculateClickRate(opened, clicked),
                calculateBounceRate(sent, bounced),
                calculateUnsubscribeRate(sent, unsubscribed),
                periodStart,
                periodEnd,
                null
        );
    }
    
    public static ReportData createOverallData(Long totalContacts, Long activeCampaigns, 
                                             Long activeAutomations, Long totalSent, Long totalDelivered,
                                             Long totalOpened, Long totalClicked, Long totalBounced,
                                             Long totalUnsubscribed, LocalDateTime periodStart,
                                             LocalDateTime periodEnd) {
        return new ReportData(
                totalSent,
                totalDelivered,
                totalOpened,
                totalClicked,
                totalBounced,
                totalUnsubscribed,
                totalContacts,
                activeCampaigns,
                activeAutomations,
                calculateDeliveryRate(totalSent, totalDelivered),
                calculateOpenRate(totalDelivered, totalOpened),
                calculateClickRate(totalOpened, totalClicked),
                calculateBounceRate(totalSent, totalBounced),
                calculateUnsubscribeRate(totalSent, totalUnsubscribed),
                periodStart,
                periodEnd,
                null
        );
    }
    
    private static BigDecimal calculateDeliveryRate(Long sent, Long delivered) {
        if (sent == null || sent == 0 || delivered == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(delivered)
                .divide(BigDecimal.valueOf(sent), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private static BigDecimal calculateOpenRate(Long delivered, Long opened) {
        if (delivered == null || delivered == 0 || opened == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(opened)
                .divide(BigDecimal.valueOf(delivered), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private static BigDecimal calculateClickRate(Long opened, Long clicked) {
        if (opened == null || opened == 0 || clicked == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(clicked)
                .divide(BigDecimal.valueOf(opened), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private static BigDecimal calculateBounceRate(Long sent, Long bounced) {
        if (sent == null || sent == 0 || bounced == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(bounced)
                .divide(BigDecimal.valueOf(sent), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private static BigDecimal calculateUnsubscribeRate(Long sent, Long unsubscribed) {
        if (sent == null || sent == 0 || unsubscribed == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(unsubscribed)
                .divide(BigDecimal.valueOf(sent), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}