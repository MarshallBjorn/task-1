package com.navrotskyi.trippyapi.controller;

import com.navrotskyi.trippyapi.domain.User;
import com.navrotskyi.trippyapi.service.TripParticipantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripParticipantControllerWiringTest {

    @Mock TripParticipantService tripParticipantService;

    @Test
    void deleteEndpoint_delegatesToService() {
        TripParticipantController controller = new TripParticipantController(tripParticipantService);
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User caller = new User();
        caller.setId(UUID.randomUUID());

        controller.removeParticipant(eventId, userId, caller);

        verify(tripParticipantService).removeTripParticipant(eventId, userId, caller);
    }
}