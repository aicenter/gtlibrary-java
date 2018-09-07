package cz.agents.gtlibrary.algorithms.cr;

public enum ResolvingMethod {
    RESOLVE_MCCFR,
    RESOLVE_CFR;

    static ResolvingMethod fromString(String s) {
        return ResolvingMethod.valueOf(s);
    }
}
