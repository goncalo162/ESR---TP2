package demo;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/** Small local snapshot format; never used on the OTT wire. */
public final class Json
{
    //* === Atributos e Variáveis === *
    private final String text;
    private int offset;



    //* === Construtores === *

    private Json(String text)
    {
        this.text = text;
    }



    //* === Métodos Públicos === *

    public static String write(Object value)
    {
        if(value == null)
            return "null";
        if(value instanceof Optional<?> x)
            return write(x.orElse(null));
        if(value instanceof OptionalLong x)
            return x.isPresent() ? Long.toString(x.getAsLong()) : "null";
        if(value instanceof OptionalDouble x)
            return x.isPresent() ? Double.toString(x.getAsDouble()) : "null";
        if(value instanceof Number || value instanceof Boolean)
            return value.toString();
        if(value instanceof Map<?, ?> map)
        {
            var parts = new ArrayList<String>();
            map.forEach((k, v) -> parts.add(write(k.toString()) + ":" + write(v)));
            return "{" + String.join(",", parts) + "}";
        }
        if(value instanceof Iterable<?> values)
        {
            var parts = new ArrayList<String>();
            for(var item : values)
            {
                parts.add(write(item));
            }
            return "[" + String.join(",", parts) + "]";
        }
        if(value.getClass().isRecord())
        {
            var fields = new LinkedHashMap<String, Object>();
            for(RecordComponent component : value.getClass().getRecordComponents())
            {
                try
                {
                    fields.put(component.getName(), component.getAccessor().invoke(value));
                }catch(ReflectiveOperationException e){
                    throw new IllegalArgumentException("Snapshot accessor: " + component.getName(), e);
                }
            }
            return write(fields);
        }
        var out = new StringBuilder("\"");
        for(char c : value.toString().toCharArray())
        {
            switch(c)
            {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if(c < 32)
                        out.append(String.format("\\u%04x", (int)c));
                    else
                        out.append(c);
            }
        }
        return out.append('"').toString();
    }


    public static Object read(String text)
    {
        var parser = new Json(text);
        Object result = parser.value(0);
        parser.space();
        if(parser.offset != text.length())
            throw new IllegalArgumentException("Trailing JSON data");
        return result;
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value)
    {
        return (Map<String, Object>)value;
    }



    //* === Auxiliar === *

    private void space()
    {
        while(offset < text.length() && Character.isWhitespace(text.charAt(offset)))
        {
            offset++;
        }
    }


    private Object value(int depth)
    {
        if(depth > 64)
            throw new IllegalArgumentException("JSON nesting limit");
        space();
        char start = text.charAt(offset++);
        if(start == '"')
            return string();
        if(start == '{' || start == '[')
        {
            var map = new LinkedHashMap<String, Object>();
            var list = new ArrayList<Object>();
            char end = start == '{' ? '}' : ']';
            space();
            if(text.charAt(offset) == end)
            {
                offset++;
                return start == '{' ? map : list;
            }
            while(true)
            {
                if(start == '{')
                {
                    String key = (String)value(depth + 1);
                    space();
                    if(text.charAt(offset++) != ':')
                        throw new IllegalArgumentException("JSON colon expected");
                    if(map.containsKey(key))
                        throw new IllegalArgumentException("Duplicate JSON key");
                    map.put(key, value(depth + 1));
                }else{
                    list.add(value(depth + 1));
                }
                space();
                char next = text.charAt(offset++);
                if(next == end)
                    return start == '{' ? map : list;
                if(next != ',')
                    throw new IllegalArgumentException("JSON comma expected");
            }
        }
        int begin = offset - 1;
        while(offset < text.length() && ",]} \r\n\t".indexOf(text.charAt(offset)) < 0)
        {
            offset++;
        }
        String token = text.substring(begin, offset);
        return switch(token)
        {
            case "true" -> true;
            case "false" -> false;
            case "null" -> null;
            default -> token.contains(".") || token.contains("E") || token.contains("e")
                    ? (Object)Double.parseDouble(token) : Long.parseLong(token);
        };
    }


    private String string()
    {
        var out = new StringBuilder();
        while(offset < text.length())
        {
            char c = text.charAt(offset++);
            if(c == '"')
                return out.toString();
            if(c == '\\')
            {
                c = text.charAt(offset++);
                switch(c)
                {
                    case 'n': c = '\n'; break;
                    case 'r': c = '\r'; break;
                    case 't': c = '\t'; break;
                    case 'u':
                        c = (char)Integer.parseInt(text.substring(offset, offset + 4), 16);
                        offset += 4;
                        break;
                    case '"', '\\', '/': break;
                    default: throw new IllegalArgumentException("Invalid JSON escape");
                }
            }
            out.append(c);
        }
        throw new IllegalArgumentException("Unterminated JSON string");
    }
}
