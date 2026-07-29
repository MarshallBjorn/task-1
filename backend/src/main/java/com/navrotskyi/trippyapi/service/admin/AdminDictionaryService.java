package com.navrotskyi.trippyapi.service.admin;

import com.navrotskyi.trippyapi.domain.Currency;
import com.navrotskyi.trippyapi.domain.TripRole;
import com.navrotskyi.trippyapi.dto.admin.CurrencyRequest;
import com.navrotskyi.trippyapi.dto.admin.CurrencyResponse;
import com.navrotskyi.trippyapi.dto.admin.TripRoleRequest;
import com.navrotskyi.trippyapi.dto.admin.TripRoleResponse;
import com.navrotskyi.trippyapi.repository.CurrencyRepository;
import com.navrotskyi.trippyapi.repository.TripRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminDictionaryService {

    private final CurrencyRepository currencyRepository;
    private final TripRoleRepository tripRoleRepository;

    public AdminDictionaryService(CurrencyRepository currencyRepository, TripRoleRepository tripRoleRepository) {
        this.currencyRepository = currencyRepository;
        this.tripRoleRepository = tripRoleRepository;
    }

    public CurrencyResponse addCurrency(CurrencyRequest request) {
        String code = request.code().toUpperCase();
        if (currencyRepository.existsById(code)) {
            throw new IllegalArgumentException("[ERR] Currency with code " + code + " already exists.");
        }

        Currency currency = new Currency(code, request.name());
        Currency saved = currencyRepository.save(currency);
        return new CurrencyResponse(saved.getCode(), saved.getName());
    }

    public List<CurrencyResponse> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(c -> new CurrencyResponse(c.getCode(), c.getName()))
                .collect(Collectors.toList());
    }

    public TripRoleResponse addTripRole(TripRoleRequest request) {
        String roleName = request.name().toUpperCase();
        if (tripRoleRepository.findByName(roleName).isPresent()) {
            throw new IllegalArgumentException("[ERR] Role with name " + roleName + " already exists");
        }

        TripRole role = new TripRole(roleName);
        TripRole saved = tripRoleRepository.save(role);
        return new TripRoleResponse(saved.getId(), saved.getName());
    }

    public List<TripRoleResponse> getAllTripRoles() {
        return tripRoleRepository.findAll().stream()
                .map(r -> new TripRoleResponse(r.getId(), r.getName()))
                .collect(Collectors.toList());
    }
}