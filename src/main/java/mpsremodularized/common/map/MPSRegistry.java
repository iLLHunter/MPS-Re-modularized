package mpsremodularized.common.map;

/**
 * Author: MachineMuse (Claire Semple)
 * Created: 4:30 AM, 29/04/13
 * <p>
 */
public class MPSRegistry<T> extends MPSBiMap<String, T> {
    public T put(final String name, final T elem) {
        return this.putName(name, elem);
    }

    public String put(final T elem, final String name) {
        return this.putElem(elem, name);
    }
}
