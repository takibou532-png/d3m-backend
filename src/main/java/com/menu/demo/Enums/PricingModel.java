package com.menu.demo.Enums;

public enum PricingModel {
    MONTHLY_FLAT,   // fixed monthlyprice regardless of attendance
    PER_SESSION     // pricePerSession × sessions marked PRESENT that month
}