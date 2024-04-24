package com.vasc;

import java.util.regex.Pattern;

public class ArgumentParser {

    static private final Pattern integerPattern = Pattern.compile("-?\\d+");
    static private final Pattern realPattern = Pattern.compile("-?\\d+[.]\\d+");
    static private final Pattern booleanPattern = Pattern.compile("false|true");

    static Object[] parse(String[] args) {
        var len = args.length;
        var res = new Object[len];
        for (int i = 0; i < len; i++) {
            var arg = args[i];
            if (integerPattern.matcher(arg).matches()) {
                res[i] = new Integer(Long.parseLong(arg));
            } else if (realPattern.matcher(arg).matches()) {
                res[i] = new Real(Double.parseDouble(arg));
            } else if (booleanPattern.matcher(arg).matches()) {
                res[i] = new Boolean(java.lang.Boolean.parseBoolean(arg));
            } else {
                throw new IllegalArgumentException("unable to parse argument: " + arg);
            }
        }
        return res;
    }

}
