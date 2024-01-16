package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.service.UserDeliveryPreferencesService;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UserPreferencesFilter implements MessageDeliveryFilter {

    private MessageDeliveryRepo deliveryRepo;
    private UserDeliveryPreferencesService userDeliveryPreferencesService;
    private final Logger logger = LoggerFactory.getLogger(UserPreferencesFilter.class);

    @Autowired
    public UserPreferencesFilter(MessageDeliveryRepo deliveryRepo,
                                 UserDeliveryPreferencesService userDeliveryPreferencesService) {
        this.deliveryRepo = deliveryRepo;
        this.userDeliveryPreferencesService = userDeliveryPreferencesService;
    }

    @Override
    public FilterResponse apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage) {
        FilterResponse response = new FilterResponse("user-preferences");

        if (templateGroup.getQualifiedTemplates().isEmpty()) {
            return response;
        }

        Iterator<MessageTemplate> templateIter = templateGroup.getQualifiedTemplates().iterator();
        while (templateIter.hasNext()) {
            MessageTemplate curTemplate = templateIter.next();

            String recipientAddress = solMessage.buildMetadata().getOrDefault(
                    curTemplate.getRecipientAddressContextKey(), "").toString();
            // TODO: This should come from a service, not via repo
            List<MessageDelivery> allDeliveries = deliveryRepo.findAllDeliveries(curTemplate.getMessageTemplateID(),
                    recipientAddress,
                    solMessage.getIdMetadataKey(),
                    solMessage.getIdMetadataValue());

            String templateSender = curTemplate.getSender();
            // Get would-be recipient for the message, given the SolMessage metadata

            if (!StringUtils.isEmpty(recipientAddress)) {
                MessageDelivery latestDelivery = allDeliveries.stream().findFirst().orElse(null);
                if (latestDelivery != null) {
                    UserDeliveryPreferencesDTO usersPrefs =
                            userDeliveryPreferencesService.getDeliveryPreferences(recipientAddress, templateSender);

                    DeliveryPermission deliveryPermission =
                            canDeliver(usersPrefs, Objects.requireNonNullElse(latestDelivery.getDateCompleted(), latestDelivery.getDateCreated()));

                    response.addDeliveryPermission(curTemplate.getMessageTemplateID(), deliveryPermission);
                }
            }
        }

        throw new NotImplementedException("UserPreferencesFilter is not yet implemented");
    }

    /**
     * Two factors here:
     * <ul>
     *     <li>{@code UserDeliveryPreferences.sendWindowStart} - {@code UserDeliveryPreferences.sendWindowEnd} delivery
     *     wind ow (results in a digest)</li>
     *     <li>{@code UserDeliveryPreferences.resendInterval} - Min amount of time to elapse before a resend (does not
     *     result in a digest</li>
     *  </li>
     * </ul>
     *
     * @param usersPrefs
     * @param latestEffective
     * @return
     */
    DeliveryPermission canDeliver(UserDeliveryPreferencesDTO usersPrefs, Date latestEffective) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(usersPrefs.getTimezone()));
        Date now = new Date();
        cal.setTime(now);

        //Ensure if this delivery would be at least resendInterval from the last delivery
        Date resendIntervalAgo = DateUtils.addMinutes(now, -usersPrefs.getResendInterval());
        if (latestEffective.after(resendIntervalAgo)) {
            return DeliveryPermission.SEND_NEVER;
        }

        // Ensure this delivery will fall within the user's preferred delivery window
        long fragmentInHours = DateUtils.getFragmentInHours(cal, 6);

        boolean withinDeliveryWindow = usersPrefs.getSendWindowStart() == null || usersPrefs.getSendWindowEnd() == null ||
                fragmentInHours >= usersPrefs.getSendWindowStart() && fragmentInHours <= usersPrefs.getSendWindowEnd();

        if (!withinDeliveryWindow) {
            return DeliveryPermission.SEND_LATER;
        }

        return DeliveryPermission.SEND_NOW;
    }
}
