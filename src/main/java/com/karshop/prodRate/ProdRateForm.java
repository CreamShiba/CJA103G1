package com.karshop.prodRate;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ProdRateForm {
    @Valid
    private List<ProdRate> rates;
    // getter, setter
    public List<ProdRate> getRates() { return rates; }
    public void setRates(List<ProdRate> rates) { this.rates = rates; }
}
