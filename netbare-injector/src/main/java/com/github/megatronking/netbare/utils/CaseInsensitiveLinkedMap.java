/*  NetBare - An android network capture and injection library.
 *  Copyright (C) 2018-2019 Megatron King
 *  Copyright (C) 2018-2019 GuoShi
 *
 *  NetBare is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU General Public License as published by the Free Software Found-
 *  ation, either version 3 of the License, or (at your option) any later version.
 *
 *  NetBare is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with NetBare.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.megatronking.netbare.utils;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link CaseInsensitiveLinkedMap} is a {@link LinkedHashMap} from <code>String</code> keys to
 * values which is case-insensitive and case-preserving with respect to the keys in the map.
 *
 * This class is not necessarily thread safe.
 *
 * @author Megatron King
 * @since 2018-12-21 23:55
 */
public class CaseInsensitiveLinkedMap<V> extends AbstractMap<String, V> {

    private final Map<CaseInsensitiveKey, V> map;

    private static final class KeySet extends AbstractSet<String> {

        private static final class KeySetIterator implements Iterator<String> {

            private Iterator<CaseInsensitiveKey> iterator;

            private KeySetIterator(Iterator<CaseInsensitiveKey> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public String next() {
                return this.iterator.next().toString();
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }
        }

        private final Set<CaseInsensitiveKey> keySet;

        private KeySet(Set<CaseInsensitiveKey> keySet) {
            this.keySet = keySet;
        }

        /**
         * Not supported for sets returned by <code>Map.keySet</code>.
         */
        @Override
        public boolean add(String o) {
            throw new UnsupportedOperationException("Map.keySet must return a Set which does not support add");
        }

        /**
         * Not supported for sets returned by <code>Map.keySet</code>.
         */
        @Override
        public boolean addAll(@NonNull Collection<? extends String> c) {
            throw new UnsupportedOperationException("Map.keySet must return a Set which does not support addAll");
        }

        @Override
        public void clear() {
            this.keySet.clear();
        }

        @Override
        public boolean contains(Object o) {
            return o instanceof String && this.keySet.contains(CaseInsensitiveKey.objectToKey(o));
        }

        @Override
        public Iterator<String> iterator() {
            return new KeySetIterator(this.keySet.iterator());
        }

        @Override
        public boolean remove(Object o) {
            // The following can throw ClassCastException which conforms to the method specification.
            return this.keySet.remove(CaseInsensitiveKey.objectToKey(o));
        }

        @Override
        public int size() {
            return this.keySet.size();
        }

    }

    private static final class EntrySet<V> extends AbstractSet<Entry<String, V>> {

        private static final class MapEntry<V> implements Entry<String, V> {

            private final Entry<CaseInsensitiveLinkedMap.CaseInsensitiveKey, V> entry;

            private MapEntry(Entry<CaseInsensitiveLinkedMap.CaseInsensitiveKey, V> entry) {
                this.entry = entry;
            }

            @Override
            public String getKey() {
                return this.entry.getKey().toString();
            }

            @Override
            public V getValue() {
                return this.entry.getValue();
            }

            @Override
            public V setValue(V value) {
                return this.entry.setValue(value);
            }

            private Entry<CaseInsensitiveLinkedMap.CaseInsensitiveKey, V> getEntry() {
                return this.entry;
            }
        }

        private static final class EntrySetIterator<V> implements Iterator<Entry<String, V>> {

            private final Iterator<Entry<CaseInsensitiveKey, V>> iterator;

            private EntrySetIterator(Iterator<Entry<CaseInsensitiveKey, V>> iterator) {
                this.iterator = iterator;
            }

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public Entry<String, V> next() {
                return new MapEntry<>(this.iterator.next());
            }

            @Override
            public void remove() {
                this.iterator.remove();
            }

        }

        private final Set<Entry<CaseInsensitiveKey, V>> entrySet;

        private final CaseInsensitiveLinkedMap<V> map;

        private EntrySet(Set<Entry<CaseInsensitiveKey, V>> entrySet, CaseInsensitiveLinkedMap<V> map) {
            this.entrySet = entrySet;
            this.map = map;
        }

        /**
         * Not supported for sets returned by <code>Map.entrySet</code>.
         */
        @Override
        public boolean add(Entry<String, V> o) {
            throw new UnsupportedOperationException("Map.entrySet must return a Set which does not support add");
        }

        /**
         * Not supported for sets returned by <code>Map.entrySet</code>.
         */
        @Override
        public boolean addAll(@NonNull Collection<? extends Entry<String, V>> c) {
            throw new UnsupportedOperationException("Map.entrySet must return a Set which does not support addAll");
        }

        @Override
        public void clear() {
            this.entrySet.clear();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry<String, V> e = (Entry<String, V>) o;
                V value = this.map.get(e.getKey());
                return value.equals(e.getValue());
            }
            return false;
        }

        @Override
        public Iterator<Entry<String, V>> iterator() {
            return new EntrySetIterator<>(this.entrySet.iterator());
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            return this.entrySet.remove(((MapEntry<V>) o).getEntry());
        }

        @Override
        public int size() {
            return this.entrySet.size();
        }

    }

    static final class CaseInsensitiveKey {

        private final String key;

        private CaseInsensitiveKey(String key) {
            this.key = key;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + key.toLowerCase().hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CaseInsensitiveKey other = (CaseInsensitiveKey) obj;
            return key == null ? other.key == null : key.equalsIgnoreCase(other.key);
        }

        @Override
        public String toString() {
            return key;
        }

        /**
         * Convert the given key <code>Object</code> to a {@link CaseInsensitiveKey}.
         * <p/>
         * Pre-condition: <code>key</code> instanceof <code>String</code>
         *
         * @param key the key to be converted
         * @return the <code>CaseInsensitiveKey</code> corresponding to the given key
         */
        public static CaseInsensitiveKey objectToKey(Object key) {
            return new CaseInsensitiveKey((String) key);
        }

    }

    public CaseInsensitiveLinkedMap(int initialCapacity) {
        this.map = new LinkedHashMap<>(initialCapacity);
    }

    public CaseInsensitiveLinkedMap() {
        this.map = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public CaseInsensitiveLinkedMap(Map<String, ? extends V> map) {
        this.map = new LinkedHashMap<>(map.size());
        if (map instanceof CaseInsensitiveLinkedMap) {
            this.map.putAll(((CaseInsensitiveLinkedMap) map).map);
        } else {
            for (Entry<String, ? extends V> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    public CaseInsensitiveLinkedMap(CaseInsensitiveLinkedMap<? extends V> map) {
        this.map = new LinkedHashMap<>(map.size());
        this.map.putAll(map.map);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && this.map.containsKey(CaseInsensitiveKey.objectToKey(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    @NonNull
    public Set<Entry<String, V>> entrySet() {
        return new EntrySet<>(this.map.entrySet(), this);
    }

    @Override
    public V get(Object key) {
        return key instanceof String ? this.map.get(CaseInsensitiveKey.objectToKey(key)) : null;
    }

    @Override
    @NonNull
    public Set<String> keySet() {
        return new KeySet(this.map.keySet());
    }

    @Override
    public V put(String key, V value) {
        if (key == null) {
            throw new NullPointerException("CaseInsensitiveLinkedMap does not permit null keys");
        }
        return this.map.put(CaseInsensitiveKey.objectToKey(key), value);
    }

    @Override
    public V remove(Object key) {
        return key instanceof String ? this.map.remove(CaseInsensitiveKey.objectToKey(key)) : null;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    @NonNull
    public Collection<V> values() {
        return this.map.values();
    }

}
