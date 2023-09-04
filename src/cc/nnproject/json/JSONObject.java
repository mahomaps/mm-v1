/*
Copyright (c) 2022 Arman Jussupgaliyev

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package cc.nnproject.json;

import java.util.Enumeration;
import java.util.Hashtable;

public class JSONObject extends AbstractJSON {

	private Hashtable table;

	public JSONObject() {
		this.table = new Hashtable();
	}

	public JSONObject(Hashtable table) {
		this.table = table;
	}
	
	public boolean has(String name) {
		return table.containsKey(name);
	}
	
	public Object get(String name) throws JSONException {
		try {
			if (has(name)) {
				Object o = table.get(name);
				if (o instanceof JSONString)
					table.put(name, o = JSON.parseJSON(o.toString()));
				return o;
			}
		} catch (JSONException e) {
			throw e;
		} catch (Exception e) {
		}
		throw new JSONException("No value for name: " + name);
	}
	
	public Object get(String name, Object def) {
		if(!has(name)) return def;
		try {
			return get(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public Object getNullable(String name) {
		return get(name, null);
	}
	
	public String getString(String name) throws JSONException {
		return get(name).toString();
	}
	
	public String getString(String name, String def) {
		try {
			Object o = get(name, def);
			if(o == null || o instanceof String) {
				return (String) o;
			}
			return o.toString();
		} catch (Exception e) {
			return def;
		}
	}
	
	public String getNullableString(String name) {
		return getString(name, null);
	}
	
	public JSONObject getObject(String name) throws JSONException {
		try {
			return (JSONObject) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not object: " + name);
		}
	}
	
	public JSONObject getNullableObject(String name) {
		if(!has(name)) return null;
		try {
			return getObject(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public JSONArray getArray(String name) throws JSONException {
		try {
			return (JSONArray) get(name);
		} catch (ClassCastException e) {
			throw new JSONException("Not array: " + name);
		}
	}
	
	public JSONArray getNullableArray(String name) {
		if(!has(name)) return null;
		try {
			return getArray(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	public int getInt(String name) throws JSONException {
		return (int) JSON.getLong(get(name));
	}
	
	public int getInt(String name, int def) {
		if(!has(name)) return def;
		try {
			return getInt(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public long getLong(String name) throws JSONException {
		return JSON.getLong(get(name));
	}

	public long getLong(String name, long def) {
		if(!has(name)) return def;
		try {
			return getLong(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public double getDouble(String name) throws JSONException {
		return JSON.getDouble(get(name));
	}

	public double getDouble(String name, double def) {
		if(!has(name)) return def;
		try {
			return getDouble(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean getBoolean(String name) throws JSONException {
		Object o = get(name);
		if(o == JSON.TRUE) return true;
		if(o == JSON.FALSE) return false;
		if(o instanceof Boolean) return ((Boolean) o).booleanValue();
		if(o instanceof String) {
			String s = (String) o;
			s = s.toLowerCase();
			if(s.equals("true")) return true;
			if(s.equals("false")) return false;
		}
		throw new JSONException("Not boolean: " + o);
	}

	public boolean getBoolean(String name, boolean def) {
		if(!has(name)) return def;
		try {
			return getBoolean(name);
		} catch (Exception e) {
			return def;
		}
	}
	
	public boolean isNull(String name) {
		return JSON.isNull(getNullable(name));
	}

	public void put(String name, String s) {
		table.put(name, s);
	}

	public void put(String name, boolean b) {
		table.put(name, new Boolean(b));
	}

	public void put(String name, double d) {
		table.put(name, new Double(d));
	}

	public void put(String name, int i) {
		table.put(name, new Integer(i));
	}

	public void put(String name, long l) {
		table.put(name, new Long(l));
	}
	
	public void put(String name, Object obj) {
		table.put(name, JSON.getJSON(obj));
	}
	
	public void remove(String name) {
		table.remove(name);
	}
	
	public void clear() {
		table.clear();
	}
	
	public int size() {
		return table.size();
	}
	
	public String toString() {
		return build();
	}
	
	public boolean equals(Object obj) {
		if(this == obj || super.equals(obj)) {
			return true;
		}
		return similar(obj);
	}
	
	public boolean similar(Object obj) {
        if(!(obj instanceof JSONObject)) {
            return false;
        }
        int size = size();
        if(size != ((JSONObject)obj).size()) {
        	return false;
        }
    	Enumeration keys = table.keys();
    	while(keys.hasMoreElements()) {
    		String key = (String) keys.nextElement();
    		Object a = get(key);
    		Object b = ((JSONObject)obj).get(key);
    		if(a == b) {
    			continue;
    		}
    		if(a == null) {
    			return false;
    		}
    		if(a instanceof AbstractJSON) {
        		if (!((AbstractJSON)a).similar(b)) {
        			return false;
        		}
        	} else if(!a.equals(b)) {
    			return false;
    		}
    	}
    	return true;
	}

	public String build() {
		if (size() == 0)
			return "{}";
		StringBuffer s = new StringBuffer("{");
		Enumeration keys = table.keys();
		while (true) {
			String k = keys.nextElement().toString();
			s.append("\"").append(k).append("\":");
			Object v = table.get(k);
			if (v instanceof AbstractJSON) {
				s.append(((AbstractJSON) v).build());
			} else if (v instanceof String) {
				s.append("\"").append(JSON.escape_utf8((String) v)).append("\"");
			} else if(JSON.json_null.equals(v)) {
				s.append((String) null);
			} else {
				s.append(v);
			}
			if (!keys.hasMoreElements()) {
				break;
			}
			s.append(",");
		}
		s.append("}");
		return s.toString();
	}

	protected String format(int l) {
		int size = size();
		if (size == 0)
			return "{}";
		String t = "";
		for (int i = 0; i < l; i++) {
			t = t.concat(JSON.FORMAT_TAB);
		}
		String t2 = t.concat(JSON.FORMAT_TAB);
		StringBuffer s = new StringBuffer("{\n");
		s.append(t2);
		Enumeration keys = table.keys();
		int i = 0;
		while(keys.hasMoreElements()) {
			String k = keys.nextElement().toString();
			s.append("\"").append(k).append("\": ");
			Object v = null;
			try {
				v = get(k);
			} catch (JSONException e) {
			}
			if (v instanceof AbstractJSON) {
				s.append(((AbstractJSON) v).format(l + 1));
			} else if (v instanceof String) {
				s.append("\"").append(JSON.escape_utf8((String) v)).append("\"");
			} else if(v == JSON.json_null) {
				s.append((String) null);
			} else {
				s.append(v);
			}
			i++;
			if (i < size) {
				s.append(",\n").append(t2);
			}
		}
		if (l > 0) {
			s.append("\n").append(t).append("}");
		} else {
			s.append("\n}");
		}
		return s.toString();
	}

	public Enumeration keys() {
		return table.keys();
	}

	public JSONArray keysAsArray() {
		JSONArray array = new JSONArray();
		Enumeration keys = table.keys();
		while(keys.hasMoreElements()) {
			array.add(keys.nextElement());
		}
		return array;
	}

}
