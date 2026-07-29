package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.*;
import com.navrotskyi.trippyapi.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    // Repozytoria
    private final CurrencyRepository currencyRepository;
    private final TripRoleRepository tripRoleRepository;
    private final UserRepository userRepository;
    private final TripEventRepository tripEventRepository;
    private final TripParticipantRepository tripParticipantRepository;
    private final TripNodeRepository tripNodeRepository;
    private final TripPostRepository tripPostRepository;

    // Seedery
    private final CurrencySeeder currencySeeder;
    private final TripRoleSeeder tripRoleSeeder;
    private final UserSeeder userSeeder;
    private final TripEventSeeder tripEventSeeder;
    private final TripParticipantSeeder tripParticipantSeeder;
    private final TripNodeSeeder tripNodeSeeder;
    private final TripPostSeeder tripPostSeeder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println("[SEEDER] Wykryto pustą bazę. Uruchamianie skryptów seedujących...");

            List<Currency> currencies = currencySeeder.getSampleCurrencies();
            currencyRepository.saveAllAndFlush(currencies);

            List<TripRole> roles = tripRoleSeeder.getSampleTripRoles();
            tripRoleRepository.saveAllAndFlush(roles);

            List<User> users = userSeeder.getSampleUsers();
            userRepository.saveAllAndFlush(users);

            List<TripEvent> trips = tripEventSeeder.getSampleTripEvents(users, currencies);
            tripEventRepository.saveAllAndFlush(trips);

            List<TripParticipant> participants = tripParticipantSeeder.getSampleParticipants(trips, users, roles);
            tripParticipantRepository.saveAllAndFlush(participants);

            List<TripNode> nodes = tripNodeSeeder.getSampleTripNodes(trips, users);
            tripNodeRepository.saveAllAndFlush(nodes);

            List<TripPost> posts = tripPostSeeder.getSampleTripPosts(nodes, users);
            tripPostRepository.saveAllAndFlush(posts);

            System.out.println("[SEEDER] Sukces! Baza została zasilona danymi testowymi.");
        }
    }
}