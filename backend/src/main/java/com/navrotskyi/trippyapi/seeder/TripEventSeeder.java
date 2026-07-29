package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.Currency;
import com.navrotskyi.trippyapi.domain.TripEvent;
import com.navrotskyi.trippyapi.domain.User;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class TripEventSeeder {

    public List<TripEvent> getSampleTripEvents(List<User> users, List<Currency> currencies) {
        List<TripEvent> trips = new ArrayList<>();
        Currency pln = currencies.get(0);
        Currency usd = currencies.get(1);
        Currency eur = currencies.get(2);
        Currency gbp = currencies.get(3);

        trips.add(createTrip("Weekend w Tatrach", users.get(1), pln, 10, 14, "1500.00"));
        trips.add(createTrip("Rzymskie Wakacje", users.get(2), eur, 20, 27, "4500.00"));
        trips.add(createTrip("Roadtrip po USA", users.get(3), usd, 40, 60, "15000.00"));
        trips.add(createTrip("Majówka na Mazurach", users.get(4), pln, -10, -5, "1200.00"));
        trips.add(createTrip("City Break Paryż", users.get(5), eur, 15, 18, "3000.00"));
        trips.add(createTrip("Narty w Alpach", users.get(2), eur, 100, 107, "5000.00"));
        trips.add(createTrip("Zwiedzanie Londynu", users.get(1), gbp, 30, 35, "4000.00"));
        trips.add(createTrip("Kemping Bieszczady", users.get(4), pln, 5, 8, "800.00"));
        trips.add(createTrip("Odpoczynek na Malediwach", users.get(3), usd, 200, 214, "25000.00"));
        trips.add(createTrip("Festiwal w Berlinie", users.get(1), eur, 12, 15, "1800.00"));
        trips.add(createTrip("Praga ze znajomymi", users.get(2), pln, -30, -27, "1000.00"));
        trips.add(createTrip("Sylwester w Wiedniu", users.get(5), eur, 250, 255, "3500.00"));
        trips.add(createTrip("Smaki Barcelony", users.get(5), eur, 45, 52, "4200.00"));
        trips.add(createTrip("Słoneczna Lizbona", users.get(3), eur, 60, 67, "3800.00"));
        trips.add(createTrip("Trekking w Norwegii", users.get(4), eur, 80, 90, "6000.00"));

        // --- SHOWCASE: demonstracja endpointu /api/trips/{id}/balances ---
        // Wycieczka owned by Jan, 5 zaakceptowanych uczestników + 1 zaproszony niezaakceptowany (Admin).
        // 5 wydatków shared (total 4000 PLN) + 1 separate (150 PLN, pomijany).
        // Oczekiwane bilanse: Jan +200, Anna +1200, Marek -600, Zofia -600, Piotr -200.
        // Oczekiwane przelewy (optimum 3): Piotr→Jan 200, Marek→Anna 600, Zofia→Anna 600.
        trips.add(createTrip("Wyjazd integracyjny: Zakopane 2026", users.get(1), pln, 30, 34, "5000.00"));

        return trips;
    }

    private TripEvent createTrip(String name, User owner, Currency currency, int startDaysFromNow, int endDaysFromNow, String budget) {
        TripEvent event = new TripEvent();
        event.setName(name);
        event.setOwner(owner);
        event.setCurrency(currency);
        event.setStartDate(LocalDate.now().plusDays(startDaysFromNow));
        event.setEndDate(LocalDate.now().plusDays(endDaysFromNow));
        event.setBudget(new BigDecimal(budget));
        event.setParticipants(new ArrayList<>());
        return event;
    }
}