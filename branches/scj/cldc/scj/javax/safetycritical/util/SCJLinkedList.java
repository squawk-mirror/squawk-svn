package javax.safetycritical.util;

public class SCJLinkedList {

    private Entry header = new Entry(null, null, null);
    private int size = 0;

    /**
     * Constructs an empty list.
     */
    public SCJLinkedList() {
        header.next = header.previous = header;
    }

    // /**
    // * Removes and returns the first element from this list.
    // *
    // * @return the first element from this list.
    // * @throws NoSuchElementException
    // * if this list is empty.
    // */
    // public Object removeFirst() {
    // return remove(header.next);
    // }
    //
    // /**
    // * Removes and returns the last element from this list.
    // *
    // * @return the last element from this list.
    // * @throws NoSuchElementException
    // * if this list is empty.
    // */
    // public Object removeLast() {
    // return remove(header.previous);
    // }

    /**
     * Inserts the given element at the beginning of this list.
     * 
     * @param o
     *            the element to be inserted at the beginning of this list.
     */
    public void addFirst(Object o) {
        addBefore(o, header.next);
    }

    /**
     * Appends the given element to the end of this list. (Identical in function
     * to the <tt>add</tt> method; included only for consistency.)
     * 
     * @param o
     *            the element to be inserted at the end of this list.
     */
    public void addLast(Object o) {
        addBefore(o, header);
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element. More
     * formally, returns <tt>true</tt> if and only if this list contains at
     * least one element <tt>e</tt> such that <tt>(o==null ? e==null
     * : o.equals(e))</tt>.
     * 
     * @param o
     *            element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list.
     */
    public int size() {
        return size;
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param o
     *            element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of
     *         <tt>Collection.add</tt>).
     */
    public boolean add(Object o) {
        addBefore(o, header);
        return true;
    }

    // /**
    // * Removes the first occurrence of the specified element in this list. If
    // * the list does not contain the element, it is unchanged. More formally,
    // * removes the element with the lowest index <tt>i</tt> such that
    // * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
    // element
    // * exists).
    // *
    // * @param o
    // * element to be removed from this list, if present.
    // * @return <tt>true</tt> if the list contained the specified element.
    // */
    // public boolean remove(Object o) {
    // if (o == null) {
    // for (Entry e = header.next; e != header; e = e.next) {
    // if (e.element == null) {
    // remove(e);
    // return true;
    // }
    // }
    // } else {
    // for (Entry e = header.next; e != header; e = e.next) {
    // if (o.equals(e.element)) {
    // remove(e);
    // return true;
    // }
    // }
    // }
    // return false;
    // }

    /**
     * Removes all of the elements from this list.
     */
    public void clear() {
        Entry e = header.next;
        while (e != header) {
            Entry next = e.next;
            e.next = e.previous = null;
            e.element = null;
            e = next;
        }
        header.next = header.previous = header;
        size = 0;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            index of element to return.
     * @return the element at the specified position in this list.
     * 
     * @throws IndexOutOfBoundsException
     *             if the specified index is out of range (
     *             <tt>index &lt; 0 || index &gt;= size()</tt>).
     */
    public Object get(int index) {
        return entry(index).element;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     * 
     * @param index
     *            index at which the specified element is to be inserted.
     * @param element
     *            element to be inserted.
     * 
     * @throws IndexOutOfBoundsException
     *             if the specified index is out of range (
     *             <tt>index &lt; 0 || index &gt; size()</tt>).
     */
    public void add(int index, Object element) {
        addBefore(element, (index == size ? header : entry(index)));
    }

    // /**
    // * Removes the element at the specified position in this list. Shifts any
    // * subsequent elements to the left (subtracts one from their indices).
    // * Returns the element that was removed from the list.
    // *
    // * @param index
    // * the index of the element to removed.
    // * @return the element previously at the specified position.
    // *
    // * @throws IndexOutOfBoundsException
    // * if the specified index is out of range (
    // * <tt>index &lt; 0 || index &gt;= size()</tt>).
    // */
    // public Object remove(int index) {
    // return remove(entry(index));
    // }

    /**
     * Return the indexed entry.
     */
    private Entry entry(int index) {

        Entry e = header;
        if (index < (size >> 1)) {
            for (int i = 0; i <= index; i++)
                e = e.next;
        } else {
            for (int i = size; i > index; i--)
                e = e.previous;
        }
        return e;
    }

    // Search Operations

    /**
     * Returns the index in this list of the first occurrence of the specified
     * element, or -1 if the List does not contain this element. More formally,
     * returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there is
     * no such index.
     * 
     * @param o
     *            element to search for.
     * @return the index in this list of the first occurrence of the specified
     *         element, or -1 if the list does not contain this element.
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element == null)
                    return index;
                index++;
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element))
                    return index;
                index++;
            }
        }
        return -1;
    }

    /**
     * Returns the index in this list of the last occurrence of the specified
     * element, or -1 if the list does not contain this element. More formally,
     * returns the highest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if there is
     * no such index.
     * 
     * @param o
     *            element to search for.
     * @return the index in this list of the last occurrence of the specified
     *         element, or -1 if the list does not contain this element.
     */
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (e.element == null)
                    return index;
            }
        } else {
            for (Entry e = header.previous; e != header; e = e.previous) {
                index--;
                if (o.equals(e.element))
                    return index;
            }
        }
        return -1;
    }

    // Queue operations.

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     * 
     * @return the head of this queue, or <tt>null</tt> if this queue is empty.
     * @since 1.5
     */
    public Object peek() {
        if (size == 0)
            return null;
        return getFirst();
    }

    /**
     * Retrieves, but does not remove, the head (first element) of this list.
     * 
     * @return the head of this queue.
     * @throws NoSuchElementException
     *             if this queue is empty.
     * @since 1.5
     */
    public Object element() {
        return getFirst();
    }

    // /**
    // * Retrieves and removes the head (first element) of this list.
    // *
    // * @return the head of this queue, or <tt>null</tt> if this queue is
    // empty.
    // * @since 1.5
    // */
    // public Object poll() {
    // if (size == 0)
    // return null;
    // return removeFirst();
    // }

    // /**
    // * Retrieves and removes the head (first element) of this list.
    // *
    // * @return the head of this queue.
    // * @throws NoSuchElementException
    // * if this queue is empty.
    // * @since 1.5
    // */
    // public Object remove() {
    // return removeFirst();
    // }

    /**
     * Adds the specified element as the tail (last element) of this list.
     * 
     * @param o
     *            the element to add.
     * @return <tt>true</tt> (as per the general contract of
     *         <tt>Queue.offer</tt>)
     * @since 1.5
     */
    public boolean offer(Object o) {
        return add(o);
    }

    private static class Entry {
        Object element;
        Entry next;
        Entry previous;

        Entry(Object element, Entry next, Entry previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
        }
    }

    private Entry addBefore(Object o, Entry e) {
        Entry newEntry = new Entry(o, e, e.previous);
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
        return newEntry;
    }

    // private Object remove(Entry e) {
    // Object result = e.element;
    // e.previous.next = e.next;
    // e.next.previous = e.previous;
    // e.next = e.previous = null;
    // e.element = null;
    // size--;
    // return result;
    // }

    public Object getFirst() {
        return header.next.element;
    }
}
