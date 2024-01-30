package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.service.UserDeliveryPreferencesService;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import com.solmod.notifications.dispatcher.service.domain.TriggeredMessageTemplateGroup;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_LATER;
import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static java.util.Collections.emptyList;

@Component
public class UserPreferencesFilter implements MessageDeliveryFilter {

    private final MessageDeliveryRepo deliveryRepo;
    private final UserDeliveryPreferencesService userDeliveryPreferencesService;
    private final Logger logger = LoggerFactory.getLogger(UserPreferencesFilter.class);

    @Autowired
    public UserPreferencesFilter(MessageDeliveryRepo deliveryRepo,
                                 UserDeliveryPreferencesService userDeliveryPreferencesService) {
        this.deliveryRepo = deliveryRepo;
        this.userDeliveryPreferencesService = userDeliveryPreferencesService;
    }

    @Override
    public FilterResponse apply(TriggeredMessageTemplateGroup templateGroup, SolMessage solMessage)
            throws FilterException {
        FilterResponse response = new FilterResponse("user-preferences");

        if (templateGroup.getQualifiedTemplates().isEmpty()) {
            return response;
        }

        // Determine send'ability for each template in the group
        for (MessageTemplate curTemplate : templateGroup.getQualifiedTemplates()) {
            DeliveryPermission permissionToSendTemplate = processRules(solMessage, curTemplate);
            response.addDeliveryPermission(curTemplate.getMessageTemplateID(), permissionToSendTemplate);
        }

        return response;
    }

    private DeliveryPermission processRules(SolMessage solMessage, MessageTemplate curTemplate) throws FilterException {
        DeliveryPermission result;
        String recipientAddress = getRecipientAddressOrException(solMessage, curTemplate);

        String templateSender = curTemplate.getSender();
        UserDeliveryPreferencesDTO usersPrefs =
                userDeliveryPreferencesService.getDeliveryPreferences(recipientAddress, templateSender);

        // Ensure the user has preferences specified for the template's sender
        if (usersPrefs == null) {
            String message = String.format("Recipient has no specified preferences for %s sender", curTemplate.getSender());
            result = new DeliveryPermission(SEND_NEVER, message);
        }
        // Ensure the user's preferences for the template's sender allow for the template's class
        else if (!Objects.requireNonNullElse(usersPrefs.getSupportedMessageClasses(), "").toUpperCase()
                .contains(curTemplate.getMessageClass().toUpperCase())) {
            String message = String.format("User preferences do not allow %s messages via %s",
                    curTemplate.getMessageClass(), curTemplate.getSender());
            result = new DeliveryPermission(SEND_NEVER, message);
        }
        // Ensure the message does not violate time-based settings: resendInterval and deliveryWindow
        else {

            // TODO: This should come from a service, not via repo
            List<MessageDelivery> allDeliveries =
                    Objects.requireNonNullElse(deliveryRepo.findAllDeliveries(curTemplate.getMessageTemplateID(),
                            recipientAddress,
                            solMessage.getIdMetadataKey(),
                            solMessage.getIdMetadataValue()), emptyList());

            MessageDelivery latestDelivery = allDeliveries.stream().findFirst().orElse(null);
            result = applyTimeBasedRules(usersPrefs, latestDelivery);
        }

        return result;
    }

    private String getRecipientAddressOrException(SolMessage solMessage, MessageTemplate curTemplate)
            throws FilterException {
        String addyKey = curTemplate.getRecipientAddressContextKey();
        Object addressData = solMessage.getMetadata().get(addyKey);
        if (addressData == null) {
            throw new FilterException("Could not determine recipient address, expected at " + addyKey);
        }
        return addressData.toString();
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
     * @param usersPrefs {@link UserDeliveryPreferencesDTO}
     * @param latestDelivery {@link MessageDelivery} Could be null if no previous deliveries sent
     * @return {@link DeliveryPermission} Indicating the permissions as they relate to time-based criteria
     */
    DeliveryPermission applyTimeBasedRules(UserDeliveryPreferencesDTO usersPrefs, MessageDelivery latestDelivery) {
        Date now = new Date();

        // If duplicate sent, ensure at least resendInterval has elapsed since the last delivery
        if (latestDelivery != null && usersPrefs.getResendInterval() != null) {
            Date effectiveLatestDeliveryDate =
                    Objects.requireNonNullElse(latestDelivery.getDateCompleted(), latestDelivery.getDateCreated());
            Date resendIntervalAgo = DateUtils.addMinutes(now, -usersPrefs.getResendInterval());
            if (effectiveLatestDeliveryDate.after(resendIntervalAgo)) {
                return new DeliveryPermission(SEND_NEVER, "Recipient's interval settings for duplicate message has not elapsed");
            }
        }

        // Ensure this delivery will fall within the user's preferred delivery window
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        long fragmentInHours = DateUtils.getFragmentInHours(cal, 6);

        boolean withinDeliveryWindow = usersPrefs.getSendWindowStart() == null || usersPrefs.getSendWindowEnd() == null ||
                fragmentInHours >= usersPrefs.getSendWindowStart() && fragmentInHours <= usersPrefs.getSendWindowEnd();

        if (!withinDeliveryWindow) {
            return new DeliveryPermission(SEND_LATER, "Cannot deliver within recipient blackout period. Sending later");
        }

        return DeliveryPermission.SEND_NOW_PERMISSION;
    }
}
