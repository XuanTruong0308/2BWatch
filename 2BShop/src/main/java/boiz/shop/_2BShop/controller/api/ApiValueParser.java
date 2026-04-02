package boiz.shop._2BShop.controller.api;

public final class ApiValueParser {

    private ApiValueParser() {
    }

    public static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        return Integer.parseInt(text);
    }

    public static String asString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return Boolean.parseBoolean(value.toString());
    }
}
