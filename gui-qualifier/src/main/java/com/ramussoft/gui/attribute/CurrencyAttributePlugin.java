package com.ramussoft.gui.attribute;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.CurrencyPropertyPersistent;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class CurrencyAttributePlugin extends DoubleAttributePlugin {

    public static final NumberFormat currentcyFormat;

    private static Currency[] currencies;

    private static Hashtable<String, String> ht = new Hashtable<String, String>();

    static {
        ht.put("ADP", "020");
        ht.put("AED", "784");
        ht.put("AFA", "004");
        ht.put("ALL", "008");
        ht.put("AMD", "051");
        ht.put("ANG", "532");
        ht.put("AON", "024");
        ht.put("AOR", "982");
        ht.put("ARS", "032");
        ht.put("ATS", "040");
        ht.put("AUD", "036");
        ht.put("AWG", "533");
        ht.put("AZM", "031");
        ht.put("BAM", "977");
        ht.put("BBD", "052");
        ht.put("BDT", "050");
        ht.put("BEF", "056");
        ht.put("BGL", "100");
        ht.put("BGN", "975");
        ht.put("BHD", "048");
        ht.put("BIF", "108");
        ht.put("BMD", "060");
        ht.put("BND", "096");
        ht.put("BRL", "986");
        ht.put("BSD", "044");
        ht.put("BTN", "064");
        ht.put("BWP", "072");
        ht.put("BYR", "974");
        ht.put("BZD", "084");
        ht.put("CAD", "124");
        ht.put("CDF", "976");
        ht.put("CHF", "756");
        ht.put("CLF", "990");
        ht.put("CLP", "152");
        ht.put("CNY", "156");
        ht.put("COP", "170");
        ht.put("CRC", "188");
        ht.put("CUP", "192");
        ht.put("CVE", "132");
        ht.put("CYP", "196");
        ht.put("CZK", "203");
        ht.put("DEM", "280");
        ht.put("DJF", "262");
        ht.put("DKK", "208");
        ht.put("DOP", "214");
        ht.put("DZD", "012");
        ht.put("ECS", "218");
        ht.put("ECV", "983");
        ht.put("EEK", "233");
        ht.put("EGP", "818");
        ht.put("ERN", "232");
        ht.put("ESP", "724");
        ht.put("ETB", "230");
        ht.put("EUR", "978");
        ht.put("FIM", "246");
        ht.put("FJD", "242");
        ht.put("FKP", "238");
        ht.put("FRF", "250");
        ht.put("GBP", "826");
        ht.put("GEL", "981");
        ht.put("GHC", "288");
        ht.put("GIP", "292");
        ht.put("GMD", "270");
        ht.put("GNF", "324");
        ht.put("GRD", "300");
        ht.put("GTQ", "320");
        ht.put("GWP", "624");
        ht.put("GYD", "328");
        ht.put("HKD", "344");
        ht.put("HNL", "340");
        ht.put("HRK", "191");
        ht.put("HTG", "332");
        ht.put("HUF", "348");
        ht.put("IDR", "360");
        ht.put("IEP", "372");
        ht.put("ILS", "376");
        ht.put("INR", "356");
        ht.put("IQD", "368");
        ht.put("IRR", "364");
        ht.put("ISK", "352");
        ht.put("ITL", "380");
        ht.put("JMD", "388");
        ht.put("JOD", "400");
        ht.put("JPY", "392");
        ht.put("KES", "404");
        ht.put("KGS", "417");
        ht.put("KHR", "116");
        ht.put("KMF", "174");
        ht.put("KPW", "408");
        ht.put("KRW", "410");
        ht.put("KWD", "414");
        ht.put("KYD", "136");
        ht.put("KZT", "398");
        ht.put("LAK", "418");
        ht.put("LBP", "422");
        ht.put("LKR", "144");
        ht.put("LRD", "430");
        ht.put("LSL", "426");
        ht.put("LTL", "440");
        ht.put("LUF", "442");
        ht.put("LVL", "428");
        ht.put("LYD", "434");
        ht.put("MAD", "504");
        ht.put("MDL", "498");
        ht.put("MGF", "450");
        ht.put("MKD", "807");
        ht.put("MMK", "104");
        ht.put("MNT", "496");
        ht.put("MOP", "446");
        ht.put("MRO", "478");
        ht.put("MTL", "470");
        ht.put("MUR", "480");
        ht.put("MVR", "462");
        ht.put("MWK", "454");
        ht.put("MXN", "484");
        ht.put("MXV", "979");
        ht.put("MYR", "458");
        ht.put("MZM", "508");
        ht.put("NAD", "516");
        ht.put("NGN", "566");
        ht.put("NIO", "558");
        ht.put("NLG", "528");
        ht.put("NOK", "578");
        ht.put("NPR", "524");
        ht.put("NZD", "554");
        ht.put("OMR", "512");
        ht.put("PAB", "590");
        ht.put("PEN", "604");
        ht.put("PGK", "598");
        ht.put("PHP", "608");
        ht.put("PKR", "586");
        ht.put("PLN", "985");
        ht.put("PTE", "620");
        ht.put("PYG", "600");
        ht.put("QAR", "634");
        ht.put("ROL", "642");
        ht.put("RUB", "643");
        ht.put("RUR", "810");
        ht.put("RWF", "646");
        ht.put("SAR", "682");
        ht.put("SBD", "090");
        ht.put("SCR", "690");
        ht.put("SDD", "736");
        ht.put("SEK", "752");
        ht.put("SGD", "702");
        ht.put("SHP", "654");
        ht.put("SIT", "705");
        ht.put("SKK", "703");
        ht.put("SLL", "694");
        ht.put("SOS", "706");
        ht.put("SRG", "740");
        ht.put("STD", "678");
        ht.put("SVC", "222");
        ht.put("SYP", "760");
        ht.put("SZL", "748");
        ht.put("THB", "764");
        ht.put("TJR", "762");
        ht.put("TJS", "972");
        ht.put("TMM", "795");
        ht.put("TND", "788");
        ht.put("TOP", "776");
        ht.put("TPE", "626");
        ht.put("TRL", "792");
        ht.put("TTD", "780");
        ht.put("TWD", "901");
        ht.put("TZS", "834");
        ht.put("UAH", "980");
        ht.put("UGX", "800");
        ht.put("USD", "840");
        ht.put("USN", "997");
        ht.put("USS", "998");
        ht.put("UYU", "858");
        ht.put("UZS", "860");
        ht.put("VEB", "862");
        ht.put("VND", "704");
        ht.put("VUV", "548");
        ht.put("WST", "882");
        ht.put("XAF", "950");
        ht.put("XAG", "961");
        ht.put("XAU", "959");
        ht.put("XBA", "955");
        ht.put("XBB", "956");
        ht.put("XBC", "957");
        ht.put("XBD", "958");
        ht.put("XCD", "951");
        ht.put("XDR", "960");
        ht.put("XOF", "952");
        ht.put("XPD", "964");
        ht.put("XPF", "953");
        ht.put("XPT", "962");
        ht.put("XTS", "963");
        ht.put("XXX", "999");
        ht.put("YER", "886");
        ht.put("YUM", "891");
        ht.put("ZAL", "991");
        ht.put("ZAR", "710");
        ht.put("ZMK", "894");
        ht.put("ZRN", "180");
        ht.put("ZWD", "716");

        if (Locale.getDefault().getLanguage().equals("uk")) {
            // java bug with UK locale.
            currentcyFormat = new DecimalFormat(
                    "###,###.## \u0433\u0440\u043d\'.\'");
            ((DecimalFormat) currentcyFormat)
                    .setDecimalSeparatorAlwaysShown(false);
            DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
            newSymbols.setGroupingSeparator(' ');
            newSymbols.setDecimalSeparator(',');
            ((DecimalFormat) currentcyFormat)
                    .setDecimalFormatSymbols(newSymbols);
        } else {
            currentcyFormat = NumberFormat.getCurrencyInstance();
        }
    }

    @Override
    protected NumberFormat getFormat() {
        return currentcyFormat;
    }

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Currency");
    }

    public static Currency[] getCurrencies() {
        if (currencies == null) {
            List<Currency> currs = new ArrayList<Currency>();
            for (String code : ht.keySet()) {
                try {
                    Currency currency = Currency.getInstance(code);
                    currs.add(currency);
                } catch (Exception e) {
                }
            }
            currencies = currs.toArray(new Currency[currs.size()]);
            Arrays.sort(currencies, new Comparator<Currency>() {
                @Override
                public int compare(Currency o1, Currency o2) {
                    return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
                }
            });
        }
        return currencies;
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        CurrencyPropertyPersistent p = null;
        if (attribute.getId() >= 0)
            p = (CurrencyPropertyPersistent) engine.getAttribute(null,
                    attribute);
        if ((p != null) && (p.getCode() != null)) {

            final DecimalFormat format;
            if ((p.getCode().equals("UAH"))
                    && (Locale.getDefault().getLanguage().equals("uk")))
                format = new DecimalFormat("###,###.## \u0433\u0440\u043d\'.\'");
            else {
                Currency currency = Currency.getInstance(p.getCode());
                String s;
                if (p.getCode().equals("EUR"))
                    s = currency.getSymbol(Locale.UK);
                else if (p.getCode().equals("USD"))
                    s = currency.getSymbol(Locale.US);
                else
                    s = currency.getSymbol();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (c == '.')
                        sb.append("\'.\'");
                    else if (c == ',')
                        sb.append("\',\'");
                    else if (c == '#')
                        sb.append("\'#\'");
                    else
                        sb.append(c);
                }

                s = sb.toString();

                String b = "###,###";
                String e = ".##";
                int digits = currency.getDefaultFractionDigits();
                if (digits > 0) {
                    e = ".";
                    while (digits > 0) {
                        digits--;
                        e += "#";
                    }

                } else
                    e = "";

                if (s.length() == 1)
                    format = new DecimalFormat(s + " " + b + e);
                else
                    format = new DecimalFormat(b + e + " " + s);
            }

            if (Locale.getDefault().getLanguage().equals("uk")) {
                format.setDecimalSeparatorAlwaysShown(false);
                DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
                newSymbols.setGroupingSeparator(' ');
                newSymbols.setDecimalSeparator(',');
                format.setDecimalFormatSymbols(newSymbols);
            }

            return new DefaultTableCellRenderer() {
                /**
                 *
                 */
                private static final long serialVersionUID = -7922052040779840252L;

                {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }

                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    Component component = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    if (value != null)
                        ((JLabel) component).setText(format.format(value));
                    return component;
                }
            };
        }
        return super.getTableCellRenderer(engine, rules, attribute);
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new AttributePreferenciesEditor() {

            private JComboBox comboBox = new JComboBox();

            @Override
            public JComponent createComponent(Attribute attribute,
                                              Engine engine, AccessRules accessRules) {
                CurrencyPropertyPersistent p = null;
                if (attribute != null)
                    p = (CurrencyPropertyPersistent) engine.getAttribute(null,
                            attribute);

                comboBox.addItem(GlobalResourcesManager.getString("ByDefault"));
                int i = 1;
                int index = 0;
                for (Currency currency : getCurrencies()) {
                    comboBox.addItem(currency);
                    if ((p != null) && (p.getCode() != null)
                            && (p.getCode().equals(currency.getCurrencyCode())))
                        index = i;
                    i++;
                }

                comboBox.setSelectedIndex(index);

                double[][] size = {
                        {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                        {5, TableLayout.MINIMUM, 5}};

                TableLayout layout = new TableLayout(size);

                JPanel panel = new JPanel(layout);

                panel.add(new JLabel(GlobalResourcesManager
                        .getString("Attribute.CurrencyCode")), "1,1");
                panel.add(comboBox, "3,1");

                return panel;
            }

            @Override
            public boolean canApply() {
                return true;
            }

            @Override
            public void apply(Attribute attribute, Engine engine,
                              AccessRules accessRules) {
                if (comboBox.getSelectedIndex() == 0) {
                    engine.setAttribute(null, attribute, null);
                } else {
                    CurrencyPropertyPersistent p = new CurrencyPropertyPersistent();
                    p.setCode(((Currency) comboBox.getSelectedItem())
                            .getCurrencyCode());
                    engine.setAttribute(null, attribute, p);
                }

            }
        };
    }
}
