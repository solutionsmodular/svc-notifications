package com.solmod.notifications.dispatcher.repository.domain;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;

/**
 * Record of a delivery of a message which will archive the delivery from before any attempts to delivery until
 * delivery is complete and any callbacks have been completed.
 * <ol>
 *     <li>When are placeholders replaced?</li>
 *     <li>Consider the context builder process. When does that happen?</li>
 *     <li>Delivery includes metadata. Metadata should include what's needed for recipient, content, and whatever else
 *         might be needed to determine duplicate</li>
 *     <li>A message can be different, depending on the sender. Need to package diff content elements into envelope</li>
 * </ol>
 */
@Entity(name = "MessageDeliveries")
public class MessageDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    @Column(unique = true)
    private String recipientAddress;
    private Long messageTemplateId;
    @Enumerated(EnumType.STRING)
    @Column(length = 3)
    private Status status;
    private String statusMessage; // e.g. when Status = failed, message is failure
    private String sender;
    @OneToMany(mappedBy = "messageDelivery", cascade = CascadeType.ALL)
    private Set<MessageMetadata> messageMetadata;
    private Date dateCreated;
    private Date dateCompleted;

    public enum Status {
        D,  // Delivered
        F,  // Failed
        PX, // Pending Context
        PD, // Pending Delivery - initial send
        PC, // Pending callback from delivery
        PR, // Pending Retry
        PT; // Pending time window
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddyContextKey) {
        this.recipientAddress = recipientAddyContextKey;
    }

    public Long getMessageTemplateId() {
        return messageTemplateId;
    }

    public void setMessageTemplateId(Long messageTemplateId) {
        this.messageTemplateId = messageTemplateId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Set<MessageMetadata> getMessageMetadata() {
        return messageMetadata;
    }

    public void setMessageMetadata(Set<MessageMetadata> messageMetadata) {
        this.messageMetadata = messageMetadata;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}
