package edu.itc.salesreport.engine;

import edu.itc.salesreport.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public final class DefaultSalesAggregator implements SalesAggregator {
    @Override
    public BranchSummary summarize(String branch, List<SaleTransaction> rows) {
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal discounts = BigDecimal.ZERO;
        Set<String> receipts = new HashSet<>();
        Map<String, BigDecimal> byCategory = new TreeMap<>();
        Map<PaymentMethod, BigDecimal> byPayment = new EnumMap<>(PaymentMethod.class);
        Map<String, BigDecimal> bySku = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        Map<String, Long> quantities = new HashMap<>();

        for (SaleTransaction t : rows) {
            BigDecimal r = t.revenue();
            revenue = revenue.add(r);
            discounts = discounts.add(t.discount());
            receipts.add(t.receiptNo());
            byCategory.merge(t.category(), r, BigDecimal::add);
            byPayment.merge(t.paymentMethod(), r, BigDecimal::add);
            bySku.merge(t.sku(), r, BigDecimal::add);
            names.putIfAbsent(t.sku(), t.productName());
            quantities.merge(t.sku(), (long) t.quantity(), Long::sum);
        }
        List<ProductTotal> top = bySku.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(10)
                .map(e -> new ProductTotal(e.getKey(), names.get(e.getKey()),
                        e.getValue(), quantities.get(e.getKey())))
                .toList();

        return new BranchSummary(branch, revenue.setScale(2, RoundingMode.HALF_UP),
                discounts.setScale(2, RoundingMode.HALF_UP), receipts.size(),
                byCategory, top, byPayment);
    }
}