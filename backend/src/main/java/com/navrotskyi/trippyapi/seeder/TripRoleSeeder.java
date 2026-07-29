package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.TripRole;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TripRoleSeeder {
    public List<TripRole> getSampleTripRoles() {
        return List.of(
                new TripRole("Organizer"),
                new TripRole("Participant"),
                new TripRole("Viewer")
        );
    }
}