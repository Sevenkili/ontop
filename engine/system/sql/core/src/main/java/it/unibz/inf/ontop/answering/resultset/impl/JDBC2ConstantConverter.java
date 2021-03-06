package it.unibz.inf.ontop.answering.resultset.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.answering.reformulation.IRIDictionary;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.exception.OntopResultConversionException;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;

import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static it.unibz.inf.ontop.answering.resultset.impl.JDBC2ConstantConverter.System.DEFAULT;
import static it.unibz.inf.ontop.answering.resultset.impl.JDBC2ConstantConverter.System.MSSQL;
import static it.unibz.inf.ontop.answering.resultset.impl.JDBC2ConstantConverter.System.ORACLE;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class JDBC2ConstantConverter {

    enum System {ORACLE, MSSQL, DEFAULT}

    private static final DecimalFormat formatter = new DecimalFormat("0.0###E0");

    private static ImmutableMap<System, ImmutableList<DateFormat>> system2DateFormats = buildDateFormatMap();

    private AtomicInteger bnodeCounter;
    private IRIDictionary iriDictionary;

    private final Map<String, String> bnodeMap;

    private final System system;

    static {
        DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
        symbol.setDecimalSeparator('.');
        formatter.setDecimalFormatSymbols(symbol);
        system2DateFormats = buildDateFormatMap();
    }


    private static ImmutableMap<System,ImmutableList<DateFormat>> buildDateFormatMap() {
        return ImmutableMap.of(

                DEFAULT, ImmutableList.<DateFormat>builder()
                        .add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)) // ISO with 'T'
                        .add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)) // ISO without 'T'
                        .add(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)) // ISO without time
                        .add(new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)) // another common case
                        .build(),
                ORACLE, ImmutableList.<DateFormat>builder()
                        //For ORACLE driver 12.1.0.2 (TODO: check earlier versions)
                        .add(new SimpleDateFormat("dd-MMM-yy HH.mm.ss.SSSSSSSSS aa XXX", Locale.ENGLISH))
                        //TODO: check whether the following formats are necessary
                        .add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH))
                        .add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH))
                        .add(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH))
                        .add(new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH))
                        .build(),
                MSSQL, ImmutableList.<DateFormat>builder()
                        .add(new SimpleDateFormat("MMM dd yyyy hh:mmaa", Locale.ENGLISH))
                        //TODO: check whether the following formats are necessary
                        .add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH))
                        .add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH))
                        .add(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH))
                        .add(new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH))
                        .build()
        );
    }

    public JDBC2ConstantConverter(DBMetadata dbMetadata, Optional<IRIDictionary> iriDictionary) {
        this.iriDictionary = iriDictionary.orElse(null);
        String vendor = dbMetadata.getDriverName();
        system = identifySystem(vendor);
        this.bnodeCounter = new AtomicInteger();
        bnodeMap = new HashMap<>(1000);
    }

    private System identifySystem(String vendor) {
        if(vendor.contains("Oracle"))
            return ORACLE;
        if(vendor.contains("SQL Server"))
            return MSSQL;
        return DEFAULT;
    }

    public Constant getConstantFromJDBC(MainTypeLangValues cell) throws OntopResultConversionException {

        Object value = "";
        String stringValue;

        try {
            value = cell.getMainValue();

            if (value == null) {
                return null;
            }
            stringValue = String.valueOf(value);

            int t = cell.getTypeValue();
            Predicate.COL_TYPE type = Predicate.COL_TYPE.getQuestType(t);
            if (type == null)
                throw new OntopResultConversionException("typeCode unknown: " + t);

            switch (type) {
                case NULL:
                    return null;

                case OBJECT:
                    if (iriDictionary != null) {
                        try {
                            Integer id = Integer.parseInt(stringValue);
                            stringValue = iriDictionary.getURI(id);
                        } catch (NumberFormatException e) {
                            // If its not a number, then it has to be a URI, so
                            // we leave realValue as it is.
                        }
                    }
                    return TERM_FACTORY.getConstantURI(stringValue.trim());

                case BNODE:
                    String scopedLabel = this.bnodeMap.get(stringValue);
                    if (scopedLabel == null) {
                        scopedLabel = "b" + bnodeCounter.getAndIncrement();
                        bnodeMap.put(stringValue, scopedLabel);
                    }
                    return TERM_FACTORY.getConstantBNode(scopedLabel);

                /**
                 * TODO: the language tag should be reserved to LITERAL_LANG
                 */
                case LITERAL:
                case LITERAL_LANG:
                    // The constant is a literal, we need to find if its
                    // rdfs:Literal or a normal literal and construct it
                    // properly.
                    String language = cell.getLangValue();
                    if (language == null || language.trim().equals(""))
                        return TERM_FACTORY.getConstantLiteral(stringValue);
                    else
                        return TERM_FACTORY.getConstantLiteral(stringValue, language);

                case BOOLEAN:
                    // TODO(xiao): more careful
                    boolean bvalue = Boolean.valueOf(stringValue);
                    //boolean bvalue = (Boolean)value;
                    return TERM_FACTORY.getBooleanConstant(bvalue);

                case DOUBLE:
                    double d = Double.valueOf(stringValue);
                    String s = formatter.format(d); // format name into correct double representation
                    return TERM_FACTORY.getConstantLiteral(s, type);

                case INT:
                    return TERM_FACTORY.getConstantLiteral(stringValue, type);

                case LONG:
                case UNSIGNED_INT:
                    return TERM_FACTORY.getConstantLiteral(stringValue, type);

                case INTEGER:
                case NEGATIVE_INTEGER:
                case NON_NEGATIVE_INTEGER:
                case POSITIVE_INTEGER:
                case NON_POSITIVE_INTEGER:
                    /**
                     * Sometimes the integer may have been converted as DECIMAL, FLOAT or DOUBLE
                     */
                    int dotIndex = stringValue.indexOf(".");
                    String integerString = dotIndex >= 0
                            ? stringValue.substring(0, dotIndex)
                            : stringValue;
                    return TERM_FACTORY.getConstantLiteral(integerString, type);

                case DATETIME:
                    return TERM_FACTORY.getConstantLiteral(
                            convertDatetimeString(value),
                            Predicate.COL_TYPE.DATETIME
                    );

                case DATETIME_STAMP:
                    stringValue = system.equals(ORACLE)?
                            convertDatetimeString(stringValue):
                            stringValue;
                return TERM_FACTORY.getConstantLiteral(
                        stringValue.replaceFirst(" ", "T").replaceAll(" ", ""),
                        Predicate.COL_TYPE.DATETIME_STAMP
                );

                case DATE:
                    if(system.equals(ORACLE)) {
                        try {
                            DateFormat df = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
                            java.util.Date date = df.parse(stringValue);
                            stringValue = date.toString();
                        } catch (ParseException e) {
                            throw new OntopResultConversionException(e);
                        }
                    }
                    return TERM_FACTORY.getConstantLiteral(stringValue, Predicate.COL_TYPE.DATE);

                case TIME:
                    return TERM_FACTORY.getConstantLiteral(stringValue.replace(' ', 'T'), Predicate.COL_TYPE.TIME);

                default:
                    return TERM_FACTORY.getConstantLiteral(stringValue, type);
            }
        } catch (IllegalArgumentException e) {
            Throwable cause = e.getCause();
            if (cause instanceof URISyntaxException) {
                OntopResultConversionException ex = new OntopResultConversionException(
                        "Error creating an object's URI. This is often due to mapping with URI templates that refer to "
                                + "columns in which illegal values may appear, e.g., white spaces and special characters.\n"
                                + "To avoid this error do not use these columns for URI templates in your mappings, or process "
                                + "them using SQL functions (e.g., string replacement) in the SQL queries of your mappings.\n\n"
                                + "Note that this last option can be bad for performance, future versions of Quest will allow to "
                                + "string manipulation functions in URI templates to avoid these performance problems.\n\n"
                                + "Detailed message: " + cause.getMessage());
                ex.setStackTrace(e.getStackTrace());
                throw ex;
            } else {
                OntopResultConversionException ex = new OntopResultConversionException("Quest couldn't parse the data value to Java object: " + value + "\n"
                        + "Please review the mapping rules to have the datatype assigned properly.");
                ex.setStackTrace(e.getStackTrace());
                throw ex;
            }
        } catch (Exception e) {
            throw new OntopResultConversionException(e);
        }
    }

    private String convertDatetimeString(Object value) throws OntopResultConversionException {
        java.util.Date dateValue = null;

        if (value instanceof java.util.Date) {
            // If JDBC gives us proper Java object, we are good
            dateValue = (java.util.Date) value;
        } else {
            // Otherwise, we need to deal with possible String representation of datetime
            String stringValue = String.valueOf(value);
            for (DateFormat format : system2DateFormats.get(system)) {
                try {
                    dateValue = format.parse(stringValue);
                    break;
                } catch (ParseException e) {
                    // continue with the next try
                }
            }

            if (dateValue == null) {
                throw new OntopResultConversionException("unparseable datetime: " + stringValue);
            }
        }
        final Timestamp ts = new Timestamp(dateValue.getTime());
        return ts.toString().replace(' ', 'T');
    }
}
