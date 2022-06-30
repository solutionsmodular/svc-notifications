package com.solmod.notification.domain;

public interface SenderStrategy {

    SendResponse deliver(SolCommunication communication);
}
