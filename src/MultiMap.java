
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* ------------------------------------------------------------ */
/** A multi valued Map.
 * This Map specializes HashMap and provides methods
 * that operate on multi valued items.
 * <P>
 * Implemented as a map of LazyList values
 *
 * @see LazyList
 * @version $Id: MultiMap.java,v 1.11.2.1 2004/03/16 17:58:01 gregwilkins Exp $
 * @author Greg Wilkins (gregw)
 */
@SuppressWarnings({ "serial", "unchecked" })
public class MultiMap extends HashMap
    implements Cloneable, Serializable
{
    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public MultiMap()
    {}

    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param size Capacity of the map
     */
    public MultiMap(int size)
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param map
     */
    public MultiMap(Map map)
    {
        super(/*(map.size()*3)/2*/);
        putAll(map);
    }

    /* ------------------------------------------------------------ */
    /** Get multiple values.
     * Single valued entries are converted to singleton lists.
     * @param name The entry key.
     * @return Unmodifieable List of values.
     */
    public List getValues(Object name)
    {
        return LazyList.getList(super.get(name),true);
    }

    /* ------------------------------------------------------------ */
    /** Get a value from a multiple value.
     * If the value is not a multivalue, then index 0 retrieves the
     * value or null.
     * @param name The entry key.
     * @param i Index of element to get.
     * @return Unmodifieable List of values.
     */
    public Object getValue(Object name,int i)
    {
        Object l=super.get(name);
        if (i==0 && LazyList.size(l)==0)
            return null;
        return LazyList.get(l,i);
    }


    /* ------------------------------------------------------------ */
    /** Get value as String.
     * Single valued items are converted to a String with the toString()
     * Object method. Multi valued entries are converted to a comma separated
     * List.  No quoting of commas within values is performed.
     * @param name The entry key.
     * @return String value.
     */
    public String getString(Object name)
    {
        Object l=super.get(name);
        switch(LazyList.size(l))
        {
          case 0:
              return null;
          case 1:
              Object o=LazyList.get(l,0);
              return o==null?null:o.toString();
          default:
              StringBuffer values=new StringBuffer(128);
              synchronized(values)
              {
                  for (int i=0; i<LazyList.size(l); i++)
                  {
                      Object e=LazyList.get(l,i);
                      if (e!=null)
                      {
                          if (values.length()>0)
                              values.append(',');
                          values.append(e.toString());
                      }
                  }
                  return values.toString();
              }
        }
    }

    /* ------------------------------------------------------------ */
    public Object get(Object name)
    {
        Object l=super.get(name);
        switch(LazyList.size(l))
        {
          case 0:
              return null;
          case 1:
              Object o=LazyList.get(l,0);
              return o;
          default:
              return LazyList.getList(l,true);
        }
    }

    /* ------------------------------------------------------------ */
    /** Put and entry into the map.
     * @param name The entry key.
     * @param value The entry value.
     * @return The previous value or null.
     */
    public Object put(Object name, Object value)
    {
        return super.put(name,LazyList.add(null,value));
    }

    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key.
     * @param values The List of multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, List values)
    {
        return super.put(name,values);
    }

    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key.
     * @param values The String array of multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, String[] values)
    {
        Object list=null;
        for (int i=0;i<values.length;i++)
            list=LazyList.add(list,values[i]);
        return put(name,list);
    }


    /* ------------------------------------------------------------ */
    /** Add value to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key.
     * @param value The entry value.
     */
    public void add(Object name, Object value)
    {
        Object lo = super.get(name);
        Object ln = LazyList.add(lo,value);
        if (lo!=ln)
            super.put(name,ln);
    }

    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key.
     * @param values The List of multiple values.
     */
    public void addValues(Object name, List values)
    {
        Object lo = super.get(name);
        Object ln = LazyList.add(lo,values);
        if (lo!=ln)
            super.put(name,ln);
    }

    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key.
     * @param values The String array of multiple values.
     */
    public void addValues(Object name, String[] values)
    {
        Object lo = super.get(name);
        Object ln = LazyList.add(lo,Arrays.asList(values));
        if (lo!=ln)
            super.put(name,ln);
    }

    /* ------------------------------------------------------------ */
    /** Remove value.
     * @param name The entry key.
     * @param value The entry value.
     * @return true if it was removed.
     */
    public boolean removeValue(Object name,Object value)
    {
        Object lo = super.get(name);
        Object ln=lo;
        int s=LazyList.size(lo);
        if (s>0)
            ln=LazyList.remove(lo,value);
        return LazyList.size(ln)!=s;
    }

    /* ------------------------------------------------------------ */
    /** Put all contents of map.
     * @param m Map
     */
    public void putAll(Map m)
    {
        Iterator i = m.entrySet().iterator();
        boolean multi=m instanceof MultiMap;
        while(i.hasNext())
        {
            Map.Entry entry =
                (Map.Entry)i.next();
            if (multi)
                super.put(entry.getKey(),LazyList.clone(entry.getValue()));
            else
                put(entry.getKey(),entry.getValue());
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Map of String arrays
     */
    public Map toStringArrayMap()
    {
        HashMap map = new HashMap(size()*3/2);

        Iterator i = super.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry entry = (Map.Entry)i.next();
            Object l = entry.getValue();
            map.put(entry.getKey(),LazyList.toStringArray(l));
        }
        return map;
    }

    /* ------------------------------------------------------------ */
    public Object clone()
    {
        return new MultiMap(this);
    }

	public MultiMap
	readFromFile (String filename)
	{
		FileInputStream fis = null ;
		ObjectInputStream ois = null ;

		try {
		fis = new FileInputStream ( filename ) ;
		ois = new ObjectInputStream ( fis ) ;

		Object ob =  ois.readObject();

		if ( ob != null )
		{
		  MultiMap map =  (MultiMap)ob;
		  								//ois.readObject();
		  return map;
		}
		}
		catch ( EOFException e )
		{
			System.err.println("==End Of File reached==\n\n") ;
		}
		catch ( Exception e )
		{
			System.err.println("== Error ===" + e + "\n") ;

		}
		return null;
	}
	public void writeToFile (String filename) throws IOException
	{
		//System.err.println("Writeme");
		FileOutputStream fos = null ;
		ObjectOutputStream oos = null ;

		try {
		fos = new FileOutputStream ( filename ) ;
		oos = new ObjectOutputStream ( fos ) ;

		oos.writeObject( this );
		System.out.println("Written...");
		}
		catch ( Exception e )
		{     }
		finally
		{
		if ( oos != null )

		  oos.close ();
		}

	}

    public static void main(String[] args) {
		MultiMap mm = new MultiMap();

		mm.add("london", "USA");
		mm.add("paris", "france");
		mm.add("london", "england");

		System.out.println(mm.getValues("london"));
	}
}