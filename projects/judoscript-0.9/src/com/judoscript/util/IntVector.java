/*
 * See copyright.txt for copyright notice.
 *
 *************************** CHANGE LOG ***************************
 *
 * Authors: JH  = James Jianbo Huang, judoscript@hotmail.com
 *
 * 05-31-2002  JH   Incepted.
 *                  Started with java.util.Vecotr.
 *
 *
 **********  No tabs. Indent 2 spaces. Follow the style. **********/


package com.judoscript.util;

import java.util.*;

public class IntVector implements Cloneable, java.io.Serializable
{
    protected int elementData[];
    protected int elementCount;
    protected int capacityIncrement;

    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment. 
     *
     * @param   initialCapacity     the initial capacity of the vector.
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IntVector(int initialCapacity, int capacityIncrement) {
	super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
	this.elementData = new int[initialCapacity];
	this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty vector with the specified initial capacity and 
     * with its capacity increment equal to zero.
     *
     * @param   initialCapacity   the initial capacity of the vector.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IntVector(int initialCapacity) {
	this(initialCapacity, 0);
    }

    /**
     * Constructs an empty vector so that its internal data array 
     * has size <tt>10</tt> and its standard capacity increment is 
     * zero. 
     */
    public IntVector() {
	this(10);
    }

    /**
     * Constructs a vector with the provided int array.
     *
     * @param   init the initial int array
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IntVector(int[] init, int capacityIncrement) {
        applyIntArray(init);
        this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs a vector with the provided int array
     * with capacityIncrement set to 0.
     *
     * @param   init the initial int array
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the vector overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public IntVector(int[] init) {
	this(init, 0);
    }

    /**
     * Applies the supplied int array as the content for this IntVector.
     */
    public void applyIntArray(int[] a) {
        elementData = a;
        elementCount = a.length;
    }

    /**
     * Copies the components of this vector into the specified array. The 
     * item at index <tt>k</tt> in this vector is copied into component 
     * <tt>k</tt> of <tt>anArray</tt>. The array must be big enough to hold 
     * all the objects in this vector, else an 
     * <tt>IndexOutOfBoundsException</tt> is thrown.
     *
     * @param   anArray   the array into which the components get copied.
     */
    public synchronized void copyInto(int anArray[]) {
	System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    /**
     * Trims the capacity of this vector to be the vector's current 
     * size. If the capacity of this cector is larger than its current 
     * size, then the capacity is changed to equal the size by replacing 
     * its internal data array, kept in the field <tt>elementData</tt>, 
     * with a smaller one. An application can use this operation to 
     * minimize the storage of a vector. 
     */
    public synchronized void trimToSize() {
	int oldCapacity = elementData.length;
	if (elementCount < oldCapacity) {
	    int oldData[] = elementData;
	    elementData = new int[elementCount];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * Increases the capacity of this vector, if necessary, to ensure 
     * that it can hold at least the number of components specified by 
     * the minimum capacity argument. <p>If the current capacity of this 
     * vector is less than <tt>minCapacity</tt>, then its capacity is
     * increased by replacing its internal data array, kept in the field 
     * <tt>elementData</tt>, with a larger one. The size of the new data 
     * array will be the old size plus <tt>capacityIncrement</tt>, unless 
     * the value of <tt>capacityIncrement</tt> is nonpositive, in which 
     * case the new capacity will be twice the old capacity; but if
     * this new size is still smaller than <tt>minCapacity</tt>, then the 
     * new capacity will be <tt>minCapacity</tt>. 
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public synchronized void ensureCapacity(int minCapacity) {
	ensureCapacityHelper(minCapacity);
    }
    
    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this 
     * method for ensuring capacity without incurring the cost of an 
     * extra synchronization.
     *
     * @see java.util.IntVector#ensureCapacity(int)
     */ 
    private void ensureCapacityHelper(int minCapacity) {
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    int oldData[] = elementData;
	    int newCapacity = (capacityIncrement > 0) ?
		(oldCapacity + capacityIncrement) : (oldCapacity * 2);
    	    if (newCapacity < minCapacity) {
		newCapacity = minCapacity;
	    }
	    elementData = new int[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * Sets the size of this vector. If the new size is greater than the 
     * current size, new <code>null</code> items are added to the end of 
     * the vector. If the new size is less than the current size, all 
     * components at index <code>newSize</code> and greater are discarded.
     *
     * @param   newSize   the new size of this vector.
     * @throws  ArrayIndexOutOfBoundsException if new size is negative.
     */
    public synchronized void setSize(int newSize) {
	if (newSize > elementCount) {
	    ensureCapacityHelper(newSize);
	} else {
	    for (int i = newSize ; i < elementCount ; i++) {
		elementData[i] = 0;
	    }
	}
	elementCount = newSize;
    }

    /**
     * Returns the current capacity of this vector.
     *
     * @return  the current capacity (the length of its internal 
     *          data arary, kept in the field <tt>elementData</tt> 
     *          of this vector.
     */
    public int capacity() {
	return elementData.length;
    }

    /**
     * Returns the number of components in this vector.
     *
     * @return  the number of components in this vector.
     */
    public int size() {
	return elementCount;
    }

    /**
     * Tests if this vector has no components.
     *
     * @return  <code>true</code> if and only if this vector has 
     *          no components, that is, its size is zero;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return elementCount == 0;
    }

    /**
     * Tests if the specified object is a component in this vector.
     *
     * @param   elem   an object.
     * @return  <code>true</code> if and only if the specified object 
     * is the same as a component in this vector, as determined by the 
     * <tt>equals</tt> method; <code>false</code> otherwise.
     */
    public boolean contains(int elem) {
	return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the first occurence of the given argument, testing 
     * for equality using the <code>equals</code> method. 
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>-1</code> if the object is not found.
     * @see     int#equals(int)
     */
    public int indexOf(int elem) {
	return indexOf(elem, 0);
    }

    /**
     * Searches for the first occurence of the given argument, beginning 
     * the search at <code>index</code>, and testing for equality using 
     * the <code>equals</code> method. 
     *
     * @param   elem    an object.
     * @param   index   the index to start searching from.
     * @return  the index of the first occurrence of the object argument in
     *          this vector at position <code>index</code> or later in the
     *          vector, that is, the smallest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &gt;= index)</tt> is 
     *          <tt>true</tt>; returns <code>-1</code> if the object is not 
     *           found.
     * @see     int#equals(int)
     */
    public synchronized int indexOf(int elem, int index) {
	for (int i = index ; i < elementCount ; i++)
	    if (elem == elementData[i])
		return i;
	return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified object in
     * this vector.
     *
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified object in
     *          this vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k])</tt> is <tt>true</tt>; 
     *          returns <code>-1</code> if the object is not found.
     */
    public int lastIndexOf(int elem) {
	return lastIndexOf(elem, elementCount-1);
    }

    /**
     * Searches backwards for the specified object, starting from the 
     * specified index, and returns an index to it. 
     *
     * @param  elem    the desired component.
     * @param  index   the index to start searching from.
     * @return the index of the last occurrence of the specified object in this
     *          vector at position less than <code>index</code> in the 
     *          vector, that is, the largest value <tt>k</tt> such that 
     *          <tt>elem.equals(elementData[k]) && (k &lt;= index)</tt> is 
     *          <tt>true</tt>; <code>-1</code> if the object is not found.
     */
    public synchronized int lastIndexOf(int elem, int index) {
	for (int i = index; i >= 0; i--)
	    if (elem == elementData[i])
		return i;
	return -1;
    }

    /**
     * Returns the component at the specified index.<p>
     *
     * This method is identical in functionality to the get method
     * (which is part of the List interface).
     *
     * @param      index   an index into this vector.
     * @return     the component at the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the <tt>index</tt> 
     *             is negative or not less than the current size of this 
     *             <tt>IntVector</tt> object.
     *             given.
     * @see	   #get(int)
     * @see	   List
     */
    public synchronized int elementAt(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
	}
	/* Since try/catch is free, except when the exception is thrown,
	   put in this extra try/catch to catch negative indexes and
	   display a more informative error message.  This might not
	   be appropriate, especially if we have a decent debugging
	   environment - JP. */
	try {
	    return elementData[index];
	} catch (ArrayIndexOutOfBoundsException e) {
	    throw new ArrayIndexOutOfBoundsException(index + " < 0");
	}
    }

    /**
     * Returns the first component (the item at index <tt>0</tt>) of 
     * this vector.
     *
     * @return     the first component of this vector.
     * @exception  NoSuchElementException  if this vector has no components.
     */
    public synchronized int firstElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[0];
    }

    /**
     * Returns the last component of the vector.
     *
     * @return  the last component of the vector, i.e., the component at index
     *          <code>size()&nbsp;-&nbsp;1</code>.
     * @exception  NoSuchElementException  if this vector is empty.
     */
    public synchronized int lastElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[elementCount - 1];
    }

    /**
     * Sets the component at the specified <code>index</code> of this 
     * vector to be the specified object. The previous component at that 
     * position is discarded.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the set method
     * (which is part of the List interface). Note that the set method reverses
     * the order of the parameters, to more closely match array usage.  Note
     * also that the set method returns the old value that was stored at the
     * specified position.
     *
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see        List
     * @see	   #set(int, java.lang.int)
     */
    public synchronized void setElementAt(int obj, int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	elementData[index] = obj;
    }

    /**
     * Deletes the component at the specified index. Each component in 
     * this vector with an index greater or equal to the specified 
     * <code>index</code> is shifted downward to have an index one 
     * smaller than the value it had previously. The size of this vector 
     * is decreased by <tt>1</tt>.<p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than the current size of the vector. <p>
     *
     * This method is identical in functionality to the remove method
     * (which is part of the List interface).  Note that the remove method
     * returns the old value that was stored at the specified position.
     *
     * @param      index   the index of the object to remove.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #remove(int)
     * @see	   List
     */
    public synchronized void removeElementAt(int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	else if (index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	int j = elementCount - index - 1;
	if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
	}
	elementData[--elementCount] = 0;
    }

    /**
     * Inserts the specified object as a component in this vector at the 
     * specified <code>index</code>. Each component in this vector with 
     * an index greater or equal to the specified <code>index</code> is 
     * shifted upward to have an index one greater than the value it had 
     * previously. <p>
     *
     * The index must be a value greater than or equal to <code>0</code> 
     * and less than or equal to the current size of the vector. (If the
     * index is equal to the current size of the vector, the new element
     * is appended to the IntVector.)<p>
     *
     * This method is identical in functionality to the add(int, int) method
     * (which is part of the List interface). Note that the add method reverses
     * the order of the parameters, to more closely match array usage.
     *
     * @param      obj     the component to insert.
     * @param      index   where to insert the new component.
     * @exception  ArrayIndexOutOfBoundsException  if the index was invalid.
     * @see        #size()
     * @see	   #add(int, int)
     * @see	   List
     */
    public synchronized void insertElementAt(int obj, int index) {
	if (index >= elementCount + 1) {
	    throw new ArrayIndexOutOfBoundsException(index
						     + " > " + elementCount);
	}
	ensureCapacityHelper(elementCount + 1);
	System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
	elementData[index] = obj;
	elementCount++;
    }

    /**
     * Adds the specified component to the end of this vector, 
     * increasing its size by one. The capacity of this vector is 
     * increased if its size becomes greater than its capacity. <p>
     *
     * This method is identical in functionality to the add(int) method
     * (which is part of the List interface).
     *
     * @param   obj   the component to be added.
     * @see	   #add(int)
     * @see	   List
     */
    public synchronized void addElement(int obj) {
	ensureCapacityHelper(elementCount + 1);
	elementData[elementCount++] = obj;
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument 
     * from this vector. If the object is found in this vector, each 
     * component in the vector with an index greater or equal to the 
     * object's index is shifted downward to have an index one smaller 
     * than the value it had previously.<p>
     *
     * This method is identical in functionality to the remove(int) 
     * method (which is part of the List interface).
     *
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          vector; <code>false</code> otherwise.
     * @see	List#remove(int)
     * @see	List
     */
    public synchronized boolean removeElement(int obj) {
	int i = indexOf(obj);
	if (i >= 0) {
	    removeElementAt(i);
	    return true;
	}
	return false;
    }

    /**
     * Removes all components from this vector and sets its size to zero.<p>
     *
     * This method is identical in functionality to the clear method
     * (which is part of the List interface).
     *
     * @see	#clear
     * @see	List
     */
    public synchronized void removeAllElements() {
	elementCount = 0;
    }

    /**
     * Returns a clone of this vector. The copy will contain a
     * reference to a clone of the internal data array, not a reference 
     * to the original internal data array of this <tt>IntVector</tt> object. 
     *
     * @return  a clone of this vector.
     */
    public synchronized Object clone() {
	try { 
	    IntVector v = (IntVector)super.clone();
	    v.elementData = new int[elementCount];
	    System.arraycopy(elementData, 0, v.elementData, 0, elementCount);
	    return v;
	} catch (CloneNotSupportedException e) { 
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }

    /**
     * Returns an array containing all of the elements in this IntVector
     * in the correct order.
     */
    public synchronized int[] toIntArray() {
	int[] result = new int[elementCount];
	System.arraycopy(elementData, 0, result, 0, elementCount);
	return result;
    }

    /**
     * Returns an array containing all of the elements in this IntVector in the
     * correct order.  The runtime type of the returned array is that of the
     * specified array.  If the IntVector fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this IntVector.<p>
     *
     * If the IntVector fits in the specified array with room to spare
     * (i.e., the array has more elements than the IntVector),
     * the element in the array immediately following the end of the
     * IntVector is set to null.  This is useful in determining the length
     * of the IntVector <em>only</em> if the caller knows that the IntVector
     * does not contain any null elements.
     *
     * @param a the array into which the elements of the IntVector are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the IntVector.
     * @exception ArrayStoreException the runtime type of a is not a supertype
     * of the runtime type of every element in this IntVector.
     */
    public synchronized int[] toArray(int a[]) {
        if (a.length < elementCount)
            a = (int[])java.lang.reflect.Array.newInstance(
                                a.getClass().getComponentType(), elementCount);

	System.arraycopy(elementData, 0, a, 0, elementCount);

        if (a.length > elementCount)
            a[elementCount] = 0;

        return a;
    }

    // Positional Access Operations

    /**
     * Returns the element at the specified position in this IntVector.
     *
     * @param index index of element to return.
     * @exception ArrayIndexOutOfBoundsException index is out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     */
    public synchronized int getAt(int index) {
	if (index >= elementCount)
	    throw new ArrayIndexOutOfBoundsException(index);

	return elementData[index];
    }

    /**
     * Replaces the element at the specified position in this IntVector with the
     * specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @exception ArrayIndexOutOfBoundsException index out of range
     *		  (index &lt; 0 || index &gt;= size()).
     * @exception IllegalArgumentException fromIndex &gt; toIndex.
     */
    public synchronized int set(int index, int element) {
	if (index >= elementCount)
	    throw new ArrayIndexOutOfBoundsException(index);

	int oldValue = elementData[index];
	elementData[index] = element;
	return oldValue;
    }

    /**
     * Appends the specified element to the end of this IntVector.
     *
     * @param o element to be appended to this IntVector.
     */
    public synchronized void add(int o) {
	ensureCapacityHelper(elementCount + 1);
	elementData[elementCount++] = o;
    }

    /**
     * Inserts the specified element at the specified position in this IntVector.
     * Shifts the element currently at that position (if any) and any
     * subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @exception ArrayIndexOutOfBoundsException index is out of range
     *		  (index &lt; 0 || index &gt; size()).
     */
    public void add(int index, int element) {
        insertElementAt(element, index);
    }

    /**
     * Removes the element at the specified position in this IntVector.
     * shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the IntVector.
     *
     * @exception ArrayIndexOutOfBoundsException index out of range (index
     * 		  &lt; 0 || index &gt;= size()).
     * @param index the index of the element to removed.
     */
    public synchronized int removeAt(int index) {
	if (index >= elementCount)
	    throw new ArrayIndexOutOfBoundsException(index);
	int oldValue = elementData[index];

	int numMoved = elementCount - index - 1;
	if (numMoved > 0)
	    System.arraycopy(elementData, index+1, elementData, index, numMoved);
	elementData[--elementCount] = 0;

	return oldValue;
    }

    /**
     * Removes all of the elements from this IntVector.  The IntVector will
     * be empty after this call returns (unless it throws an exception).
     */
    public void clear() {
        removeAllElements();
    }

    // Bulk Operations

    /**
     * Compares the specified int with this IntVector for equality.  Returns
     * true if and only if the specified int is also a List, both Lists
     * have the same size, and all corresponding pairs of elements in the two
     * Lists are <em>equal</em>.  (Two elements <code>e1</code> and
     * <code>e2</code> are <em>equal</em> if <code>(e1==null ? e2==null :
     * e1.equals(e2))</code>.)  In other words, two Lists are defined to be
     * equal if they contain the same elements in the same order.
     *
     * @param o the int to be compared for equality with this IntVector.
     * @return true if the specified int is equal to this IntVector
     */
    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Returns the hash code value for this IntVector.
     */
    public synchronized int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a string representation of this IntVector, containing
     * the String representation of each element.
     */
    public synchronized String toString() {
        return super.toString();
    }

    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the ArrayList by (toIndex - fromIndex) elements.  (If
     * toIndex==fromIndex, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param fromIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
	int numMoved = elementCount - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex,
                         numMoved);

	// Let gc do its work
	int newElementCount = elementCount - (toIndex-fromIndex);
	while (elementCount != newElementCount)
	    elementData[--elementCount] = 0;
    }
}
