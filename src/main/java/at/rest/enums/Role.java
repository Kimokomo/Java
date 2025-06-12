package at.rest.enums;

public enum Role {
    USER, ADMIN, SUPERADMIN, UNKNOWN;

    public static Role getDefault() {
        return USER;
    }
}
