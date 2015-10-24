package com.ramussoft.eval;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import com.ramussoft.localefix.DecimalFormatWithFix;

public class Eval {

    public static DecimalFormatWithFix format = new DecimalFormatWithFix();

    public static DateFormat dateFormat = DateFormat
            .getDateInstance(DateFormat.SHORT);

    private static Hashtable<String, Class<? extends Part>> partTypes;

    private static final String PRIORITY = "^ *x/ +x- ! =x<x>x>=x=>x<=x=<x!=x<> & | ?x:";// x
    // -
    // only
    // a
    // divider.

    public static final String OPERATIONS = "^*/+-&|=<>!?:";

    private static final int NUMBER = 10;

    private static final int OPERATION = 1;

    private static final int OPEN = 2;

    private static final int CLOSE = 3;

    private static final int TEXT = 4;

    private static final int SUPER_TEXT = 5;

    private static final int SUPER_END = 6;

    private static final int VALUE = 7;

    private static final int FUNCTION = 8;

    private static final int CLOSE_PARAM = 9;

    private static int LEVEL_COUNT;

    private static Hashtable<String, Integer> priorities;

    static {
        partTypes = new Hashtable<String, Class<? extends Part>>();
        partTypes.put("+", PlusPart.class);
        partTypes.put("-", MinusPart.class);
        partTypes.put("*", MultPart.class);
        partTypes.put("/", DivPart.class);
        partTypes.put("^", PowerPart.class);

        partTypes.put(">", MorePart.class);
        partTypes.put("<", LessPart.class);
        partTypes.put("=", EqualsPart.class);

        partTypes.put("&", AndPart.class);
        partTypes.put("|", OrPart.class);

        partTypes.put("!", NotPart.class);

        partTypes.put("<=", LessEqualsPart.class);
        partTypes.put("=<", LessEqualsPart.class);

        partTypes.put(">=", MoreEqualsPart.class);
        partTypes.put("=>", MoreEqualsPart.class);

        partTypes.put("!=", NotEqualsPart.class);
        partTypes.put("=!", NotEqualsPart.class);
        partTypes.put("<>", NotEqualsPart.class);

        partTypes.put("?", QuestionPart.class);
        partTypes.put(":", QuestionSPart.class);

        priorities = new Hashtable<String, Integer>();
        fillPriorities();
    }

    private class Pair {

        Pair(String operation, Value value) {
            this.operation = operation;
            this.value = value;
        }

        String operation;

        Value value;
    }

    ;

    private String function;

    private Value value;

    private int position;

    private int type;

    private ArrayList<Eval> childs = new ArrayList<Eval>();

    private ArrayList<String> childStrings = new ArrayList<String>();

    private ArrayList<ValueValue> values;

    private ArrayList<FunctionPart> functions;

    private final Eval parent;

    private ArrayList<String> uniqueValues;

    private ArrayList<String> uniqueFunctions;

    public Eval(String function) {
        this.function = function;
        this.parent = this;
        this.values = new ArrayList<ValueValue>();
        this.functions = new ArrayList<FunctionPart>();
        compile();
    }

    private Eval(String function, int position, Eval parent) {
        this.function = function;
        this.position = position;
        this.parent = parent;
        compile();
    }

    private static void fillPriorities() {
        StringTokenizer st = new StringTokenizer(PRIORITY, " ");
        int p = 0;
        while (st.hasMoreElements()) {
            String sword = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(sword, "x");
            while (st2.hasMoreElements()) {
                String word = st2.nextToken();
                priorities.put(word, p);
            }
            p++;
        }
        LEVEL_COUNT = p + 1;
    }

    private int getPriority(String c) {
        if (c == null)
            return LEVEL_COUNT - 1;
        if (priorities.get(c) == null)
            throw new RuntimeException("Unknown operation " + c);
        return priorities.get(c);
    }

    private boolean isNumber(char c) {
        return (c <= '9') && (c >= '0');
    }

    private boolean isNumberSymbol(char c) {
        if (isNumber(c))
            return true;
        return (',' == c) || (c == '.') || (c == ' ');
    }

    private void compile() {
        ArrayList<Pair> list = new ArrayList<Pair>();
        String operation = null;

        String next;
        while ((next = next()) != null) {
            switch (type) {
                case NUMBER:
                    try {
                        list.add(new Pair(operation, new ConstValue(new EObject(
                                new Double(format.parseNative(next).doubleValue())))));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                case TEXT:
                    list
                            .add(new Pair(operation, new ConstValue(new EObject(
                                    next))));
                    break;
                case OPERATION:
                    operation = next;
                    break;

                case OPEN:
                    compile();
                    list.add(new Pair(operation, new ParenthesesValue(this.value)));
                    break;
                case CLOSE:
                case CLOSE_PARAM:
                    compileList(list);
                    return;

                case SUPER_END:
                    compileList(list);
                    return;
                case SUPER_TEXT:
                    Part part = new WorkStringPart(childs.toArray(new Eval[childs
                            .size()]), childStrings.toArray(new String[childStrings
                            .size()]));
                    list.add(new Pair(operation, new PartValue(part)));
                    break;

                case VALUE:
                    ValueValue value = new ValueValue(next);
                    Object object = getConstant(next);
                    if ((object == null) && (!"NULL".equals(next)))
                        parent.values.add(value);
                    else
                        value.setValue(new EObject(object));
                    list.add(new Pair(operation, value));
                    break;
                case FUNCTION:
                    ArrayList<Value> values = new ArrayList<Value>();

                    this.value = null;

                    do {
                        int pos = position;
                        compile();
                        if (pos == position)
                            throw new RuntimeException("Function is wrong.");
                        if (this.value != null)
                            values.add(this.value);
                    } while (type != CLOSE);

                    FunctionPart functionPart = new FunctionPart(values
                            .toArray(new Value[values.size()]), next);
                    list.add(new Pair(operation, new PartValue(functionPart)));
                    Function function = FunctionFactory.getFunction(functionPart
                            .getName());
                    if (function == null)
                        functions.add(functionPart);
                    else
                        functionPart.setFunction(function);
                    break;

            }
        }

        type = 0;
        compileList(list);
    }

    private Object getConstant(String next) {
        if (next.equals("PI"))
            return Math.PI;
        if (next.equals("EXP"))
            return Math.E;
        return null;
    }

    private void compileList(ArrayList<Pair> list) {
        if (list.size() > 1) {

            for (int level = 0; level < LEVEL_COUNT; level++) {
                int i = 1;

                while (i < list.size()) {
                    Pair pair = list.get(i);
                    if (getPriority(pair.operation) == level) {
                        ArrayList<Value> values = new ArrayList<Value>();
                        values.add(list.get(i - 1).value);
                        int j = i;
                        while (i < list.size()) {
                            Pair p = list.get(i);
                            if (p.operation.charAt(0) != pair.operation
                                    .charAt(0))
                                break;
                            values.add(p.value);
                            i++;
                        }
                        i--;
                        Part part = createPart(pair.operation, values
                                .toArray(new Value[values.size()]));
                        list.get(j - 1).value = new PartValue(part);
                        for (int k = i; k >= j; k--) {
                            i--;
                            list.remove(k);
                        }
                    }
                    i++;
                }
            }
        }
        if (list.size() == 1) {
            Pair pair = list.get(0);
            if (pair.operation == null) {
                this.value = pair.value;
            } else {
                Part p = createPart(pair.operation, new Value[]{
                        new ConstValue(new EObject(null)), pair.value});
                this.value = new PartValue(p);
            }
            if (type == CLOSE)
                this.value = new PValue(this.value);

        }
    }

    private String next() {
        if (position >= function.length())
            return null;

        StringBuffer sb = new StringBuffer();

        char s = function.charAt(position);
        if (isNumber(s)) {
            type = NUMBER;
            char c;
            while (isNumberSymbol(c = s)) {
                sb.append(c);
                position++;
                if (position >= function.length())
                    break;
                s = function.charAt(position);
            }
        } else if (OPERATIONS.indexOf(s) >= 0) {
            type = OPERATION;
            position++;
            if (position < function.length()) {
                s = function.charAt(position);
                if (OPERATIONS.indexOf(s) >= 0) {
                    position++;
                    return function.substring(position - 2, position);
                }
            }
            return function.substring(position - 1, position);
        } else if (s == '(') {
            type = OPEN;
            position++;
            return "(";
        } else if (s == ')') {
            type = CLOSE;
            position++;
            return ")";
        } else if (s == ';') {
            type = CLOSE_PARAM;
            position++;
            return ";";
        } else if (s == '\\') {
            position++;
            s = function.charAt(position);
            sb.append(s);
            position++;
        } else if (s == '\'') {
            position++;
            while ((s = function.charAt(position)) != '\'') {
                if (s == '\\') {
                    position++;
                    s = function.charAt(position);
                }
                sb.append(s);
                position++;
            }
            position++;
            type = TEXT;
        } else if (s == '\"') {
            position++;
            int last = position;
            childs.clear();
            childStrings.clear();
            while ((s = function.charAt(position)) != '\"') {
                if (s == '\\') {
                    position++;
                    s = function.charAt(position);
                } else if ((s == '#')
                        && ((function.charAt(position + 1) == '{'))) {
                    childStrings.add(function.substring(last, position));
                    Eval eval = new Eval(function, position + 2, parent);
                    childs.add(eval);
                    position = eval.position;
                    last = position + 1;
                }
                position++;
            }
            childStrings.add(function.substring(last, position));
            position++;
            type = SUPER_TEXT;
        } else if (s == '}') {
            type = SUPER_END;
        } else if ((Character.isLetter(s)) || (s == '_')) {
            while ((position < function.length())
                    && (isLetterOrDigitOr_(s = function.charAt(position)))) {
                sb.append(s);
                position++;
            }

            if (((position == function.length()))
                    || (function.charAt(position) != '(')) {
                type = VALUE;
            } else {
                position++;
                type = FUNCTION;
            }
        } else if (s == '[') {
            while (true) {
                s = function.charAt(position);
                while ((s == '\\') && (position < function.length())) {
                    sb.append(s);
                    position++;
                    if (position < function.length())
                        sb.append(function.charAt(position));
                    position++;
                    if (position < function.length())
                        s = function.charAt(position);
                    break;
                }
                sb.append(s);
                position++;
                if ((position >= function.length()) || (s == ']'))
                    break;
            }
            type = VALUE;
        } else {
            throw new RuntimeException("Unknow character \"" + s + "\"");
        }
        return sb.toString();
    }

    public static boolean isLetterOrDigitOr_(char s) {
        return (s == '_') || (s == ' ') || (s == '.')
                || (Character.isLetterOrDigit(s));
    }

    public EObject calculate() {
        if (value == null)
            return new EObject(null);
        return value.get();
    }

    private Part createPart(String operation, Value[] values) {
        Class<? extends Part> clazz = partTypes.get(operation);
        try {
            Part part = clazz.getConstructor(Value[].class).newInstance(
                    (Object) values);
            return part;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getValues() {
        if (uniqueValues == null) {
            uniqueValues = new ArrayList<String>();
            for (ValueValue v : values) {
                String name = v.getName();
                if (uniqueValues.indexOf(name) < 0) {
                    uniqueValues.add(name);
                }
            }
        }
        return uniqueValues.toArray(new String[uniqueValues.size()]);
    }

    public void setValue(String name, EObject value) {
        for (ValueValue v : values) {
            if (v.getName().equals(name)) {
                v.setValue(value);
            }
        }
    }

    public String[] getFunctions() {
        if (uniqueFunctions == null) {
            uniqueFunctions = new ArrayList<String>();
            for (FunctionPart f : functions) {
                String name = f.getName();
                if (uniqueFunctions.indexOf(name) < 0) {
                    uniqueFunctions.add(name);
                }
            }
        }
        return uniqueFunctions.toArray(new String[uniqueFunctions.size()]);
    }

    public void setFunction(String name, Function function) {
        for (FunctionPart f : functions) {
            if (f.getName().equals(name))
                f.setFunction(function);
        }
    }

    public void setValueNewName(String oldName, String newName) {
        for (ValueValue v : values) {
            if (v.getName().equals(oldName)) {
                v.setName(newName);
            }
        }
        this.uniqueValues = null;
    }

    public boolean replaceValueNames(Replacementable r) {

        boolean res = false;
        for (ValueValue v : values) {
            String nName = r.getNewName(v.getName());
            if (nName != null) {
                v.setName(nName);
                res = true;
            }
        }
        this.uniqueValues = null;
        return res;
    }

    public boolean replaceFunctionNames(Replacementable r) {
        boolean res = false;
        for (FunctionPart fp : functions) {
            String nName = r.getNewName(fp.getName());
            if (nName != null) {
                fp.setName(nName);
                res = true;
            }
        }
        this.uniqueFunctions = null;
        return res;
    }

    public void setFunctionNewName(String oldName, String newName) {
        for (FunctionPart fp : functions) {
            if (fp.getName().equals(oldName)) {
                fp.setName(newName);
            }
        }
        this.uniqueFunctions = null;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        this.value.fill(sb);

        return sb.toString();
    }

    public boolean isValuePresent(String v) {
        for (String value : getValues())
            if (value.equals(v))
                return true;
        return false;
    }

    public void fillEmpty() {
        for (ValueValue v : values) {
            if (v.getValue() == null) {
                v.setValue(new EObject(null));
            }
        }
    }

    public List<ValueValue> getValueHolders() {
        return values;
    }
}
