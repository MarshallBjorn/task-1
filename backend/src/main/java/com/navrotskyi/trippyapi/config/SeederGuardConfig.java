package com.navrotskyi.trippyapi.config;

import com.navrotskyi.trippyapi.seeder.DataInitializer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Defense-in-depth guard: nawet jeśli ktoś przypadkowo usunie {@code @Profile("dev")}
 * z {@link DataInitializer} albo doda nowy seeder bez adnotacji, ten bean
 * aktywny tylko na profilu {@code prod} sprawdzi kontekst przy starcie i wywali
 * aplikację (fail-fast), zanim dane produkcyjne zostaną nadpisane seedem.
 */
@Slf4j
@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class SeederGuardConfig {

    private final ApplicationContext context;

    @PostConstruct
    void validateNoSeedersOnProd() {
        if (!context.getBeansOfType(DataInitializer.class).isEmpty()) {
            throw new IllegalStateException(
                    "[PROD-GUARD] Wykryto DataInitializer na profilu 'prod'. "
                            + "Seedery nie mogą zostać uruchomione w środowisku produkcyjnym. "
                            + "Sprawdź adnotację @Profile(\"dev\") na DataInitializer."
            );
        }

        log.info("[PROD-GUARD] SeederGuardConfig aktywny — seedery na profilu 'prod' zablokowane.");
    }
}
