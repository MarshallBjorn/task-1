package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripParticipant;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripParticipantSeeder {

    public List<TripParticipant> getSampleParticipants(List<TripEvent> trips, List<User> users, List<TripRole> roles) {
        List<TripParticipant> participants = new ArrayList<>();
        TripRole organizer = roles.get(0);
        TripRole participant = roles.get(1);
        TripRole viewer = roles.get(2);

        // Ostatnia wycieczka to SHOWCASE — ma własny, custom zestaw uczestników.
        int genericCount = trips.size() - 1;
        for (int i = 0; i < genericCount; i++) {
            TripEvent t = trips.get(i);
            participants.add(new TripParticipant(t, t.getOwner(), organizer, BigDecimal.ZERO, true));

            User pUser = users.get((i + 1) % users.size());
            if (!pUser.equals(t.getOwner())) {
                participants.add(new TripParticipant(t, pUser, participant, new BigDecimal("200"), true));
            }

            User vUser = users.get((i + 2) % users.size());
            if (!vUser.equals(t.getOwner()) && !vUser.equals(pUser)) {
                participants.add(new TripParticipant(t, vUser, viewer, BigDecimal.ZERO, true));
            }
        }

        // --- SHOWCASE: 5 accepted + 1 invited-not-accepted (Admin) ---
        TripEvent showcase = trips.get(trips.size() - 1);
        User admin = users.get(0);
        User jan    = users.get(1);
        User anna   = users.get(2);
        User marek  = users.get(3);
        User zofia  = users.get(4);
        User piotr  = users.get(5);

        participants.add(new TripParticipant(showcase, jan,   organizer,   BigDecimal.ZERO, true));
        participants.add(new TripParticipant(showcase, anna,  participant, new BigDecimal("500"), true));
        participants.add(new TripParticipant(showcase, marek, participant, new BigDecimal("500"), true));
        participants.add(new TripParticipant(showcase, zofia, participant, new BigDecimal("500"), true));
        participants.add(new TripParticipant(showcase, piotr, participant, new BigDecimal("500"), true));
        // Admin — zaproszony, ale jeszcze nie potwierdził. Nie powinien być w bilansach.
        participants.add(new TripParticipant(showcase, admin, viewer,      BigDecimal.ZERO, false));

        return participants;
    }
}