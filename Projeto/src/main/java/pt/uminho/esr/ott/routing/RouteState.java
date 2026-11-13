package pt.uminho.esr.ott.routing;

/** Local domain state, not a wire code. IMP-008 produces only the first two values. */
public enum RouteState { UNAVAILABLE, INACTIVE, ACTIVATING, ACTIVE, SWITCHING }
