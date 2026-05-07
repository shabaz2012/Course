

/*
We are developing a stock trading data management software that tracks the prices of different stocks over time and provides useful statistics.

The program includes three classes: `Stock`, `PriceRecord`, and `StockCollection`.

Classes:
* The `Stock` class represents data about a specific stock.
* The `PriceRecord` class holds information about a single price record for a stock.
* The `StockCollection` class manages a collection of price records for a particular stock and provides methods to retrieve useful statistics about the stock's prices.

To begin with, we present you with two tasks:
1-1) Read through and understand the code below. Please take as much time as necessary, and feel free to run the code.
1-2) The test for StockCollection is not passing due to a bug in the code. Make the necessary changes to StockCollection to fix the bug.
*/

/*
2) We want to add a new function called "getBiggestChange" to the StockCollection class. This function calculates and returns the largest change in stock price between any two consecutive days in the price records of a stock along with the dates of the change in a list. For example, let's consider the following price records of a stock:

Price Records:
Price:  110         112         90          105
Date:   2023-06-29  2023-07-01  2023-06-25  2023-07-06

Stock price changes (sorted based on date):
Date:     2023-06-25  ->  2023-06-29  ->  2023-07-01 ->  2023-07-06
Price:        90      ->      110     ->     112     ->     105
Change:              +20              +2             -7

In this case, the biggest change in the stock price was +20, which occurred between 2023-06-25 and 2023-06-29. In this case, the function should return [20, "2023-06-25", "2023-06-29"]

Two days are considered consecutive if there are no other days' data in between them in the price records based on their dates.

To assist you in testing this new function, we have provided the testGetBiggestChange function.
*/


import java.io.*;
import java.util.*;
import org.junit.jupiter.api.Assertions;

class Stock {
    /** Data about a particular stock. */
    String symbol; // String, the symbol of the stock
    String name; // String, the name of the stock

    Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Stock stock = (Stock) other;
        return symbol.equals(stock.symbol) && name.equals(stock.name);
    }
    @Override
    public int hashCode() {
        return java.util.Objects.hash(symbol, name);
    }
}

class PriceRecord {
    /** Data and methods about a single price record of a stock. */
    Stock stock; // Stock object representing the stock
    int price; // int, the price of the stock
    String date; // String, the date of the price record is of the format "YYYY-MM-DD"

    PriceRecord(Stock stock, int price, String date) {
        this.stock = stock;
        this.price = price;
        this.date = date;
    }
}

class StockCollection {
    /**
     * Data for a collection of price records for a particular stock, and methods for
     * getting useful statistics about the stock's prices.
     */
    ArrayList<PriceRecord> priceRecords = new ArrayList<>(); // list of PriceRecord objects, the price records for this particular stock
    Stock stock; // Stock, the Stock this StockCollection is for

    StockCollection(Stock stock) {
        this.stock = stock;
    }

    int getNumPriceRecords() {
        /** Returns the number of PriceRecords in this StockCollection */
        return priceRecords.size();
    }

    void addPriceRecord(PriceRecord priceRecord) {
        /** Adds a PriceRecord to this StockCollection. */
        if (!priceRecord.stock.equals(this.stock)) {
            throw new IllegalArgumentException("PriceRecord's Stock is not the same as the StockCollection's");
        }
        priceRecords.add(priceRecord);
    }

    int getMaxPrice() {
        /** Return the maximum price recorded in this StockCollection. */
        return priceRecords.stream().mapToInt(record -> record.price).max().orElse(-1);
    }

    int getMinPrice() {
        /** Return the minimum price recorded in this StockCollection. */
        return priceRecords.stream().mapToInt(record -> record.price).min().orElse(-1);
    }

    double getAvgPrice() {
        /** Return the average price recorded in this StockCollection. */
        if(priceRecords.isEmpty())
        {
            return -1.0;
        }
        else{
            double total = priceRecords.stream().mapToInt(record -> record.price).sum();
            return total / priceRecords.size();
        }
    }

    Object[] getBiggestChange(){
        if (priceRecords == null || priceRecords.isEmpty()) {
            return null;
        }
        List<PriceRecord> sorted = new ArrayList<>(priceRecords);
        sorted.sort(Comparator.comparing(p -> p.date));
        String startDate="";
        String endDate="";
        int maxChange = Integer.MIN_VALUE;
        boolean isNegative = false;

        for(int i= 1 ; i< sorted.size() ; i++)
        {
            PriceRecord prev = sorted.get(i - 1);
            PriceRecord curr = sorted.get(i);

            int changeAbs = curr.price - prev.price;
            int change = Math.abs(changeAbs);
            if(change > maxChange)
            {
                maxChange = change;
                startDate = prev.date;
                endDate = curr.date;
                isNegative = curr.price < prev.price;
            }
        }
        if (isNegative) {
            maxChange = Math.negateExact(maxChange);
        }
        return new Object[] {maxChange , startDate, endDate};
    }
}

public class SolutionStock {

    public static void main(String[] args) {
        testPriceRecord();
        testStockCollection();
        testGetBiggestChange();
    }

    public static void testPriceRecord() {
        // Test basic PriceRecord functionality
        System.out.println("Running testPriceRecord");
        Stock testStock = new Stock("AAPL", "Apple Inc.");
        PriceRecord testPriceRecord = new PriceRecord(testStock, 100, "2023-07-01");

        Assertions.assertEquals(testPriceRecord.stock, testStock);
        Assertions.assertEquals(testPriceRecord.price, 100);
        Assertions.assertEquals(testPriceRecord.date, "2023-07-01");
    }

    private static StockCollection makeStockCollection(Stock stock, Object[][] priceData) {
        StockCollection stockCollection = new StockCollection(stock);
        for (Object[] priceRecordData : priceData) {
            PriceRecord priceRecord = new PriceRecord(stock, (int) priceRecordData[0], (String) priceRecordData[1]);
            stockCollection.addPriceRecord(priceRecord);
        }
        return stockCollection;
    }

    public static void testStockCollection() {
        System.out.println("Running testStockCollection");
        // Test basic StockCollection functionality
        Stock testStock = new Stock("AAPL", "Apple Inc.");
        StockCollection stockCollection = new StockCollection(testStock);

        Assertions.assertEquals(0, stockCollection.getNumPriceRecords());
        Assertions.assertEquals(-1, stockCollection.getMaxPrice());
        Assertions.assertEquals(-1, stockCollection.getMinPrice());
        Assertions.assertEquals(-1.0, stockCollection.getAvgPrice(), 0.001);

        /*
         * Price Records: Price: 110 112 90 105 Date: 2023-06-29 2023-07-01 2023-06-28
         * 2023-07-06
         */
        Object[][] priceData = { { 110, "2023-06-29" }, { 112, "2023-07-01" }, { 90, "2023-06-28" },
                { 105, "2023-07-06" } };
        testStock = new Stock("AAPL", "Apple Inc.");
        stockCollection = makeStockCollection(testStock, priceData);

        Assertions.assertEquals(priceData.length, stockCollection.getNumPriceRecords());
        Assertions.assertEquals(112, stockCollection.getMaxPrice());
        Assertions.assertEquals(90, stockCollection.getMinPrice());
        Assertions.assertEquals(104.25, stockCollection.getAvgPrice(), 0.1);
    }

    public static void testGetBiggestChange() {
        // Test the getBiggestChange method
        System.out.println("Running testGetBiggestChange");
        Stock testStock = new Stock("AAPL", "Apple Inc.");
        StockCollection stockCollection = new StockCollection(testStock);

        Assertions.assertNull(stockCollection.getBiggestChange());

        /*
         * Price Records: Price: 110 112 90 105 Date: 2023-06-29 2023-07-01 2023-06-25
         * 2023-07-06
         */
        Object[][] priceData = { { 110, "2023-06-29" }, { 112, "2023-07-01" }, { 90, "2023-06-25" },
                { 105, "2023-07-06" } };
        stockCollection = makeStockCollection(testStock, priceData);

        Assertions.assertArrayEquals(new Object[] { 20, "2023-06-25", "2023-06-29" }, stockCollection.getBiggestChange());

        /*
         * Price Records: Price: 200 210 190 180 Date: 2000-01-04 1999-12-30 2000-01-03
         * 2000-01-01
         */
        Object[][] priceData2 = { { 200, "2000-01-04" }, { 210, "1999-12-30" }, { 190, "2000-01-03" },
                { 180, "2000-01-01" } };
        stockCollection = makeStockCollection(testStock, priceData2);

        Assertions.assertArrayEquals(new Object[] { -30, "1999-12-30", "2000-01-01" }, stockCollection.getBiggestChange());
    }
}
