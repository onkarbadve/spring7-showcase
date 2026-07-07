/**
 * Root package. {@code @NullMarked} makes every type/parameter/return value in this
 * package non-null by default (JSpecify). Individual elements opt out with
 * {@code @Nullable}. Spring Framework 7 understands these annotations for
 * compile-time null checking and at runtime (e.g. constructor/setter injection).
 */
@NullMarked
package com.example.showcase;

import org.jspecify.annotations.NullMarked;
