package com.navrotskyi.trippyapi.repository;

import com.navrotskyi.trippyapi.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    
}