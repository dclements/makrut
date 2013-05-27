/**
 * ResultSetHandler implementations.  The ResultSetHandlers provided by the
 * Apache commons library will still work, but can sometimes (e.g., the ScalarHandler)
 * return null, which doesn't always play nicely with makrut's caching system, and
 * also can return mutable values (e.g., MapHandler), which *really* doesn't work well
 * with makrut's caching system (which assumes immutable returns).
 *
 * These are designed to minimize the amount of code that the end-user has to write
 * while working particularly well with makrut by, for example, providing null safety
 * and immutable objects.
 */
@com.readytalk.makrut.util.ReturnTypesAreNonnullByDefault
@javax.annotation.ParametersAreNonnullByDefault
package com.readytalk.makrut.db.handlers;
