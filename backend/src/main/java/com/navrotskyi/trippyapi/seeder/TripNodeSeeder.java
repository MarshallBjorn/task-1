package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.TripNode;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripNodeSeeder {

    public List<TripNode> getSampleTripNodes(List<TripEvent> trips, List<User> users) {
        List<TripNode> nodes = new ArrayList<>();
        User jan = users.get(1);
        User anna = users.get(2);
        User marek = users.get(3);
        User zofia = users.get(4);
        User piotr = users.get(5);

        TripEvent tatry = trips.get(0);
        TripEvent rzym = trips.get(1);
        TripEvent usa = trips.get(2);
        TripEvent mazury = trips.get(3);
        TripEvent paryz = trips.get(4);

        nodes.add(createNode(tatry, jan, "Przejazd pociągiem PKP", "Wagon 4, przedział 2.", tatry.getStartDate().atTime(8, 0), tatry.getStartDate().atTime(13, 30), "120.00", false));
        nodes.add(createNode(tatry, jan, "Zameldowanie w Willi", "Pamiętać o opłacie.", tatry.getStartDate().atTime(14, 0), tatry.getStartDate().atTime(14, 30), "600.00", false));
        nodes.add(createNode(tatry, anna, "Obiad na Krupówkach", "Stolik w Karczmie.", tatry.getStartDate().atTime(15, 30), tatry.getStartDate().atTime(17, 0), "250.00", true));
        nodes.add(createNode(rzym, anna, "Lot WizzAir", "Odprawa zrobiona.", rzym.getStartDate().atTime(10, 0), rzym.getStartDate().atTime(12, 15), "450.00", false));
        nodes.add(createNode(usa, marek, "Lot Warszawa - Nowy Jork", "Lot bezpośredni LOT.", usa.getStartDate().atTime(12, 0), usa.getStartDate().atTime(20, 0), "3500.00", false));
        nodes.add(createNode(mazury, zofia, "Odbiór żaglówki", "Port Sztynort.", mazury.getStartDate().atTime(14, 0), mazury.getStartDate().atTime(15, 0), "1500.00", false));
        nodes.add(createNode(paryz, piotr, "Wejście na Wieżę Eiffla", "Wjazd windą.", paryz.getStartDate().plusDays(1).atTime(18, 0), paryz.getStartDate().plusDays(1).atTime(20, 0), "300.00", false));

        // Ostatnia wycieczka to SHOWCASE — ma własny, custom zestaw wydatków.
        int genericEnd = trips.size() - 1;
        for (int i = 5; i < genericEnd; i++) {
            TripEvent trip = trips.get(i);
            nodes.add(createNode(trip, trip.getOwner(), "Planowanie dnia", "Krótkie spotkanie", trip.getStartDate().atTime(9, 0), trip.getStartDate().atTime(9, 30), "0.00", false));
        }

        // --- SHOWCASE: 5 wydatków shared (total 4000) + 1 separate (pomijany w bilansie) ---
        TripEvent showcase = trips.get(trips.size() - 1);
        LocalDateTime day0 = showcase.getStartDate().atTime(10, 0);

        nodes.add(createNode(showcase, jan,
                "Transport busem Warszawa-Zakopane i z powrotem",
                "Bus 8-osobowy, paliwo + opłaty.",
                day0, day0.plusHours(6), "1000.00", false));

        nodes.add(createNode(showcase, anna,
                "Nocleg Willa Bachledówka (4 noce)",
                "Pokój 6-osobowy z kuchnią.",
                day0.plusHours(7), day0.plusDays(4), "2000.00", false));

        nodes.add(createNode(showcase, marek,
                "Wypożyczenie nart dla całej grupy",
                "Pakiet: narty + buty + kijki x5.",
                day0.plusDays(1).withHour(9), day0.plusDays(1).withHour(10), "200.00", false));

        nodes.add(createNode(showcase, zofia,
                "Obiad w Karczmie Regionalnej",
                "Kwaśnica, pierogi, oscypki dla wszystkich.",
                day0.plusDays(1).withHour(14), day0.plusDays(1).withHour(16), "200.00", false));

        nodes.add(createNode(showcase, piotr,
                "Zakupy do wspólnej kuchni",
                "Biedronka + lokalny sklep. Śniadania i kolacje.",
                day0.plusDays(1).withHour(17), day0.plusDays(1).withHour(18), "600.00", false));

        // Wydatek SEPARATE — Jan kupił tylko dla siebie, pomijany w rozliczeniu grupy.
        nodes.add(createNode(showcase, jan,
                "Pamiątka: oscypek i ciupaga (tylko dla Jana)",
                "Prezent dla żony. is_separate=true — nie wlicza się do bilansu grupy.",
                day0.plusDays(2).withHour(12), day0.plusDays(2).withHour(13), "150.00", true));

        return nodes;
    }

    private TripNode createNode(TripEvent event, User reporter, String name, String note, LocalDateTime start, LocalDateTime end, String price, boolean separate) {
        TripNode node = new TripNode();
        node.setEvent(event);
        node.setReporter(reporter);
        node.setName(name);
        node.setNote(note);
        node.setStartTime(start);
        node.setEndTime(end);
        node.setPrice(new BigDecimal(price));
        node.setSeparate(separate);
        return node;
    }
}