package edu.itc.salesreport.web;

import edu.itc.salesreport.engine.DefaultSalesAggregator;
import edu.itc.salesreport.engine.SalesAggregator;
import edu.itc.salesreport.ingest.CsvMonthReader;
import edu.itc.salesreport.ingest.CsvTransactionParser;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Wires the unchanged sales-report classes as Spring beans. */
@SpringBootApplication
public class SalesWebApp {

    public static void main(String[] args) {
        SpringApplication.run(SalesWebApp.class, args);
    }

    @Bean
    CsvMonthReader csvMonthReader() {
        return new CsvMonthReader(new CsvTransactionParser());
    }

    @Bean
    SalesAggregator salesAggregator() {
        return new DefaultSalesAggregator();
    }

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Asia/Phnom_Penh"));
    }
}
