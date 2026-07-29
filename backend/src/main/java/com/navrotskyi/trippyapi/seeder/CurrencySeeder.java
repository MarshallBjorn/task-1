package com.navrotskyi.trippyapi.seeder;

import com.navrotskyi.trippyapi.domain.Currency;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CurrencySeeder {
    public List<Currency> getSampleCurrencies() {
        return List.of(
                new Currency("PLN", "Polski Złoty"),
                new Currency("USD", "Dolar Amerykański"),
                new Currency("EUR", "Euro"),
                new Currency("GBP", "Funt Brytyjski"),
                new Currency("CHF", "Frank Szwajcarski")
        );
    }
}