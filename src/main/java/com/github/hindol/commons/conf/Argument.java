package com.github.hindol.commons.conf;

public abstract class Argument {

    public static Unresolvable resolver() {
        return new Chain();
    }

    public enum Type {
        ENVIRONMENT_VARIABLE,
        PROPERTY
    }

    public interface Unresolvable {
        Resolvable firstTry(String name, Type type);
    }

    public interface Resolvable {
        Resolvable thenTry(String name, Type type);
        Resolvable orElse(String valueIfMissing);
        String resolve();
    }

    private static class Chain implements Unresolvable, Resolvable {

        private String mResolved;

        private Chain() {}

        @Override
        public Resolvable firstTry(String name, Type type) {
            return new Chain().thenTry(name, type);
        }

        @Override
        public Resolvable thenTry(String name, Type type) {
            if (mResolved == null) {
                try {
                    switch (type) {
                        case PROPERTY:
                            mResolved = System.getProperty(name);
                            break;

                        case ENVIRONMENT_VARIABLE:
                            mResolved = System.getenv(name);
                            break;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }

            return this;
        }

        @Override
        public Resolvable orElse(String valueIfMissing) {
            if (mResolved == null) {
                mResolved = valueIfMissing;
            }
            return this;
        }

        public String resolve() {
            return mResolved;
        }
    }
}
