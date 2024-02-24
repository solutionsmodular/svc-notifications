package com.solmod.notifications.dispatcher.filter;

import com.solmod.notifications.admin.service.UserDeliveryPreferencesService;
import com.solmod.notifications.admin.web.model.UserDeliveryPreferencesDTO;
import com.solmod.notifications.dispatcher.domain.MessageTemplate;
import com.solmod.notifications.dispatcher.domain.SolMessage;
import com.solmod.notifications.dispatcher.domain.TriggeringEvent;
import com.solmod.notifications.dispatcher.repository.MessageDeliveryRepo;
import com.solmod.notifications.dispatcher.repository.domain.MessageDelivery;
import com.solmod.notifications.dispatcher.service.domain.DeliveryPermission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_LATER;
import static com.solmod.notifications.dispatcher.service.domain.DeliveryPermission.Verdict.SEND_NEVER;
import static java.util.Collections.emptyList;

@Component
public class UserPreferencesFilter implements MessageDeliveryFilter {

    private final MessageDeliveryRepo deliveryRepo;
    private final UserDeliveryPreferencesService userDeliveryPreferencesService;

    @Autowired
    public UserPreferencesFilter(MessageDeliveryRepo deliveryRepo,
                                 UserDeliveryPreferencesService userDeliveryPreferencesService) {
        this.deliveryRepo = deliveryRepo;
        this.userDeliveryPreferencesService = userDeliveryPreferencesService;
    }

    @Override
    public String getFilterName() {
        return "user-preferences";
    }

    @Override
    public DeliveryPermission apply(final MessageTemplate messageTemplate, TriggeringEvent trigger)
            throws FilterException {

        return processRules(trigger, messageTemplate);
    }

    /**
     * Determine if the following hold true before permitting:
     * <ul>
     *     <li>Recipient's address can be gleaned from the {@link SolMessage}</li>
     *     <li>Recipient has preferences configured for the template sender</li>
     *     <li>Recipient allows the template class via sender</li>
     *     <li>Sending this message will not be a duplicate of a message sent within the resendInterval</li>
     *     <li>"Now" is within the valid send window. When it isn't, it's set to be sent later</li>
     * </ul>
     *
     * @param trigger {@link TriggeringEvent}
     * @param curTemplate {@link MessageTemplate}
     * @return {@link DeliveryPermission}
     * @throws FilterException in the event of processing error, such as recipient can't be gleaned from message
     */
    private DeliveryPermission processRules(TriggeringEvent trigger, MessageTemplate curTemplate) throws FilterException {
        DeliveryPermission result;
        String recipientAddress = getRecipientAddressOrException(trigger, curTemplate);

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
                            trigger.getSubjectIdMetadataKey(),
                            trigger.getEventMetadata().get(trigger.getSubjectIdMetadataKey())), emptyList());

            MessageDelivery latestDelivery = allDeliveries.stream().findFirst().orElse(null);
            result = applyTimeBasedRules(usersPrefs, latestDelivery);
        }

        return result;
    }

    private String getRecipientAddressOrException(TriggeringEvent trigger, MessageTemplate curTemplate)
            throws FilterException {
        String addyKey = curTemplate.getRecipientAddressContextKey();
        String addressData = trigger.getEventMetadata().get(addyKey);
        if (StringUtils.isBlank(addressData)) {
            throw new FilterException("Could not determine recipient address, expected at " + addyKey);
        }
        return addressData;
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

        if (latestDelivery == null) {
            return DeliveryPermission.SEND_NOW_PERMISSION;
        }

        // If duplicate sent, ensure at least resendInterval has elapsed since the last delivery
        if (usersPrefs.getResendInterval() != null) {
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
