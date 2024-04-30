package org.schematik.util;

public class Tuple2<K, V> {

    private K first;
    private V second;

    public Tuple2(K first, V second){
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Tuple2<?,?>)) {
            return false;
        }

        try {
            Tuple2<K, V> otherTuple = (Tuple2<K, V>) other;

            return first.equals(otherTuple.getFirst()) && second.equals(otherTuple.getSecond());
        } catch (Exception e) {
            return false;
        }
    }
}