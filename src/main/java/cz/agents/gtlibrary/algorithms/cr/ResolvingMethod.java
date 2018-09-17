package cz.agents.gtlibrary.algorithms.cr;

public enum ResolvingMethod {
    RESOLVE_MCCFR,
    RESOLVE_CFR,
    RESOLVE_RANDOM;

    static ResolvingMethod fromString(String s) {
        return ResolvingMethod.valueOf(s);
    }
}
