package org.hss.sny.sooryanamaskarayagnya.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> STATS = new ArrayList<DummyItem>();
    public static List<DummyItem> SOOCHANA = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> STATS_MAP = new HashMap<String, DummyItem>();
    public static Map<String, DummyItem> SOOCHANA_MAP = new HashMap<String, DummyItem>();

    static {
        // Add 3 sample items.
        addStats(new DummyItem("1", "Totals"));
        addStats(new DummyItem("2", "Top participants"));
        // addStats(new DummyItem("3", "Top groups"));

        addSoochana(new DummyItem("1", "HSS announces the Eighth annual \"Health for Humanity Yogathon\" or \"Surya Namaskar Yajna\". The Yogathon dates are from January 17th to February 1st, 2015."));
        addSoochana(new DummyItem("2", "HSS received Proclamation for 2015 Health for Humanity  Yogathon from City of Newark CA. "));
    }

    private static void addStats(DummyItem item) {
        STATS.add(item);
        STATS_MAP.put(item.id, item);
    }
    private static void addSoochana(DummyItem item) {
        SOOCHANA.add(item);
        SOOCHANA_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
