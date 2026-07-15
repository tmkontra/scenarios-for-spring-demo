package io.github.tmkontra.scenarios.demo.domain;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.math.BigInteger;

public record ReportView(
        @Nonnull
        String status,
        @Nullable
        ReportData data
) {
    public record ReportData(BigInteger value) {
    }
}
